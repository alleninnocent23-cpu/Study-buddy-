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
        get() {
            val trimmed = key.trim()
            return when {
                trimmed.length <= 8 -> "••••••••"
                trimmed.startsWith("AIzaSy") -> "AIzaSy••••" + trimmed.takeLast(4)
                trimmed.startsWith("sk-or-") -> "sk-or-••••" + trimmed.takeLast(4)
                trimmed.startsWith("gsk_") -> "gsk_••••" + trimmed.takeLast(4)
                else -> trimmed.take(4) + "••••••••" + trimmed.takeLast(4)
            }
        }

    fun isAvailableNow(): Boolean {
        if (!isEnabled) return false
        if (status == KeyHealthStatus.INVALID) return false
        val now = System.currentTimeMillis()
        return now >= cooldownUntilTimestamp
    }

    fun cooldownRemainingSeconds(): Int {
        val now = System.currentTimeMillis()
        return if (cooldownUntilTimestamp > now) {
            ((cooldownUntilTimestamp - now) / 1000).toInt()
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
 * PRODUCTION-GRADE ADVANCED AI API MANAGEMENT & AUTOMATIC FAILOVER SYSTEM
 *
 * Capabilities:
 * - Multi-key management from the same provider (e.g. Gemini Primary, Gemini Secondary)
 * - Multi-provider intelligent failover (Gemini -> OpenRouter -> Groq -> Custom)
 * - Automatic detection of HTTP 429 (Rate Limit), 401/403 (Invalid Key), 408/500/502/503/504 (Server issues)
 * - Exponential cooldown backoff to avoid hammering exhausted keys
 * - Live Health & Metrics monitoring (Dashboard)
 * - Thread-safe request dispatching with seamless retries
 * - Strict security: full keys are never exposed in UI or logs
 */
object AiApiManager {
    private const val TAG = "AiApiManager"
    private const val PREFS_NAME = "studybuddy_ai_api_manager_prefs"
    private const val KEY_CONFIGURED_KEYS_JSON = "configured_keys_json"
    private const val KEY_ACTIVE_PROVIDER = "active_provider"

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

    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (sharedPreferences != null) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences = prefs

        val loadedKeys = loadKeysFromStorage(prefs)
        val initialList = if (loadedKeys.isNotEmpty()) {
            loadedKeys
        } else {
            // Seed with BuildConfig or environment key if available
            val defaultBuildKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
            val validKey = if (defaultBuildKey.isNotBlank() && defaultBuildKey != "MY_GEMINI_API_KEY") {
                defaultBuildKey
            } else ""

            listOf(
                ApiKeyEntry(
                    id = "gemini-primary-built-in",
                    provider = AiProviderType.GEMINI,
                    key = validKey,
                    label = "Gemini Primary (AI Studio Default)",
                    priority = 1,
                    isEnabled = true
                )
            )
        }

        _keysList.value = initialList
        _activeKeyId.value = initialList.firstOrNull { it.isAvailableNow() }?.id ?: initialList.firstOrNull()?.id
    }

    private fun loadKeysFromStorage(prefs: SharedPreferences): List<ApiKeyEntry> {
        val json = prefs.getString(KEY_CONFIGURED_KEYS_JSON, null) ?: return emptyList()
        val list = mutableListOf<ApiKeyEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val provider = try {
                    AiProviderType.valueOf(obj.getString("provider"))
                } catch (e: Exception) {
                    AiProviderType.GEMINI
                }
                list.add(
                    ApiKeyEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        provider = provider,
                        key = obj.getString("key"),
                        label = obj.optString("label", "${provider.displayName} Key"),
                        priority = obj.optInt("priority", 1),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        totalRequests = obj.optInt("totalRequests", 0),
                        successfulRequests = obj.optInt("successfulRequests", 0),
                        failedRequests = obj.optInt("failedRequests", 0),
                        rateLimit429Errors = obj.optInt("rateLimit429Errors", 0),
                        lastSuccessTimestamp = obj.optLong("lastSuccessTimestamp", 0L),
                        averageLatencyMs = obj.optLong("averageLatencyMs", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load keys from storage", e)
        }
        return list
    }

    private fun saveKeysToStorage() {
        val prefs = sharedPreferences ?: return
        try {
            val array = JSONArray()
            _keysList.value.forEach { entry ->
                array.put(JSONObject().apply {
                    put("id", entry.id)
                    put("provider", entry.provider.name)
                    put("key", entry.key)
                    put("label", entry.label)
                    put("priority", entry.priority)
                    put("isEnabled", entry.isEnabled)
                    put("totalRequests", entry.totalRequests)
                    put("successfulRequests", entry.successfulRequests)
                    put("failedRequests", entry.failedRequests)
                    put("rateLimit429Errors", entry.rateLimit429Errors)
                    put("lastSuccessTimestamp", entry.lastSuccessTimestamp)
                    put("averageLatencyMs", entry.averageLatencyMs)
                })
            }
            prefs.edit().putString(KEY_CONFIGURED_KEYS_JSON, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save keys to storage", e)
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
     * Retrieves health and usage summary aggregated per provider for the Admin Dashboard.
     */
    fun getProviderSummaries(): List<ProviderHealthSummary> {
        return AiProviderType.values().map { provider ->
            val providerKeys = _keysList.value.filter { it.provider == provider }
            val activeKeys = providerKeys.filter { it.isAvailableNow() }
            val totalReqs = providerKeys.sumOf { it.totalRequests }
            val failedReqs = providerKeys.sumOf { it.failedRequests }
            val rateLimits = providerKeys.sumOf { it.rateLimit429Errors }
            val successReqs = providerKeys.sumOf { it.successfulRequests }
            val healthPercent = if (totalReqs > 0) {
                ((successReqs.toDouble() / totalReqs.toDouble()) * 100).toInt()
            } else 100

            val latestSuccess = providerKeys.maxOfOrNull { it.lastSuccessTimestamp } ?: 0L
            val activeKey = providerKeys.firstOrNull { it.isAvailableNow() }?.maskedKey

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
     * Executes an AI API call with automatic multi-key failover and provider switching.
     * If Key A hits 429 or 5xx, it immediately fails over to Key B and retries the request seamlessly.
     */
    suspend fun <T> executeWithFailover(
        operationName: String = "generateContent",
        block: suspend (keyEntry: ApiKeyEntry) -> Result<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        var attempts = 0
        val maxAttempts = maxOf(1, _keysList.value.size * 2)
        var lastException: Throwable? = null

        while (attempts < maxAttempts) {
            val keyEntry = getNextAvailableKey()
            if (keyEntry == null) {
                val errorMsg = "All configured AI keys are currently rate-limited or exhausted. Please check the AI API Management dashboard."
                _lastFailoverEvent.value = errorMsg
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }

            _activeKeyId.value = keyEntry.id
            val startTime = System.currentTimeMillis()

            try {
                recordRequestStart(keyEntry.id)
                val result = block(keyEntry)
                val duration = System.currentTimeMillis() - startTime

                if (result.isSuccess) {
                    recordRequestSuccess(keyEntry.id, duration)
                    return@withContext result
                } else {
                    val error = result.exceptionOrNull()
                    lastException = error
                    val failoverHandled = handleExecutionFailure(keyEntry.id, error)
                    if (!failoverHandled) {
                        // User input error or non-recoverable error, return without useless retries
                        return@withContext result
                    }
                }
            } catch (e: Exception) {
                lastException = e
                handleExecutionFailure(keyEntry.id, e)
            }

            attempts++
        }

        Result.failure(lastException ?: IOException("Failed to obtain AI response after failover attempts across all keys."))
    }

    private suspend fun getNextAvailableKey(): ApiKeyEntry? = mutex.withLock {
        val now = System.currentTimeMillis()
        val allKeys = _keysList.value

        // 1. First priority: enabled keys of lowest priority number that have passed cooldown and are not invalid
        val candidates = allKeys
            .filter { it.isEnabled && it.status != KeyHealthStatus.INVALID }
            .sortedWith(compareBy({ it.priority }, { it.cooldownUntilTimestamp }))

        val ready = candidates.firstOrNull { it.isAvailableNow() }
        if (ready != null) return ready

        // 2. If all keys in cooldown, pick the one whose cooldown expires soonest
        val soonest = candidates.minByOrNull { it.cooldownUntilTimestamp }
        if (soonest != null && (soonest.cooldownUntilTimestamp - now) <= 3000) {
            // If cooldown expires within 3s, wait for it
            kotlinx.coroutines.delay(maxOf(0L, soonest.cooldownUntilTimestamp - now))
            return soonest
        }

        return null
    }

    private suspend fun recordRequestStart(keyId: String) = mutex.withLock {
        _keysList.value = _keysList.value.map {
            if (it.id == keyId) it.copy(totalRequests = it.totalRequests + 1) else it
        }
    }

    private suspend fun recordRequestSuccess(keyId: String, latencyMs: Long) = mutex.withLock {
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
                    cooldownUntilTimestamp = 0L
                )
            } else it
        }
        saveKeysToStorage()
    }

    /**
     * Determines failure cause and triggers intelligent key failover.
     * Returns true if request should fail over and retry with the next key.
     */
    private suspend fun handleExecutionFailure(keyId: String, error: Throwable?): Boolean = mutex.withLock {
        val errorMsg = error?.message ?: "Unknown API error"
        val now = System.currentTimeMillis()

        val isRateLimit429 = errorMsg.contains("429") || errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true)
        val isInvalidKey401 = errorMsg.contains("401") || errorMsg.contains("403") || errorMsg.contains("API_KEY_INVALID", ignoreCase = true)
        val isServerOverload = errorMsg.contains("500") || errorMsg.contains("502") || errorMsg.contains("503") || errorMsg.contains("504") || errorMsg.contains("timeout", ignoreCase = true)

        var shouldRetryNextKey = false

        _keysList.value = _keysList.value.map { entry ->
            if (entry.id == keyId) {
                val newFailed = entry.failedRequests + 1
                when {
                    isRateLimit429 -> {
                        shouldRetryNextKey = true
                        val newRateLimits = entry.rateLimit429Errors + 1
                        val cooldownSec = (60 * (1 + (newRateLimits % 3))).toLong() // 60s, 120s, 180s
                        val cooldownUntil = now + (cooldownSec * 1000)
                        _lastFailoverEvent.value = "Failover: Key '${entry.label}' hit rate-limit 429. Cooling down for ${cooldownSec}s. Switching key..."
                        entry.copy(
                            failedRequests = newFailed,
                            rateLimit429Errors = newRateLimits,
                            status = KeyHealthStatus.RATE_LIMITED,
                            cooldownUntilTimestamp = cooldownUntil,
                            lastErrorTimestamp = now,
                            lastErrorMessage = "HTTP 429: Rate limit exceeded"
                        )
                    }
                    isInvalidKey401 -> {
                        shouldRetryNextKey = true
                        _lastFailoverEvent.value = "Failover: Key '${entry.label}' returned 401/403 unauthorized. Marked invalid. Switching key..."
                        entry.copy(
                            failedRequests = newFailed,
                            status = KeyHealthStatus.INVALID,
                            lastErrorTimestamp = now,
                            lastErrorMessage = "HTTP 401/403: Invalid credentials"
                        )
                    }
                    isServerOverload -> {
                        shouldRetryNextKey = true
                        val cooldownUntil = now + (30 * 1000) // 30s temporary backoff
                        _lastFailoverEvent.value = "Failover: Provider server error on '${entry.label}'. Switching to next backup key..."
                        entry.copy(
                            failedRequests = newFailed,
                            status = KeyHealthStatus.TEMPORARY_ERROR,
                            cooldownUntilTimestamp = cooldownUntil,
                            lastErrorTimestamp = now,
                            lastErrorMessage = errorMsg.take(80)
                        )
                    }
                    else -> {
                        // General network error or formatting issue
                        shouldRetryNextKey = error is IOException
                        entry.copy(
                            failedRequests = newFailed,
                            lastErrorTimestamp = now,
                            lastErrorMessage = errorMsg.take(80)
                        )
                    }
                }
            } else entry
        }

        saveKeysToStorage()
        return shouldRetryNextKey
    }

    /**
     * Live test validation for a specific API key.
     */
    suspend fun testKeyValidation(keyEntry: ApiKeyEntry): Result<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            when (keyEntry.provider) {
                AiProviderType.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${keyEntry.key}"
                    val bodyJson = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", "ping") })
                                })
                            })
                        })
                    }
                    val request = Request.Builder()
                        .url(url)
                        .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val latency = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        recordRequestSuccess(keyEntry.id, latency)
                        Result.success("Success: Gemini API key validated in ${latency}ms")
                    } else {
                        val code = response.code
                        val errorStr = "HTTP $code: ${response.message}"
                        handleExecutionFailure(keyEntry.id, IOException(errorStr))
                        Result.failure(IOException(errorStr))
                    }
                }
                AiProviderType.OPENROUTER, AiProviderType.GROQ, AiProviderType.CUSTOM_OPENAI -> {
                    val endpoint = keyEntry.provider.defaultBaseUrl
                    val bodyJson = JSONObject().apply {
                        put("model", keyEntry.provider.defaultModel)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "ping")
                            })
                        })
                        put("max_tokens", 5)
                    }
                    val request = Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer ${keyEntry.key}")
                        .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val latency = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        recordRequestSuccess(keyEntry.id, latency)
                        Result.success("Success: ${keyEntry.provider.displayName} key validated in ${latency}ms")
                    } else {
                        val code = response.code
                        val errorStr = "HTTP $code: ${response.message}"
                        handleExecutionFailure(keyEntry.id, IOException(errorStr))
                        Result.failure(IOException(errorStr))
                    }
                }
            }
        } catch (e: Exception) {
            handleExecutionFailure(keyEntry.id, e)
            Result.failure(e)
        }
    }
}
