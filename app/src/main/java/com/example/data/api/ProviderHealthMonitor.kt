package com.example.data.api

import kotlin.math.min

data class ProviderCircuitState(
    val provider: AiProviderType,
    val consecutiveFailures: Int = 0,
    val averageLatencyMs: Long = 0L,
    val lastSuccessMs: Long = 0L,
    val lastFailureMs: Long = 0L,
    val outageCooldownUntilMs: Long = 0L,
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0
) {
    fun isHealthy(nowMs: Long = System.currentTimeMillis()): Boolean = nowMs >= outageCooldownUntilMs

    fun successRatePercent(): Int {
        if (totalRequests <= 0) return 100
        return ((successfulRequests.toDouble() / totalRequests.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }
}

/**
 * Monitors provider-level health, latency, temporary outages, and circuit-breaker recovery.
 */
object ProviderHealthMonitor {
    private val lock = Any()
    private val providerStates = mutableMapOf<AiProviderType, ProviderCircuitState>()

    fun recordSuccess(
        provider: AiProviderType,
        latencyMs: Long,
        nowMs: Long = System.currentTimeMillis()
    ) {
        synchronized(lock) {
            val prev = providerStates[provider] ?: ProviderCircuitState(provider)
            val newAvgLatency = if (prev.averageLatencyMs > 0L) {
                (prev.averageLatencyMs * 2 + latencyMs) / 3
            } else {
                latencyMs
            }
            providerStates[provider] = prev.copy(
                consecutiveFailures = 0,
                averageLatencyMs = newAvgLatency,
                lastSuccessMs = nowMs,
                outageCooldownUntilMs = 0L,
                totalRequests = prev.totalRequests + 1,
                successfulRequests = prev.successfulRequests + 1
            )
        }
    }

    fun recordFailure(
        provider: AiProviderType,
        isOutageOrServerError: Boolean,
        nowMs: Long = System.currentTimeMillis()
    ) {
        synchronized(lock) {
            val prev = providerStates[provider] ?: ProviderCircuitState(provider)
            val failures = prev.consecutiveFailures + 1
            // Apply bounded exponential provider cooldown only after repeated server outages
            val cooldownMs = if (isOutageOrServerError && failures >= 2) {
                min(120_000L, 10_000L * (1L shl min(3, failures - 2)))
            } else {
                0L
            }
            providerStates[provider] = prev.copy(
                consecutiveFailures = failures,
                lastFailureMs = nowMs,
                outageCooldownUntilMs = if (cooldownMs > 0L) nowMs + cooldownMs else prev.outageCooldownUntilMs,
                totalRequests = prev.totalRequests + 1
            )
        }
    }

    fun isProviderAvailable(provider: AiProviderType, nowMs: Long = System.currentTimeMillis()): Boolean {
        synchronized(lock) {
            val state = providerStates[provider] ?: return true
            if (nowMs >= state.outageCooldownUntilMs && state.outageCooldownUntilMs > 0L) {
                providerStates[provider] = state.copy(outageCooldownUntilMs = 0L, consecutiveFailures = 0)
                return true
            }
            return state.isHealthy(nowMs)
        }
    }

    fun getProviderState(provider: AiProviderType): ProviderCircuitState {
        synchronized(lock) {
            return providerStates[provider] ?: ProviderCircuitState(provider)
        }
    }

    fun resetForTesting() {
        synchronized(lock) {
            providerStates.clear()
        }
    }
}
