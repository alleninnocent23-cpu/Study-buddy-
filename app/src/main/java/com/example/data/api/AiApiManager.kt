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

enum class ProviderConnectionStatus(val displayLabel: String) {
    NOT_CONFIGURED("Not Configured"),
    SAVED_UNVERIFIED("Saved (Not Tested)"),
    TESTING("Testing..."),
    CONNECTED("Connected"),
    INVALID_KEY("Invalid Key"),
    QUOTA_EXCEEDED("Quota Exceeded"),
    CONNECTION_FAILED("Connection Failed")
}

data class ProviderApiUiState(
    val provider: AiProviderType,
    val hasSavedKey: Boolean = false,
    val maskedKey: String = "(unconfigured)",
    val connectionStatus: ProviderConnectionStatus = ProviderConnectionStatus.NOT_CONFIGURED,
    val statusDetail: String? = null
)

/**
 * Centralized AI API key rotation, quota tracking, health monitoring, and automatic failover coordinator.
 */
object AiApiManager {
    private const val TAG = "AiApiManager"
    const val SETTINGS_GEMINI_KEY_ID = "settings-gemini-key"
    const val SETTINGS_GROQ_KEY_ID = "settings-groq-key"

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

    private val _geminiUiState = MutableStateFlow(ProviderApiUiState(AiProviderType.GEMINI))
    val geminiUiState: StateFlow<ProviderApiUiState> = _geminiUiState.asStateFlow()

    private val _groqUiState = MutableStateFlow(ProviderApiUiState(AiProviderType.GROQ))
    val groqUiState: StateFlow<ProviderApiUiState> = _groqUiState.asStateFlow()

    private var appContext: Context? = null
    private var activeOwnerId: String? = null

    fun initialize(context: Context, ownerId: String? = activeOwnerId) {
        val resolvedContext = context.applicationContext ?: context
        appContext = resolvedContext
        val normalizedOwner = ownerId?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val ownerChanged = normalizedOwner != activeOwnerId
        if (ownerChanged) {
            activeOwnerId = normalizedOwner
            QuotaMonitor.resetForTesting()
            ProviderHealthMonitor.resetForTesting()
        }
        if (_keysList.value.isNotEmpty() && !ownerChanged) {
            val withFirebase = attachFirebaseAiLogicIfAvailable(_keysList.value)
            if (withFirebase != _keysList.value) {
                withFirebase.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
                _keysList.value = withFirebase
                _activeKeyId.value = withFirebase.firstOrNull { it.isAvailableNow() }?.id ?: withFirebase.firstOrNull()?.id
            }
            syncProviderUiStates()
            return
        }
        val loaded = APIKeyManager.initialize(resolvedContext, activeOwnerId)
        val combined = attachFirebaseAiLogicIfAvailable(loaded)
        combined.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
        _keysList.value = combined
        _activeKeyId.value = combined.firstOrNull { it.isAvailableNow() }?.id ?: combined.firstOrNull()?.id
        syncProviderUiStates()
    }

    fun setActiveOwner(context: Context, ownerId: String?) {
        val resolvedContext = context.applicationContext ?: context
        val normalized = ownerId?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        if (appContext != null && normalized == activeOwnerId && _keysList.value.isNotEmpty()) {
            return
        }
        _keysList.value = emptyList()
        _activeKeyId.value = null
        _lastFailoverEvent.value = null
        activeOwnerId = normalized
        initialize(resolvedContext, normalized)
    }

    private fun hasUserConfiguredKeys(entries: List<ApiKeyEntry>): Boolean {
        return entries.any { !FirebaseAiLogicService.isVirtualKey(it) && it.isEnabled && it.key.isNotBlank() }
    }

    private fun attachFirebaseAiLogicIfAvailable(existing: List<ApiKeyEntry>): List<ApiKeyEntry> {
        // When user has configured their own Gemini or Groq API keys, route through their keys (1. Gemini -> 2. Groq).
        // Otherwise fall back to Firebase AI Logic App Check seamlessly.
        if (hasUserConfiguredKeys(existing)) {
            return existing.filterNot { it.id == FirebaseAiLogicService.VIRTUAL_KEY_ID }
        }
        if (!FirebaseAiLogicService.isAvailable(appContext)) return existing
        if (existing.any { it.id == FirebaseAiLogicService.VIRTUAL_KEY_ID }) return existing

        val firebaseEntry = ApiKeyEntry(
            id = FirebaseAiLogicService.VIRTUAL_KEY_ID,
            provider = AiProviderType.GEMINI,
            key = FirebaseAiLogicService.VIRTUAL_KEY_SENTINEL,
            label = "StudyBuddy AI (Firebase App Check)",
            priority = 10,
            isEnabled = true
        )
        return (existing + firebaseEntry).sortedBy { it.priority }
    }

