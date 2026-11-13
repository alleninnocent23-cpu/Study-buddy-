package com.example.data.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AiExecutionRequest(
    val prompt: String,
    val systemInstruction: String = "",
    val conversationHistory: List<Pair<String, String>> = emptyList(),
    val capability: AiTaskCapability = AiTaskCapability.TEXT_CHAT,
    val tier: StudyBuddyModelTier = StudyBuddyModelTier.FREE,
    val base64Image: String? = null,
    val base64Audio: String? = null,
    val audioMimeType: String = "audio/mp4",
    val expectJsonResponse: Boolean = false
)

class ProviderHttpException(
    val httpCode: Int,
    val retryAfterSeconds: Long? = null,
    message: String
) : IOException(message)

/**
 * Unified HTTP execution layer across Google Gemini, OpenRouter, Groq, and Custom OpenAI endpoints.
 * Respects resolved model configurations, multimodal attachments, and structured JSON output flags.
 */
object AIProviderManager {
    private const val TAG = "AIProviderManager"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(75, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun executeWithKey(
        keyEntry: ApiKeyEntry,
        request: AiExecutionRequest
    ): String {
        if (FirebaseAiLogicService.isVirtualKey(keyEntry)) {
            return FirebaseAiLogicService.execute(request)
        }

        val resolved = ModelSelectionService.resolveForProvider(
            tier = request.tier,
            provider = keyEntry.provider,
            capability = request.capability
        )

        return try {
            executeSingleModel(keyEntry, resolved, resolved.modelId, request)
        } catch (e: ProviderHttpException) {
            val fallbackModel = resolved.fallbackModelId
            // If primary tier model (e.g. gemini-2.5-pro) is unavailable/rate-limited (404/429),
            // attempt the tier's intra-provider fallback model cleanly once before rotating keys.
            if (fallbackModel != null && (e.httpCode == 404 || e.httpCode == 429 || e.httpCode == 503)) {
                Log.w(TAG, "Primary model ${resolved.modelId} returned HTTP ${e.httpCode}; trying fallback $fallbackModel")
                executeSingleModel(keyEntry, resolved, fallbackModel, request)
            } else {
                throw e
            }
        }
    }

    private fun executeSingleModel(
        keyEntry: ApiKeyEntry,
        config: ResolvedModelConfig,
        modelId: String,
        req: AiExecutionRequest
    ): String {
        return when (keyEntry.provider) {
            AiProviderType.GEMINI -> executeGemini(keyEntry.key, config, modelId, req)
            AiProviderType.OPENROUTER,
            AiProviderType.GROQ,
            AiProviderType.CUSTOM_OPENAI -> executeOpenAiCompatible(keyEntry, config, modelId, req)
        }
    }

    private fun executeGemini(
        apiKey: String,
        config: ResolvedModelConfig,
        modelId: String,
        req: AiExecutionRequest
    ): String {
        val fullSystemInstruction = buildString {
            if (req.systemInstruction.isNotBlank()) {
                append(req.systemInstruction.trim())
                append("\n\n")
            }
            append(config.systemPromptDirective)
        }.trim()

        val bodyJson = JSONObject().apply {
            if (fullSystemInstruction.isNotBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", fullSystemInstruction) })
                    })
                })
            }

            val contents = JSONArray()
            req.conversationHistory.takeLast(config.maxHistoryTurns).forEach { (role, histText) ->
                if (histText.isNotBlank()) {
                    contents.put(JSONObject().apply {
                        put("role", if (role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", histText) })
                        })
                    })
                }
            }

            val currentParts = JSONArray().apply {
                put(JSONObject().apply { put("text", req.prompt) })
                if (!req.base64Image.isNullOrBlank()) {
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", req.base64Image)
                        })
                    })
                }
                if (!req.base64Audio.isNullOrBlank()) {
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", req.audioMimeType)
                            put("data", req.base64Audio)
                        })
                    })
                }
            }

            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", currentParts)
            })
            put("contents", contents)

            put("generationConfig", JSONObject().apply {
                put("temperature", config.temperature)
                put("topP", 0.95)
                put("maxOutputTokens", config.maxOutputTokens)
                if (req.expectJsonResponse) {
                    put("responseMimeType", "application/json")
                }
                if (config.thinkingBudgetTokens > 0 && modelId.contains("2.5") && !req.expectJsonResponse) {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingBudget", config.thinkingBudgetTokens)
                    })
                }
            })
        }

        val endpoint = "${AiProviderType.GEMINI.defaultBaseUrl}$modelId:generateContent"
        val httpRequest = Request.Builder()
            .url(endpoint)
            .addHeader("x-goog-api-key", apiKey)
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(bodyString, response.code, response.message)
                val retryAfter = response.header("Retry-After")?.toLongOrNull()
                throw ProviderHttpException(response.code, retryAfter, "HTTP ${response.code}: $errorMsg")
            }

            val jsonResponse = JSONObject(bodyString)
            val candidates = jsonResponse.getJSONArray("candidates")
            val parts = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (!part.optBoolean("thought", false) && part.has("text")) {
                    textBuilder.append(part.getString("text"))
                }
            }
            val extracted = textBuilder.toString().trim()
            if (extracted.isBlank()) {
                throw IOException("Empty response received from Gemini.")
            }
            return extracted
        }
    }

    private fun executeOpenAiCompatible(
        keyEntry: ApiKeyEntry,
        config: ResolvedModelConfig,
        modelId: String,
        req: AiExecutionRequest
    ): String {
        val fullSystemInstruction = buildString {
            if (req.systemInstruction.isNotBlank()) {
                append(req.systemInstruction.trim())
                append("\n\n")
            }
            append(config.systemPromptDirective)
        }.trim()

        val messages = JSONArray().apply {
            if (fullSystemInstruction.isNotBlank()) {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", fullSystemInstruction)
                })
            }
            req.conversationHistory.takeLast(config.maxHistoryTurns).forEach { (role, histText) ->
                if (histText.isNotBlank()) {
                    put(JSONObject().apply {
                        put("role", if (role == "user") "user" else "assistant")
                        put("content", histText)
                    })
                }
            }
            if (!req.base64Image.isNullOrBlank()) {
                val multimodalContent = JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", req.prompt)
                    })
                    put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", JSONObject().apply {
                            put("url", "data:image/jpeg;base64,${req.base64Image}")
                        })
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", multimodalContent)
                })
            } else {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", req.prompt)
                })
            }
        }

        val reqJson = JSONObject().apply {
            put("model", modelId)
            put("messages", messages)
            put("temperature", config.temperature)
            put("max_tokens", config.maxOutputTokens)
        }

        val builder = Request.Builder()
            .url(keyEntry.provider.defaultBaseUrl)
            .addHeader("Authorization", "Bearer ${keyEntry.key}")
            .post(reqJson.toString().toRequestBody("application/json".toMediaType()))

        if (keyEntry.provider == AiProviderType.OPENROUTER) {
            builder.addHeader("X-Title", "StudyBuddy Android")
        }

        httpClient.newCall(builder.build()).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(bodyString, response.code, response.message)
                val retryAfter = response.header("Retry-After")?.toLongOrNull()
                throw ProviderHttpException(response.code, retryAfter, "HTTP ${response.code}: $errorMsg")
            }

            val json = JSONObject(bodyString)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            if (content.isBlank()) {
                throw IOException("Empty response received from ${keyEntry.provider.displayName}.")
            }
            return content
        }
    }

    private fun parseErrorMessage(body: String, code: Int, fallbackMessage: String): String {
        return try {
            val obj = JSONObject(body)
            obj.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: obj.optString("message").takeIf { it.isNotBlank() }
                ?: "HTTP $code: $fallbackMessage"
        } catch (_: Exception) {
            "HTTP $code: $fallbackMessage"
        }
    }
}
