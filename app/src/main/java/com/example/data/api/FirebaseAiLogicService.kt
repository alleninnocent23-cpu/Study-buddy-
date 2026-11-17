package com.example.data.api

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Executes generative AI inference via provisioned Firebase AI Logic (Firebase.ai + App Check)
 * using 1M-token context Gemini models (gemini-3.5-flash-lite, gemini-3.8-flash, gemini-4-argon)
 * with zero client-side API key required.
 */
object FirebaseAiLogicService {
    private const val TAG = "FirebaseAiLogicService"
    const val VIRTUAL_KEY_ID = "firebase-ai-logic-appcheck"
    const val VIRTUAL_KEY_SENTINEL = "firebase-app-check"
    private const val AI_TIMEOUT_MS = 60_000L

    data class FirebaseTierModels(
        val primaryModel: String,
        val fallbackModel: String,
        val systemDirective: String
    )

    fun resolveFirebaseModels(tier: StudyBuddyModelTier): FirebaseTierModels {
        return when (tier) {
            StudyBuddyModelTier.FREE -> FirebaseTierModels(
                primaryModel = "gemini-3.5-flash-lite",
                fallbackModel = "gemini-3.8-flash",
                systemDirective = "ACTIVE MODE: StudyBuddy Free (1M Context). Deliver accurate, clear, concise academic explanations with core worked steps."
            )
            StudyBuddyModelTier.PLUS -> FirebaseTierModels(
                primaryModel = "gemini-3.8-flash",
                fallbackModel = "gemini-3.5-flash-lite",
                systemDirective = "ACTIVE MODE: StudyBuddy Plus (1M Context). Deliver thorough, detailed step-by-step reasoning, clear examples, and verify all mathematical working."
            )
            StudyBuddyModelTier.MAX -> FirebaseTierModels(
                primaryModel = "gemini-4-argon",
                fallbackModel = "gemini-3.8-flash",
                systemDirective = "ACTIVE MODE: StudyBuddy Max (1M Context). Apply deep analytical reasoning, rigorously verify multi-step derivations, surface edge cases and exam traps, and synthesize complex context completely."
            )
        }
    }

    fun isAvailable(context: Context?): Boolean {
        val ctx = context ?: return false
        return try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
            FirebaseApp.getApps(ctx).isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }

    fun isVirtualKey(entry: ApiKeyEntry): Boolean {
        return entry.id == VIRTUAL_KEY_ID || entry.key == VIRTUAL_KEY_SENTINEL
    }

    @Volatile
    private var lastDiagnosticProbeMs: Long = 0L

    private fun isAppCheckFailure(error: Throwable?): Boolean {
        return generateSequence(error) { it.cause }.any { t ->
            val m = t.message?.lowercase().orEmpty()
            m.contains("app check") ||
                m.contains("appcheck") ||
                m.contains("attestation") ||
                m.contains("too many attempts") ||
                m.contains("recaptcha")
        }
    }

    private fun formatExceptionSummary(error: Throwable): String {
        val type = error.javaClass.simpleName.ifBlank { "Exception" }
        val primary = error.localizedMessage?.trim().orEmpty()
        val causeMsg = error.cause?.localizedMessage?.trim()?.takeIf {
            it.isNotBlank() && !primary.contains(it, ignoreCase = true)
        }
        return buildString {
            if (primary.isNotBlank()) {
                append(primary)
            } else {
                append(type)
            }
            if (causeMsg != null) {
                append(" (cause: ").append(causeMsg).append(")")
            }
        }.take(200)
    }

    internal fun formatAppCheckFailureMessage(tokenError: String): String {
        val status = AppCheckDiagnostics.statusSummary
        val priorRootCause = AppCheckDiagnostics.lastUnderlyingTokenError
        if (tokenError.contains("too many attempts", ignoreCase = true)) {
            return if (!priorRootCause.isNullOrBlank()) {
                "App Check ($status) is cooling down after: $priorRootCause. Turn off any VPN or Private DNS, wait 2 minutes, and retry."
            } else {
                "App Check ($status) is temporarily rate-limited (Too many attempts). Turn off any VPN, Private DNS, or ad-blocker if active, wait 2-5 minutes, and retry."
            }
        }
        return "App Check ($status) failed: $tokenError. Disable any VPN or Private DNS if active, check your connection, and retry."
    }