    private fun saveKeysToStorage() {
        APIKeyManager.saveKeys(appContext, _keysList.value, activeOwnerId)
    }

    private fun settingsKeyIdFor(provider: AiProviderType): String = when (provider) {
        AiProviderType.GEMINI -> SETTINGS_GEMINI_KEY_ID
        AiProviderType.GROQ -> SETTINGS_GROQ_KEY_ID
        else -> UUID.randomUUID().toString()
    }

    private fun defaultPriorityFor(provider: AiProviderType): Int = when (provider) {
        AiProviderType.GEMINI -> 1
        AiProviderType.GROQ -> 2
        AiProviderType.OPENROUTER -> 3
        AiProviderType.CUSTOM_OPENAI -> 4
    }

    fun saveProviderKey(provider: AiProviderType, rawKey: String): Boolean {
        val trimmed = rawKey.trim()
        if (trimmed.isEmpty()) return false
        val targetId = settingsKeyIdFor(provider)
        val priority = defaultPriorityFor(provider)
        val label = when (provider) {
            AiProviderType.GEMINI -> "Google Gemini API Key"
            AiProviderType.GROQ -> "Groq API Key"
            else -> "${provider.displayName} Key"
        }
        val newEntry = ApiKeyEntry(
            id = targetId,
            provider = provider,
            key = trimmed,
            label = label,
            priority = priority,
            isEnabled = true,
            status = KeyHealthStatus.HEALTHY,
            cooldownUntilTimestamp = 0L
        )
        QuotaMonitor.resetForTesting()
        ProviderHealthMonitor.resetForTesting()
        val retained = _keysList.value.filterNot {
            FirebaseAiLogicService.isVirtualKey(it) || it.provider == provider
        }
        val updated = (retained + newEntry).sortedBy { it.priority }
        updated.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
        _keysList.value = updated
        _activeKeyId.value = updated.firstOrNull { it.isAvailableNow() }?.id ?: updated.firstOrNull()?.id
        _lastFailoverEvent.value = null
        saveKeysToStorage()
        updateProviderUiState(
            provider = provider,
            status = ProviderConnectionStatus.SAVED_UNVERIFIED,
            detail = "Key saved securely"
        )
        return true
    }

    fun removeProviderKey(provider: AiProviderType) {
        val withoutProvider = _keysList.value.filterNot {
            !FirebaseAiLogicService.isVirtualKey(it) && it.provider == provider
        }
        val combined = attachFirebaseAiLogicIfAvailable(withoutProvider)
        combined.forEach { QuotaMonitor.ensureTracked(it.id, it.provider) }
        _keysList.value = combined
        _activeKeyId.value = combined.firstOrNull { it.isAvailableNow() }?.id ?: combined.firstOrNull()?.id
        saveKeysToStorage()
        updateProviderUiState(
            provider = provider,
            status = ProviderConnectionStatus.NOT_CONFIGURED,
            detail = null
        )
    }

    fun getProviderUiState(provider: AiProviderType): ProviderApiUiState {
        return when (provider) {
            AiProviderType.GEMINI -> _geminiUiState.value
            AiProviderType.GROQ -> _groqUiState.value
            else -> buildProviderUiState(provider, ProviderConnectionStatus.NOT_CONFIGURED, null)
        }
    }

    private fun buildProviderUiState(
        provider: AiProviderType,
        overrideStatus: ProviderConnectionStatus? = null,
        detail: String? = null
    ): ProviderApiUiState {
        val entry = _keysList.value.firstOrNull {
            !FirebaseAiLogicService.isVirtualKey(it) && it.provider == provider && it.key.isNotBlank()
        }
        if (entry == null) {
            return ProviderApiUiState(
                provider = provider,
                hasSavedKey = false,
                maskedKey = "(unconfigured)",
                connectionStatus = if (overrideStatus == ProviderConnectionStatus.TESTING) {
                    ProviderConnectionStatus.TESTING
                } else {
                    overrideStatus ?: ProviderConnectionStatus.NOT_CONFIGURED
                },
                statusDetail = detail
            )
        }
        val inferredStatus = overrideStatus ?: when (entry.status) {
            KeyHealthStatus.HEALTHY -> if (entry.successfulRequests > 0) {
                ProviderConnectionStatus.CONNECTED
            } else {
                ProviderConnectionStatus.SAVED_UNVERIFIED
            }
            KeyHealthStatus.INVALID -> ProviderConnectionStatus.INVALID_KEY
            KeyHealthStatus.RATE_LIMITED,
            KeyHealthStatus.EXHAUSTED -> ProviderConnectionStatus.QUOTA_EXCEEDED
            KeyHealthStatus.TEMPORARY_ERROR -> ProviderConnectionStatus.CONNECTION_FAILED
        }
        return ProviderApiUiState(
            provider = provider,
            hasSavedKey = true,
            maskedKey = entry.maskedKey,
            connectionStatus = inferredStatus,
            statusDetail = detail ?: entry.lastErrorMessage
        )
    }

