package com.example.data.api

import kotlin.math.max

/**
 * Tracks per-key usage against rolling per-minute (RPM) and per-day (RPD) windows,
 * HTTP 429 cooldowns, and exhausted states.
 */
data class KeyQuotaSnapshot(
    val keyId: String,
    val rpmLimit: Int,
    val rpdLimit: Int,
    val requestsLastMinute: Int = 0,
    val requestsToday: Int = 0,
    val minuteWindowStartMs: Long = 0L,
    val dayWindowStartMs: Long = 0L,
    val cooldownUntilMs: Long = 0L,
    val isExhausted: Boolean = false
) {
    fun remainingMinuteQuota(nowMs: Long = System.currentTimeMillis()): Int {
        val activeMinuteCount = if (nowMs - minuteWindowStartMs >= 60_000L) 0 else requestsLastMinute
        return max(0, rpmLimit - activeMinuteCount)
    }

    fun remainingDailyQuota(nowMs: Long = System.currentTimeMillis()): Int {
        val activeDayCount = if (nowMs - dayWindowStartMs >= 86_400_000L) 0 else requestsToday
        return max(0, rpdLimit - activeDayCount)
    }

    fun hasQuotaAvailable(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (nowMs < cooldownUntilMs) return false
        if (isExhausted && (nowMs - dayWindowStartMs) < 86_400_000L) return false
        return remainingMinuteQuota(nowMs) > 0 && remainingDailyQuota(nowMs) > 0
    }
}

object QuotaMonitor {
    private val lock = Any()
    private val quotaMap = mutableMapOf<String, KeyQuotaSnapshot>()

    private const val ONE_MINUTE_MS = 60_000L
    private const val ONE_DAY_MS = 86_400_000L

    fun defaultRpmLimit(provider: AiProviderType): Int = when (provider) {
        AiProviderType.GEMINI -> 15
        AiProviderType.GROQ -> 30
        AiProviderType.OPENROUTER -> 20
        AiProviderType.CUSTOM_OPENAI -> 60
    }

    fun defaultRpdLimit(provider: AiProviderType): Int = when (provider) {
        AiProviderType.GEMINI -> 1500
        AiProviderType.GROQ -> 14400
        AiProviderType.OPENROUTER -> 1000
        AiProviderType.CUSTOM_OPENAI -> 5000
    }

    fun ensureTracked(keyId: String, provider: AiProviderType, nowMs: Long = System.currentTimeMillis()): KeyQuotaSnapshot {
        synchronized(lock) {
            return quotaMap.getOrPut(keyId) {
                KeyQuotaSnapshot(
                    keyId = keyId,
                    rpmLimit = defaultRpmLimit(provider),
                    rpdLimit = defaultRpdLimit(provider),
                    minuteWindowStartMs = nowMs,
                    dayWindowStartMs = nowMs
                )
            }
        }
    }

    fun recordRequestAttempt(keyId: String, provider: AiProviderType, nowMs: Long = System.currentTimeMillis()): KeyQuotaSnapshot {
        synchronized(lock) {
            val existing = ensureTracked(keyId, provider, nowMs)
            val minuteExpired = nowMs - existing.minuteWindowStartMs >= ONE_MINUTE_MS
            val dayExpired = nowMs - existing.dayWindowStartMs >= ONE_DAY_MS

            val updated = existing.copy(
                requestsLastMinute = if (minuteExpired) 1 else existing.requestsLastMinute + 1,
                minuteWindowStartMs = if (minuteExpired) nowMs else existing.minuteWindowStartMs,
                requestsToday = if (dayExpired) 1 else existing.requestsToday + 1,
                dayWindowStartMs = if (dayExpired) nowMs else existing.dayWindowStartMs,
                isExhausted = if (dayExpired) false else existing.isExhausted
            )
            quotaMap[keyId] = updated
            return updated
        }
    }

    fun markRateLimited(
        keyId: String,
        provider: AiProviderType,
        cooldownMs: Long,
        markExhaustedForDay: Boolean = false,
        nowMs: Long = System.currentTimeMillis()
    ): KeyQuotaSnapshot {
        synchronized(lock) {
            val existing = ensureTracked(keyId, provider, nowMs)
            val updated = existing.copy(
                cooldownUntilMs = max(existing.cooldownUntilMs, nowMs + cooldownMs),
                isExhausted = existing.isExhausted || markExhaustedForDay
            )
            quotaMap[keyId] = updated
            return updated
        }
    }

    fun clearCooldownIfRecovered(keyId: String, nowMs: Long = System.currentTimeMillis()): KeyQuotaSnapshot? {
        synchronized(lock) {
            val existing = quotaMap[keyId] ?: return null
            val dayExpired = nowMs - existing.dayWindowStartMs >= ONE_DAY_MS
            val minuteExpired = nowMs - existing.minuteWindowStartMs >= ONE_MINUTE_MS
            val updated = existing.copy(
                requestsLastMinute = if (minuteExpired) 0 else existing.requestsLastMinute,
                minuteWindowStartMs = if (minuteExpired) nowMs else existing.minuteWindowStartMs,
                requestsToday = if (dayExpired) 0 else existing.requestsToday,
                dayWindowStartMs = if (dayExpired) nowMs else existing.dayWindowStartMs,
                cooldownUntilMs = if (nowMs >= existing.cooldownUntilMs) 0L else existing.cooldownUntilMs,
                isExhausted = if (dayExpired) false else existing.isExhausted
            )
            quotaMap[keyId] = updated
            return updated
        }
    }

    fun canUseKey(keyId: String, provider: AiProviderType, nowMs: Long = System.currentTimeMillis()): Boolean {
        synchronized(lock) {
            val snapshot = clearCooldownIfRecovered(keyId, nowMs) ?: ensureTracked(keyId, provider, nowMs)
            return snapshot.hasQuotaAvailable(nowMs)
        }
    }

    fun getSnapshot(keyId: String): KeyQuotaSnapshot? {
        synchronized(lock) {
            return quotaMap[keyId]
        }
    }

    fun resetForTesting() {
        synchronized(lock) {
            quotaMap.clear()
        }
    }
}