    private suspend fun ensureAppCheckTokenReady() {
        val tokenError = try {
            withTimeout(10_000L) {
                suspendCancellableCoroutine<String?> { cont ->
                    FirebaseAppCheck.getInstance().getAppCheckToken(false)
                        .addOnSuccessListener {
                            AppCheckDiagnostics.clearTokenFailure()
                            if (cont.isActive) cont.resume(null)
                        }
                        .addOnFailureListener { ex ->
                            if (cont.isActive) cont.resume(formatExceptionSummary(ex))
                        }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: TimeoutCancellationException) {
            "App Check token request timed out."
        } catch (t: Throwable) {
            formatExceptionSummary(t)
        }

        if (!tokenError.isNullOrBlank()) {
            AppCheckDiagnostics.recordTokenFailure(tokenError)
            if (BuildConfig.DEBUG && !AppCheckDiagnostics.hasLaunchDebugToken) {
                throw IOException(
                    "Debug APK App Check is not authorized on standalone devices. Install StudyBuddy-release.apk (not StudyBuddy-debug.apk)."
                )
            }
            throw IOException(formatAppCheckFailureMessage(tokenError))
        }
    }

    suspend fun execute(request: AiExecutionRequest): String {
        val configuredFirebaseKey = try {
            FirebaseApp.getInstance().options.apiKey
        } catch (_: Throwable) {
            ""
        }
        if (configuredFirebaseKey == "ci-build-placeholder" || configuredFirebaseKey == "dummy_key_for_ci_build") {
            throw IOException(
                "This APK was built with a placeholder Firebase config instead of the provisioned app/google-services.json."
            )
        }
        ensureAppCheckTokenReady()
        val models = resolveFirebaseModels(request.tier)
        return try {
            executeSingleModel(models.primaryModel, models.systemDirective, request)
        } catch (e: CancellationException) {
            throw e
        } catch (primaryErr: Exception) {
            Log.e(
                TAG,
                "Firebase AI primary model ${models.primaryModel} failed: ${primaryErr.localizedMessage}",
                primaryErr
            )
            if (isAppCheckFailure(primaryErr)) {
                val rawMessage = primaryErr.localizedMessage ?: "Firebase App Check attestation failed."
                throw IOException(enrichAppCheckErrorIfNeeded(rawMessage))
            }
            try {
                executeSingleModel(models.fallbackModel, models.systemDirective, request)
            } catch (e: CancellationException) {
                throw e
            } catch (fallbackErr: Exception) {
                Log.e(TAG, "Firebase AI fallback model ${models.fallbackModel} failed", fallbackErr)
                if (fallbackErr is TimeoutCancellationException || primaryErr is TimeoutCancellationException) {
                    throw IOException("AI response timed out after 60 seconds. Please try again.")
                }
                val rawMessage = fallbackErr.localizedMessage ?: primaryErr.localizedMessage ?: "Firebase AI generation failed."
                val diagnostic = enrichAppCheckErrorIfNeeded(rawMessage)
                throw IOException(diagnostic)
            }
        }
    }

    private suspend fun enrichAppCheckErrorIfNeeded(rawMessage: String): String {
        val lower = rawMessage.lowercase()
        if (!lower.contains("app check") &&
            !lower.contains("appcheck") &&
            !lower.contains("attestation") &&
            !lower.contains("too many attempts") &&
            !lower.contains("recaptcha")
        ) {
            return rawMessage
        }

        if (BuildConfig.DEBUG && !AppCheckDiagnostics.hasLaunchDebugToken) {
            return "Debug APK App Check is not authorized on standalone devices. Install StudyBuddy-release.apk (not StudyBuddy-debug.apk)."
        }

        val status = AppCheckDiagnostics.statusSummary
        if (lower.contains("too many attempts")) {
            return formatAppCheckFailureMessage(rawMessage)
        }

        val now = System.currentTimeMillis()
        if (now - lastDiagnosticProbeMs < 30_000L) {
            val prior = AppCheckDiagnostics.lastUnderlyingTokenError
            return if (!prior.isNullOrBlank()) "$rawMessage [$status: $prior]" else "$rawMessage [$status]"
        }
        lastDiagnosticProbeMs = now

        val tokenError = try {
            withTimeout(8_000L) {
                suspendCancellableCoroutine<String?> { cont ->
                    FirebaseAppCheck.getInstance().getAppCheckToken(false)
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(null)
                        }
                        .addOnFailureListener { ex ->
                            if (cont.isActive) cont.resume(formatExceptionSummary(ex))
                        }
                }
            }
        } catch (t: TimeoutCancellationException) {
            "App Check token request timed out."
        } catch (t: Throwable) {
            formatExceptionSummary(t)
        }

        return if (!tokenError.isNullOrBlank()) {
            AppCheckDiagnostics.recordTokenFailure(tokenError)
            formatAppCheckFailureMessage(tokenError)
        } else {
            "$rawMessage [$status]"
        }
    }

    private suspend fun executeSingleModel(
        modelName: String,
        tierDirective: String,
        req: AiExecutionRequest
    ): String {
        val fullSystemInstruction = buildString {
            if (req.systemInstruction.isNotBlank()) {
                append(req.systemInstruction.trim())
                append("\n\n")
            }
            append(tierDirective)
        }.trim()

        val config = generationConfig {
            temperature = req.tier.temperature.toFloat()
            topP = 0.95f
            maxOutputTokens = req.tier.maxOutputTokens
            if (req.expectJsonResponse) {
                responseMimeType = "application/json"
            }
        }

        val generativeModel = Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = config,
            systemInstruction = if (fullSystemInstruction.isNotBlank()) {
                content { text(fullSystemInstruction) }
            } else {
                null
            }
        )

        val historyContents = req.conversationHistory
            .takeLast(req.tier.maxHistoryTurns)
            .filter { (_, histText) -> histText.isNotBlank() }
            .map { (role, histText) ->
                val normalizedRole = if (role == "user") "user" else "model"
                content(normalizedRole) {
                    text(histText)
                }
            }

        val userPromptContent = content("user") {
            text(req.prompt)

            if (!req.base64Image.isNullOrBlank()) {
                val imgBytes = Base64.decode(req.base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                if (bitmap != null) {
                    image(bitmap)
                } else {
                    inlineData(imgBytes, "image/jpeg")
                }
            }

            if (!req.base64Audio.isNullOrBlank()) {
                val audioBytes = Base64.decode(req.base64Audio, Base64.DEFAULT)
                inlineData(audioBytes, req.audioMimeType)
            }
        }

        val response = withTimeout(AI_TIMEOUT_MS) {
            if (historyContents.isNotEmpty() && req.base64Image.isNullOrBlank() && req.base64Audio.isNullOrBlank()) {
                val chat = generativeModel.startChat(history = historyContents)
                chat.sendMessage(userPromptContent)
            } else {
                generativeModel.generateContent(historyContents + userPromptContent)
            }
        }

        val extracted = response.text?.trim().orEmpty()
        if (extracted.isBlank()) {
            throw IOException("Empty response received from Firebase AI ($modelName).")
        }
        return extracted
    }
}