    private fun syncProviderUiStates() {
        _geminiUiState.value = buildProviderUiState(
            AiProviderType.GEMINI,
            overrideStatus = _geminiUiState.value.connectionStatus.takeIf {
                it == ProviderConnectionStatus.CONNECTED ||
                    it == ProviderConnectionStatus.INVALID_KEY ||
                    it == ProviderConnectionStatus.QUOTA_EXCEEDED ||
                    it == ProviderConnectionStatus.CONNECTION_FAILED
            }?.takeIf { statusMatchesEntry(AiProviderType.GEMINI, it) }
        )
        _groqUiState.value = buildProviderUiState(
            AiProviderType.GROQ,
            overrideStatus = _groqUiState.value.connectionStatus.takeIf {
                it == ProviderConnectionStatus.CONNECTED ||
                    it == ProviderConnectionStatus.INVALID_KEY ||
                    it == ProviderConnectionStatus.QUOTA_EXCEEDED ||
                    it == ProviderConnectionStatus.CONNECTION_FAILED
            }?.takeIf { statusMatchesEntry(AiProviderType.GROQ, it) }
        )
    }

    private fun statusMatchesEntry(provider: AiProviderType, status: ProviderConnectionStatus): Boolean {
        val entry = _keysList.value.firstOrNull {
            !FirebaseAiLogicService.isVirtualKey(it) && it.provider == provider
        } ?: return false
        return when (entry.status) {
            KeyHealthStatus.HEALTHY -> status == ProviderConnectionStatus.CONNECTED
            KeyHealthStatus.INVALID -> status == ProviderConnectionStatus.INVALID_KEY
            KeyHealthStatus.RATE_LIMITED, KeyHealthStatus.EXHAUSTED -> status == ProviderConnectionStatus.QUOTA_EXCEEDED
            KeyHealthStatus.TEMPORARY_ERROR -> status == ProviderConnectionStatus.CONNECTION_FAILED
        }
    }

    private fun updateProviderUiState(
        provider: AiProviderType,
        status: ProviderConnectionStatus,
        detail: String?
    ) {
        val state = buildProviderUiState(provider, status, detail)
        when (provider) {
            AiProviderType.GEMINI -> _geminiUiState.value = state
            AiProviderType.GROQ -> _groqUiState.value = state
            else -> {}
        }
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
        val combined = attachFirebaseAiLogicIfAvailable((_keysList.value + newEntry).sortedBy { it.priority })
        _keysList.value = combined
        saveKeysToStorage()
        syncProviderUiStates()
        return true
    }

    fun removeApiKey(id: String) {
        val remaining = _keysList.value.filter { it.id != id }
        _keysList.value = attachFirebaseAiLogicIfAvailable(remaining)
        if (_activeKeyId.value == id) {
            _activeKeyId.value = _keysList.value.firstOrNull { it.isAvailableNow() }?.id
        }
        saveKeysToStorage()
        syncProviderUiStates()
    }

    fun toggleKeyEnabled(id: String, enabled: Boolean) {
        _keysList.value = attachFirebaseAiLogicIfAvailable(
            _keysList.value.map {
                if (it.id == id) it.copy(isEnabled = enabled) else it
            }
        )
        saveKeysToStorage()
        syncProviderUiStates()
    }

