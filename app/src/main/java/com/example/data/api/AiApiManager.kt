package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class AiProviderType(val displayName: String, val defaultBaseUrl: String, val defaultModel: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta/models/", "gemini-2.5-flash"),
    OPENROUTER("OpenRouter AI", "https://openrouter.ai/api/v1/chat/completions", "google/gemini-2.5-flash"),
    GROQ("Groq Fast Inference", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile"),
    CUSTOM_OPENAI("Custom AI Provider", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini")
}

enum class KeyHealthStatus(val label: String) {
    HEALTHY("Active & Healthy"),
    RATE_LIMITED("Rate Limited (Cooling down)"),
    EXHAUSTED("Quota Exhausted"),
    INVALID("Invalid / Unauthorized"),
    TEMPORARY_ERROR("Server Error")
}

data class ApiKeyEntry(
    val id: String = UUID.randomUUID().toString(),
    val provider: AiProviderType,
    val key: String,
    val label: String,
    val priority: Int = 1, // 1 = highest
    val isEnabled: Boolean = true,
    val status: KeyHealthStatus = KeyHealthStatus.HEALTHY,
    val cooldownUntilTimestamp: Long = 0L,
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0,
    val rateLimit429Errors: Int = 0,
    val lastSuccessTimestamp: Long = 0L,
    val lastErrorTimestamp: Long = 0L,
    val lastErrorMessage: String? = null,
    val averageLatencyMs: Long = 0L
) {
    val maskedKey: String
        get() = APIKeyManager.maskKey(key)

    fun isAvailableNow(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isEnabled) return false
        if (key.isBlank()) return false
        if (status == KeyHealthStatus.INVALID) return false
        return nowMs >= cooldownUntilTimestamp
    }

    fun cooldownRemainingSeconds(nowMs: Long = System.currentTimeMillis()): Int {
        return if (cooldownUntilTimestamp > nowMs) {
            ((cooldownUntilTimestamp - nowMs) / 1000).toInt()
        } else 0
    }
}

data class ProviderHealthSummary(
    val provider: AiProviderType,
    val totalKeys: Int,
    val activeKeysCount: Int,
    val totalRequests: Int,
    val failedRequests: Int,
    val rateLimitErrors: Int,
    val healthPercent: Int,
    val lastSuccessTimestamp: Long,
    val activeKeyMasked: String?
)

/**
 * Centralized AI API key rotation, quota tracking, health monitoring, and automatic failover coordinator.
 */
object AiApiManager {
    private const val TAG = "AiApiManager"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mutex = Mutex()

    private val _keysList = MutableStateFlow<List<ApiKeyEntry>>(emptyList())
    val keysList: StateFlow<List<ApiKeyEntry>> = _keysList.asStateFlow()

    private val _activeKeyId = MutableStateFlow<String?>(null)
    val activeKeyId: StateFlow<String?> = _activeKeyId.asStateFlow()

    private val _lastFailoverEvent = MutableStateFlow<String?>(null)
    val lastFailoverEvent: StateFlow<String?> = _lastFailoverEvent.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext != null && _keysList.value.isNotEmpty()) return
        appContext = context.applicationContext ?: context
        val loaded = APIKeyManager.initialize(appContext!!)
        val combined = attachFirebaseAiLogicIfAvailable(loaded)
        combined.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
        _keysList.value = combined
        _activeKeyId.value = combined.firstOrNull { it.isAvailableNow() }?.id ?: combined.firstOrNull()?.id
    }

    private fun attachFirebaseAiLogicIfAvailable(existing: List<ApiKeyEntry>): List<ApiKeyEntry> {
        if (!FirebaseAiLogicService.isAvailable(appContext)) return existing
        if (existing.any { it.id == FirebaseAiLogicService.VIRTUAL_KEY_ID }) return existing

        val hasRealGeminiKey = existing.any { it.provider == AiProviderType.GEMINI && it.key.isNotBlank() }
        val firebaseEntry = ApiKeyEntry(
            id = FirebaseAiLogicService.VIRTUAL_KEY_ID,
            provider = AiProviderType.GEMINI,
            key = FirebaseAiLogicService.VIRTUAL_KEY_SENTINEL,
            label = "StudyBuddy AI (Firebase App Check)",
            priority = if (hasRealGeminiKey) 2 else 1,
            isEnabled = true
        )
        return (existing + firebaseEntry).sortedBy { it.priority }
    }

    private fun saveKeysToStorage() {
        APIKeyManager.saveKeys(appContext, _keysList.value)
    }

    fun addApiKey(provider: AiProviderType, key: String, label: String, priority: Int = 1): Boolean {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return false
        val newEntry = ApiKeyEntry(
            provider = provider,
            key = trimmed,
            label = label.ifBlank { "${provider.displayName} Key" },
            priority = priority
        )
        QuotaMonitor.ensureTracked(newEntry.id, newEntry.provider)
        _keysList.value = (_keysList.value + newEntry).sortedBy { it.priority }
        saveKeysToStorage()
        return true
    }

    fun removeApiKey(id: String) {
        _keysList.value = _keysList.value.filter { it.id != id }
        if (_activeKeyId.value == id) {
            _activeKeyId.value = _keysList.value.firstOrNull { it.isAvailableNow() }?.id
        }
        saveKeysToStorage()
    }

    fun toggleKeyEnabled(id: String, enabled: Boolean) {
        _keysList.value = _keysList.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        saveKeysToStorage()
    }

    fun updateKeyPriority(id: String, priority: Int) {
        _keysList.value = _keysList.value.map {
            if (it.id == id) it.copy(priority = priority) else it
        }.sortedBy { it.priority }
        saveKeysToStorage()
    }

    /**
     * Retrieves health and usage summary aggregated per provider.
     */
    fun getProviderSummaries(): List<ProviderHealthSummary> {
        val now = System.currentTimeMillis()
        return AiProviderType.entries.map { provider ->
            val providerKeys = _keysList.value.filter { it.provider == provider }
            val activeKeys = providerKeys.filter {
                it.isAvailableNow(now) && QuotaMonitor.canUseKey(it.id, it.provider, now)
            }
            val totalReqs = providerKeys.sumOf { it.totalRequests }
            val failedReqs = providerKeys.sumOf { it.failedRequests }
            val rateLimits = providerKeys.sumOf { it.rateLimit429Errors }
            val successReqs = providerKeys.sumOf { it.successfulRequests }
            val healthPercent = if (totalReqs > 0) {
                ((successReqs.toDouble() / totalReqs.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else {
                ProviderHealthMonitor.getProviderState(provider).successRatePercent()
            }

            val latestSuccess = providerKeys.maxOfOrNull { it.lastSuccessTimestamp } ?: 0L
            val activeKey = activeKeys.firstOrNull()?.maskedKey

            ProviderHealthSummary(
                provider = provider,
                totalKeys = providerKeys.size,
                activeKeysCount = activeKeys.size,
                totalRequests = totalReqs,
                failedRequests = failedReqs,
                rateLimitErrors = rateLimits,
                healthPercent = healthPercent,
                lastSuccessTimestamp = latestSuccess,
                activeKeyMasked = activeKey
            )
        }
    }

    /**
     * Executes an AI API call with capability-aware key rotation, quota tracking,
     * finite bounded retries, and jittered exponential backoff.
     */
    suspend fun <T> executeWithFailover(
        operationName: String = "generateContent",
        tier: StudyBuddyModelTier = StudyBuddyModelTier.FREE,
        capability: AiTaskCapability = AiTaskCapability.TEXT_CHAT,
        block: suspend (keyEntry: ApiKeyEntry) -> Result<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        ensureSeededFromBuildConfigIfEmpty()

        val allConfiguredKeys = _keysList.value
        val maxAttempts = minOf(
            RequestRetryService.DEFAULT_MAX_ATTEMPTS,
            maxOf(1, allConfiguredKeys.count { it.isEnabled && it.key.isNotBlank() } * 2)
        )

        val triedKeyIds = mutableSetOf<String>()
        var attempts = 0
        var lastError: Throwable? = null
        val sensitiveKeys = allConfiguredKeys.mapNotNull { it.key.takeIf { k -> k.isNotBlank() } }

        while (attempts < maxAttempts) {
            val keyEntry = getNextAvailableKey(
                tier = tier,
                capability = capability,
                excludedKeyIds = triedKeyIds
            ) ?: if (triedKeyIds.isNotEmpty()) {
                // Allow a second pass only if a transient key's backoff has recovered and we still have attempts
                getNextAvailableKey(tier = tier, capability = capability, excludedKeyIds = emptySet())
            } else {
                null
            }

            if (keyEntry == null) {
                val hasAnyNonEmptyKey = allConfiguredKeys.any { it.isEnabled && it.key.isNotBlank() }
                val errorMsg = if (!hasAnyNonEmptyKey) {
                    "AI service credentials are not configured for this build."
                } else {
                    "AI service is temporarily busy or rate-limited. Please try again shortly."
                }
                _lastFailoverEvent.value = errorMsg
                return@withContext Result.failure(lastError ?: IllegalStateException(errorMsg))
            }

            triedKeyIds.add(keyEntry.id)
            _activeKeyId.value = keyEntry.id
            val startTime = System.currentTimeMillis()

            try {
                recordRequestStart(keyEntry.id, keyEntry.provider)
                val result = block(keyEntry)
                val duration = System.currentTimeMillis() - startTime

                if (result.isSuccess) {
                    recordRequestSuccess(keyEntry.id, keyEntry.provider, duration)
                    return@withContext result
                } else {
                    val error = result.exceptionOrNull()
                    val isVirtualRoute = FirebaseAiLogicService.isVirtualKey(keyEntry)
                    val classification = RequestRetryService.classifyFailure(
                        httpCode = (error as? ProviderHttpException)?.httpCode,
                        error = error,
                        attemptIndex = attempts,
                        retryAfterSeconds = (error as? ProviderHttpException)?.retryAfterSeconds,
                        sensitiveKeys = sensitiveKeys,
                        isFirebaseAppCheckRoute = isVirtualRoute
                    )
                    lastError = IllegalStateException(classification.safeMessage)
                    handleExecutionFailure(keyEntry.id, keyEntry.provider, classification)
                    val hasUntriedBackupKey = allConfiguredKeys.any {
                        it.id !in triedKeyIds && it.isEnabled && it.key.isNotBlank() && it.isAvailableNow()
                    }
                    if (!classification.shouldRetryWithNextKey ||
                        (isVirtualRoute && classification.kind == RetryableErrorKind.INVALID_KEY_401_403 && !hasUntriedBackupKey)
                    ) {
                        return@withContext Result.failure(lastError!!)
                    }
                    RequestRetryService.waitBeforeRetry(classification.backoffDelayMs)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                val isVirtualRoute = FirebaseAiLogicService.isVirtualKey(keyEntry)
                val classification = RequestRetryService.classifyFailure(
                    httpCode = (e as? ProviderHttpException)?.httpCode,
                    error = e,
                    attemptIndex = attempts,
                    retryAfterSeconds = (e as? ProviderHttpException)?.retryAfterSeconds,
                    sensitiveKeys = sensitiveKeys,
                    isFirebaseAppCheckRoute = isVirtualRoute
                )
                lastError = IllegalStateException(classification.safeMessage)
                handleExecutionFailure(keyEntry.id, keyEntry.provider, classification)
                val hasUntriedBackupKey = allConfiguredKeys.any {
                    it.id !in triedKeyIds && it.isEnabled && it.key.isNotBlank() && it.isAvailableNow()
                }
                if (!classification.shouldRetryWithNextKey ||
                    (isVirtualRoute && classification.kind == RetryableErrorKind.INVALID_KEY_401_403 && !hasUntriedBackupKey)
                ) {
                    return@withContext Result.failure(lastError!!)
                }
                RequestRetryService.waitBeforeRetry(classification.backoffDelayMs)
            }

            attempts++
        }

        val finalSafeMessage = RequestRetryService.sanitizeErrorMessage(lastError?.message, sensitiveKeys)
        Result.failure(IOException(finalSafeMessage.ifBlank { "Unable to reach AI providers after $attempts attempts." }))
    }

    private suspend fun ensureSeededFromBuildConfigIfEmpty() = mutex.withLock {
        val current = _keysList.value
        val buildKey = APIKeyManager.getBuildConfigGeminiKey()
        val runtimeKey = GeminiClient.customApiKey?.trim().orEmpty()
        val effective = runtimeKey.ifBlank { buildKey }

        var updated = if (current.none { it.key.isNotBlank() && !FirebaseAiLogicService.isVirtualKey(it) } && effective.isNotBlank()) {
            val seeded = ApiKeyEntry(
                id = "gemini-primary-built-in",
                provider = AiProviderType.GEMINI,
                key = effective,
                label = "Gemini Primary",
                priority = 1,
                isEnabled = true
            )
            QuotaMonitor.ensureTracked(seeded.id, seeded.provider)
            current + seeded
        } else {
            current
        }

        updated = attachFirebaseAiLogicIfAvailable(updated)
        if (updated != current) {
            updated.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
            _keysList.value = updated
        }
    }

    private suspend fun getNextAvailableKey(
        tier: StudyBuddyModelTier,
        capability: AiTaskCapability,
        excludedKeyIds: Set<String>
    ): ApiKeyEntry? = mutex.withLock {
        val now = System.currentTimeMillis()
        // Automatically recover any keys whose cooldown has elapsed
        var recoveredAny = false
        val refreshed = _keysList.value.map { entry ->
            QuotaMonitor.clearCooldownIfRecovered(entry.id, now)
            if (entry.cooldownUntilTimestamp in 1..now && entry.status != KeyHealthStatus.INVALID) {
                recoveredAny = true
                entry.copy(status = KeyHealthStatus.HEALTHY, cooldownUntilTimestamp = 0L)
            } else {
                entry
            }
        }
        if (recoveredAny) {
            _keysList.value = refreshed
        }

        return APIKeyRotationService.pickNextCandidate(
            allKeys = _keysList.value,
            tier = tier,
            capability = capability,
            excludedKeyIds = excludedKeyIds,
            nowMs = now
        )
    }

    private suspend fun recordRequestStart(keyId: String, provider: AiProviderType) = mutex.withLock {
        QuotaMonitor.recordRequestAttempt(keyId, provider)
        _keysList.value = _keysList.value.map {
            if (it.id == keyId) it.copy(totalRequests = it.totalRequests + 1) else it
        }
    }

    private suspend fun recordRequestSuccess(keyId: String, provider: AiProviderType, latencyMs: Long) = mutex.withLock {
        ProviderHealthMonitor.recordSuccess(provider, latencyMs)
        _keysList.value = _keysList.value.map {
            if (it.id == keyId) {
                val newSuccess = it.successfulRequests + 1
                val newAvgLatency = if (it.averageLatencyMs > 0) {
                    (it.averageLatencyMs + latencyMs) / 2
                } else latencyMs
                it.copy(
                    successfulRequests = newSuccess,
                    status = KeyHealthStatus.HEALTHY,
                    lastSuccessTimestamp = System.currentTimeMillis(),
                    averageLatencyMs = newAvgLatency,
                    cooldownUntilTimestamp = 0L,
                    lastErrorMessage = null
                )
            } else it
        }
        saveKeysToStorage()
    }

    private suspend fun handleExecutionFailure(
        keyId: String,
        provider: AiProviderType,
        classification: RetryClassification
    ) = mutex.withLock {
        val now = System.currentTimeMillis()
        val isOutage = classification.kind == RetryableErrorKind.SERVER_OUTAGE_5XX ||
            classification.kind == RetryableErrorKind.TRANSIENT_NETWORK
        ProviderHealthMonitor.recordFailure(provider, isOutage, now)

        if (classification.kind == RetryableErrorKind.RATE_LIMIT_429 ||
            classification.kind == RetryableErrorKind.QUOTA_EXHAUSTED
        ) {
            QuotaMonitor.markRateLimited(
                keyId = keyId,
                provider = provider,
                cooldownMs = classification.keyCooldownMs,
                markExhaustedForDay = classification.kind == RetryableErrorKind.QUOTA_EXHAUSTED,
                nowMs = now
            )
        }

        _lastFailoverEvent.value = classification.safeMessage
        _keysList.value = _keysList.value.map { entry ->
            if (entry.id != keyId) entry
            else {
                val newFailed = entry.failedRequests + 1
                when (classification.kind) {
                    RetryableErrorKind.RATE_LIMIT_429 -> entry.copy(
                        failedRequests = newFailed,
                        rateLimit429Errors = entry.rateLimit429Errors + 1,
                        status = KeyHealthStatus.RATE_LIMITED,
                        cooldownUntilTimestamp = now + classification.keyCooldownMs,
                        lastErrorTimestamp = now,
                        lastErrorMessage = classification.safeMessage
                    )
                    RetryableErrorKind.QUOTA_EXHAUSTED -> entry.copy(
                        failedRequests = newFailed,
                        rateLimit429Errors = entry.rateLimit429Errors + 1,
                        status = KeyHealthStatus.EXHAUSTED,
                        cooldownUntilTimestamp = now + classification.keyCooldownMs,
                        lastErrorTimestamp = now,
                        lastErrorMessage = classification.safeMessage
                    )
                    RetryableErrorKind.INVALID_KEY_401_403 -> if (FirebaseAiLogicService.isVirtualKey(entry)) {
                        entry.copy(
                            failedRequests = newFailed,
                            status = KeyHealthStatus.TEMPORARY_ERROR,
                            cooldownUntilTimestamp = now + 5_000L,
                            lastErrorTimestamp = now,
                            lastErrorMessage = classification.safeMessage
                        )
                    } else {
                        entry.copy(
                            failedRequests = newFailed,
                            status = KeyHealthStatus.INVALID,
                            lastErrorTimestamp = now,
                            lastErrorMessage = classification.safeMessage
                        )
                    }
                    RetryableErrorKind.SERVER_OUTAGE_5XX,
                    RetryableErrorKind.TRANSIENT_NETWORK -> entry.copy(
                        failedRequests = newFailed,
                        status = KeyHealthStatus.TEMPORARY_ERROR,
                        cooldownUntilTimestamp = now + classification.keyCooldownMs,
                        lastErrorTimestamp = now,
                        lastErrorMessage = classification.safeMessage
                    )
                    RetryableErrorKind.NON_RETRYABLE_CLIENT_ERROR -> entry.copy(
                        failedRequests = newFailed,
                        lastErrorTimestamp = now,
                        lastErrorMessage = classification.safeMessage
                    )
                }
            }
        }
        saveKeysToStorage()
    }

    /**
     * Live test validation for an internal/configured API key.
     */
    suspend fun testKeyValidation(keyEntry: ApiKeyEntry): Result<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val output = AIProviderManager.executeWithKey(
                keyEntry = keyEntry,
                request = AiExecutionRequest(
                    prompt = "Reply with OK.",
                    capability = AiTaskCapability.TEXT_CHAT,
                    tier = StudyBuddyModelTier.FREE
                )
            )
            val latency = System.currentTimeMillis() - startTime
            if (output.isNotBlank()) {
                recordRequestSuccess(keyEntry.id, keyEntry.provider, latency)
                Result.success("Success: ${keyEntry.provider.displayName} validated in ${latency}ms")
            } else {
                Result.failure(IOException("Empty validation response."))
            }
        } catch (e: Exception) {
            val classification = RequestRetryService.classifyFailure(
                httpCode = (e as? ProviderHttpException)?.httpCode,
                error = e,
                sensitiveKeys = listOf(keyEntry.key)
            )
            handleExecutionFailure(keyEntry.id, keyEntry.provider, classification)
            Log.w(TAG, "Validation failed for ${keyEntry.label}: ${classification.safeMessage}")
            Result.failure(IOException(classification.safeMessage))
        }
    }

    internal fun resetForTesting(seedKeys: List<ApiKeyEntry> = emptyList()) {
        QuotaMonitor.resetForTesting()
        ProviderHealthMonitor.resetForTesting()
        seedKeys.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
        _keysList.value = seedKeys
        _activeKeyId.value = seedKeys.firstOrNull()?.id
        _lastFailoverEvent.value = null
    }
}
