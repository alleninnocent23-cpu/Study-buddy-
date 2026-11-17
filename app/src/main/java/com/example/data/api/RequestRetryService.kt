package com.example.data.api

import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

enum class RetryableErrorKind {
    RATE_LIMIT_429,
    QUOTA_EXHAUSTED,
    INVALID_KEY_401_403,
    SERVER_OUTAGE_5XX,
    TRANSIENT_NETWORK,
    NON_RETRYABLE_CLIENT_ERROR
}

data class RetryClassification(
    val kind: RetryableErrorKind,
    val shouldRetryWithNextKey: Boolean,
    val backoffDelayMs: Long,
    val keyCooldownMs: Long,
    val safeMessage: String
)

/**
 * Handles bounded request retries with jittered exponential backoff and HTTP status classification.
 * Never executes infinite retry loops and strips any accidental API keys from error messages.
 */
object RequestRetryService {

    const val DEFAULT_MAX_ATTEMPTS = 5
    private const val BASE_BACKOFF_MS = 250L
    private const val MAX_BACKOFF_MS = 2_500L

    fun sanitizeErrorMessage(raw: String?, sensitiveKeys: Collection<String> = emptyList()): String {
        var msg = raw?.trim().orEmpty()
        if (msg.isBlank()) return "Temporary AI service error."
        for (secret in sensitiveKeys) {
            if (secret.length > 6 && msg.contains(secret)) {
                msg = msg.replace(secret, APIKeyManager.maskKey(secret))
            }
        }
        // Strip accidental ?key=... query parameter values
        msg = msg.replace(Regex("([?&]key=)[^&\\s]+", RegexOption.IGNORE_CASE), "$1***")
        msg = msg.replace(Regex("(Bearer\\s+)[A-Za-z0-9._-]+", RegexOption.IGNORE_CASE), "$1***")
        return msg.take(220)
    }

    fun classifyFailure(
        httpCode: Int? = null,
        error: Throwable? = null,
        attemptIndex: Int = 0,
        retryAfterSeconds: Long? = null,
        sensitiveKeys: Collection<String> = emptyList(),
        isFirebaseAppCheckRoute: Boolean = false
    ): RetryClassification {
        val rawMsg = error?.message.orEmpty()
        val lower = rawMsg.lowercase()
        val safeMsg = sanitizeErrorMessage(rawMsg, sensitiveKeys)

        val effectiveCode = httpCode ?: extractHttpCode(rawMsg)

        val kind = when {
            effectiveCode == 429 || lower.contains("429") || lower.contains("rate limit") -> {
                if (lower.contains("daily") || lower.contains("billing") || lower.contains("exceeded your current quota")) {
                    RetryableErrorKind.QUOTA_EXHAUSTED
                } else {
                    RetryableErrorKind.RATE_LIMIT_429
                }
            }
            lower.contains("resource_exhausted") || lower.contains("quota") -> RetryableErrorKind.QUOTA_EXHAUSTED
            effectiveCode == 401 ||
                effectiveCode == 403 ||
                lower.contains("app check") ||
                lower.contains("appcheck") ||
                lower.contains("attestation") ||
                lower.contains("too many attempts") ||
                lower.contains("recaptcha") ||
                lower.contains("api_key_invalid") ||
                lower.contains("api key not valid") ||
                lower.contains("unauthorized") ->
                RetryableErrorKind.INVALID_KEY_401_403
            effectiveCode in listOf(408, 500, 502, 503, 504) ||
                lower.contains("500") || lower.contains("502") || lower.contains("503") || lower.contains("504") ||
                lower.contains("overloaded") || lower.contains("temporarily unavailable") ->
                RetryableErrorKind.SERVER_OUTAGE_5XX
            error is IOException || lower.contains("timeout") || lower.contains("unable to resolve host") || lower.contains("connection") ->
                RetryableErrorKind.TRANSIENT_NETWORK
            else -> RetryableErrorKind.NON_RETRYABLE_CLIENT_ERROR
        }

        val backoff = computeBackoffMs(attemptIndex, retryAfterSeconds)
        return when (kind) {
            RetryableErrorKind.RATE_LIMIT_429 -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = true,
                backoffDelayMs = backoff,
                keyCooldownMs = maxOf(60_000L, (retryAfterSeconds ?: 60L) * 1000L),
                safeMessage = "HTTP 429: Rate limit reached, rotating provider/key."
            )
            RetryableErrorKind.QUOTA_EXHAUSTED -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = true,
                backoffDelayMs = backoff,
                keyCooldownMs = 300_000L,
                safeMessage = "Provider quota reached, failing over to next available key."
            )
            RetryableErrorKind.INVALID_KEY_401_403 -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = true,
                backoffDelayMs = 100L,
                keyCooldownMs = if (isFirebaseAppCheckRoute) 5_000L else 3_600_000L,
                safeMessage = if (isFirebaseAppCheckRoute) {
                    safeMsg.ifBlank { "Firebase AI Logic authorization check failed." }
                } else {
                    "Configured key rejected by provider; failing over."
                }
            )
            RetryableErrorKind.SERVER_OUTAGE_5XX -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = true,
                backoffDelayMs = backoff,
                keyCooldownMs = 30_000L,
                safeMessage = "Provider temporarily busy; switching to backup route."
            )
            RetryableErrorKind.TRANSIENT_NETWORK -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = true,
                backoffDelayMs = backoff,
                keyCooldownMs = 5_000L,
                safeMessage = safeMsg.ifBlank { "Network interruption; retrying request." }
            )
            RetryableErrorKind.NON_RETRYABLE_CLIENT_ERROR -> RetryClassification(
                kind = kind,
                shouldRetryWithNextKey = false,
                backoffDelayMs = 0L,
                keyCooldownMs = 0L,
                safeMessage = safeMsg
            )
        }
    }

    fun computeBackoffMs(attemptIndex: Int, retryAfterSeconds: Long? = null): Long {
        if (retryAfterSeconds != null && retryAfterSeconds in 1..2) {
            return retryAfterSeconds * 1000L
        }
        val exp = BASE_BACKOFF_MS * (1L shl min(3, attemptIndex.coerceAtLeast(0)))
        val jitter = Random.nextLong(50L, 200L)
        return min(MAX_BACKOFF_MS, exp + jitter)
    }

    suspend fun waitBeforeRetry(delayMs: Long) {
        if (delayMs > 0L) {
            delay(delayMs)
        }
    }

    private fun extractHttpCode(message: String): Int? {
        val match = Regex("\\bHTTP\\s+(\\d{3})\\b", RegexOption.IGNORE_CASE).find(message) ?: return null
        return match.groupValues[1].toIntOrNull()
    }
}