    fun updateKeyPriority(id: String, priority: Int) {
        _keysList.value = _keysList.value.map {
            if (it.id == id) it.copy(priority = priority) else it
        }.sortedBy { it.priority }
        saveKeysToStorage()
        syncProviderUiStates()
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
        val activeCandidateCount = allConfiguredKeys.count {
            it.isEnabled && it.key.isNotBlank() && it.status != KeyHealthStatus.INVALID
        }
        val maxAttempts = maxOf(
            RequestRetryService.DEFAULT_MAX_ATTEMPTS,
            activeCandidateCount + 2
        ).coerceAtMost(12)

        val triedKeyIds = mutableSetOf<String>()
        val providerFailureSummaries = linkedMapOf<AiProviderType, String>()
        var attempts = 0
        var lastError: Throwable? = null
        val sensitiveKeys = allConfiguredKeys.mapNotNull { it.key.takeIf { k -> k.isNotBlank() } }

        while (attempts < maxAttempts) {
            val keyEntry = getNextAvailableKey(
                tier = tier,
                capability = capability,
                excludedKeyIds = triedKeyIds
            ) ?: if (triedKeyIds.isNotEmpty() && triedKeyIds.size < activeCandidateCount) {
                getNextAvailableKey(tier = tier, capability = capability, excludedKeyIds = emptySet())
            } else {
                null
            }

            if (keyEntry == null) {
                val hasAnyNonEmptyKey = allConfiguredKeys.any { it.isEnabled && it.key.isNotBlank() }
                val combinedFailure = providerFailureSummaries.values.joinToString(" | ")
                val errorMsg = when {
                    combinedFailure.isNotBlank() -> combinedFailure
                    !hasAnyNonEmptyKey -> "AI API keys are not configured. Add a Gemini or Groq key in Settings."
                    else -> "All configured AI providers are temporarily busy or rate-limited. Please try again shortly."
                }
                _lastFailoverEvent.value = errorMsg
                val finalErr = if (combinedFailure.isNotBlank()) {
                    IOException(combinedFailure)
                } else {
                    lastError ?: IllegalStateException(errorMsg)
                }
                return@withContext Result.failure(finalErr)
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
                    val cleanReason = RequestRetryService.sanitizeErrorMessage(error?.message ?: classification.safeMessage, sensitiveKeys)
                    providerFailureSummaries[keyEntry.provider] = "${keyEntry.provider.displayName}: ${cleanReason.ifBlank { classification.safeMessage }}"
                    handleExecutionFailure(keyEntry.id, keyEntry.provider, classification)
                    val hasUntriedBackupKey = allConfiguredKeys.any {
                        it.id !in triedKeyIds && it.isEnabled && it.key.isNotBlank() && it.isAvailableNow()
                    }
                    if (!classification.shouldRetryWithNextKey ||
                        (isVirtualRoute && classification.kind == RetryableErrorKind.INVALID_KEY_401_403 && !hasUntriedBackupKey)
                    ) {
                        return@withContext Result.failure(IOException(providerFailureSummaries.values.joinToString(" | ").ifBlank { classification.safeMessage }))
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
                val cleanReason = RequestRetryService.sanitizeErrorMessage(e.message ?: classification.safeMessage, sensitiveKeys)
                providerFailureSummaries[keyEntry.provider] = "${keyEntry.provider.displayName}: ${cleanReason.ifBlank { classification.safeMessage }}"
                handleExecutionFailure(keyEntry.id, keyEntry.provider, classification)
                val hasUntriedBackupKey = allConfiguredKeys.any {
                    it.id !in triedKeyIds && it.isEnabled && it.key.isNotBlank() && it.isAvailableNow()
                }
                if (!classification.shouldRetryWithNextKey ||
                    (isVirtualRoute && classification.kind == RetryableErrorKind.INVALID_KEY_401_403 && !hasUntriedBackupKey)
                ) {
                    return@withContext Result.failure(IOException(providerFailureSummaries.values.joinToString(" | ").ifBlank { classification.safeMessage }))
                }
                RequestRetryService.waitBeforeRetry(classification.backoffDelayMs)
            }

            attempts++
        }

        val combinedFailure = providerFailureSummaries.values.joinToString(" | ")
        val finalSafeMessage = RequestRetryService.sanitizeErrorMessage(
            combinedFailure.ifBlank { lastError?.message },
            sensitiveKeys
        )
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
            syncProviderUiStates()
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
        if (provider == AiProviderType.GEMINI || provider == AiProviderType.GROQ) {
            val savedEntry = _keysList.value.firstOrNull { !FirebaseAiLogicService.isVirtualKey(it) && it.provider == provider }
            if (savedEntry != null) {
                updateProviderUiState(provider, ProviderConnectionStatus.CONNECTED, "Connected (${latencyMs}ms)")
            }
        }
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
        if (provider == AiProviderType.GEMINI || provider == AiProviderType.GROQ) {
            val savedEntry = _keysList.value.firstOrNull { it.id == keyId && !FirebaseAiLogicService.isVirtualKey(it) }
            if (savedEntry != null) {
                val mappedStatus = mapClassificationToUiStatus(classification.kind)
                updateProviderUiState(provider, mappedStatus, classification.safeMessage)
            }
        }
    }

    internal fun mapClassificationToUiStatus(kind: RetryableErrorKind): ProviderConnectionStatus {
        return when (kind) {
            RetryableErrorKind.INVALID_KEY_401_403 -> ProviderConnectionStatus.INVALID_KEY
            RetryableErrorKind.RATE_LIMIT_429,
            RetryableErrorKind.QUOTA_EXHAUSTED -> ProviderConnectionStatus.QUOTA_EXCEEDED
            RetryableErrorKind.SERVER_OUTAGE_5XX,
            RetryableErrorKind.TRANSIENT_NETWORK,
            RetryableErrorKind.NON_RETRYABLE_CLIENT_ERROR -> ProviderConnectionStatus.CONNECTION_FAILED
        }
    }

    /**
     * Live connection test for Gemini or Groq (uses candidate key if provided, otherwise saved key).
     * Never logs or exposes raw keys.
     */
    suspend fun testProviderConnection(
        provider: AiProviderType,
        candidateRawKey: String? = null
    ): Result<ProviderConnectionStatus> = withContext(Dispatchers.IO) {
        val savedEntry = _keysList.value.firstOrNull {
            !FirebaseAiLogicService.isVirtualKey(it) && it.provider == provider
        }
        val effectiveRawKey = candidateRawKey?.trim()?.takeIf { it.isNotBlank() }
            ?: savedEntry?.key?.trim().orEmpty()

        if (effectiveRawKey.isBlank()) {
            updateProviderUiState(
                provider = provider,
                status = ProviderConnectionStatus.NOT_CONFIGURED,
                detail = "Enter an API key first."
            )
            return@withContext Result.failure(IOException("Enter a ${provider.displayName} API key first."))
        }

        val isTestingSavedEntry = savedEntry != null && savedEntry.key == effectiveRawKey
        val probeEntry = (savedEntry ?: ApiKeyEntry(
            id = settingsKeyIdFor(provider),
            provider = provider,
            key = effectiveRawKey,
            label = "${provider.displayName} API Key",
            priority = defaultPriorityFor(provider)
        )).copy(key = effectiveRawKey)

        updateProviderUiState(provider, ProviderConnectionStatus.TESTING, "Checking connection...")
        val startTime = System.currentTimeMillis()
        try {
            val output = AIProviderManager.executeWithKey(
                keyEntry = probeEntry,
                request = AiExecutionRequest(
                    prompt = "Reply with OK.",
                    capability = AiTaskCapability.TEXT_CHAT,
                    tier = StudyBuddyModelTier.FREE
                )
            )
            val latency = System.currentTimeMillis() - startTime
            if (output.isNotBlank()) {
                if (isTestingSavedEntry) {
                    recordRequestSuccess(probeEntry.id, provider, latency)
                }
                updateProviderUiState(
                    provider = provider,
                    status = ProviderConnectionStatus.CONNECTED,
                    detail = "Connected (${latency}ms)"
                )
                Result.success(ProviderConnectionStatus.CONNECTED)
            } else {
                updateProviderUiState(
                    provider = provider,
                    status = ProviderConnectionStatus.CONNECTION_FAILED,
                    detail = "Empty response from provider."
                )
                Result.failure(IOException("Empty validation response from ${provider.displayName}."))
            }
        } catch (e: Exception) {
            val classification = RequestRetryService.classifyFailure(
                httpCode = (e as? ProviderHttpException)?.httpCode,
                error = e,
                sensitiveKeys = listOf(effectiveRawKey),
                isFirebaseAppCheckRoute = false
            )
            if (isTestingSavedEntry) {
                handleExecutionFailure(probeEntry.id, provider, classification)
            }
            val uiStatus = mapClassificationToUiStatus(classification.kind)
            updateProviderUiState(provider, uiStatus, classification.safeMessage)
            Log.w(TAG, "Connection test failed for ${provider.displayName}: ${uiStatus.displayLabel}")
            Result.failure(IOException("${uiStatus.displayLabel}: ${classification.safeMessage}"))
        }
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
                sensitiveKeys = listOf(keyEntry.key),
                isFirebaseAppCheckRoute = FirebaseAiLogicService.isVirtualKey(keyEntry)
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
        syncProviderUiStates()
    }
}
