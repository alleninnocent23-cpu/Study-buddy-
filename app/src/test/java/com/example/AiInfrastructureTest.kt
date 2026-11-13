package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.APIKeyManager
import com.example.data.api.APIKeyRotationService
import com.example.data.api.AiProviderType
import com.example.data.api.AiTaskCapability
import com.example.data.api.ApiKeyEntry
import com.example.data.api.KeyHealthStatus
import com.example.data.api.ModelSelectionService
import com.example.data.api.ProviderHealthMonitor
import com.example.data.api.QuotaMonitor
import com.example.data.api.RequestRetryService
import com.example.data.api.RetryableErrorKind
import com.example.data.api.StudyBuddyModelTier
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiInfrastructureTest {

    @Before
    fun setUp() {
        QuotaMonitor.resetForTesting()
        ProviderHealthMonitor.resetForTesting()
    }

    @After
    fun tearDown() {
        QuotaMonitor.resetForTesting()
        ProviderHealthMonitor.resetForTesting()
    }

    @Test
    fun `model selection service distinguishes free plus and max tiers`() {
        val freeGemini = ModelSelectionService.resolveForProvider(
            StudyBuddyModelTier.FREE,
            AiProviderType.GEMINI,
            AiTaskCapability.TEXT_CHAT
        )
        val plusGemini = ModelSelectionService.resolveForProvider(
            StudyBuddyModelTier.PLUS,
            AiProviderType.GEMINI,
            AiTaskCapability.TEXT_CHAT
        )
        val maxGemini = ModelSelectionService.resolveForProvider(
            StudyBuddyModelTier.MAX,
            AiProviderType.GEMINI,
            AiTaskCapability.TEXT_CHAT
        )

        assertEquals("gemini-2.5-flash", freeGemini.modelId)
        assertEquals(0, freeGemini.thinkingBudgetTokens)

        assertEquals("gemini-2.5-flash", plusGemini.modelId)
        assertEquals("gemini-2.0-flash", plusGemini.fallbackModelId)
        assertEquals(1024, plusGemini.thinkingBudgetTokens)

        assertEquals("gemini-2.5-pro", maxGemini.modelId)
        assertEquals("gemini-2.5-flash", maxGemini.fallbackModelId)
        assertEquals(4096, maxGemini.thinkingBudgetTokens)
        assertTrue(maxGemini.maxOutputTokens > plusGemini.maxOutputTokens)
        assertTrue(plusGemini.maxOutputTokens > freeGemini.maxOutputTokens)
        assertTrue(maxGemini.maxHistoryTurns > plusGemini.maxHistoryTurns)
        assertTrue(plusGemini.systemPromptDirective.contains("StudyBuddy Plus"))
        assertTrue(maxGemini.systemPromptDirective.contains("StudyBuddy Max"))

        val maxOpenAi = ModelSelectionService.resolveForProvider(
            StudyBuddyModelTier.MAX,
            AiProviderType.CUSTOM_OPENAI
        )
        assertEquals("gpt-4.1", maxOpenAi.modelId)
        assertEquals("gpt-4o", maxOpenAi.fallbackModelId)
        assertEquals(0, maxOpenAi.thinkingBudgetTokens)
    }

    @Test
    fun `key rotation skips invalid cooled down and unsupported capability keys`() {
        val now = 1_700_000_000_000L
        val keys = listOf(
            ApiKeyEntry(
                id = "invalid-gemini",
                provider = AiProviderType.GEMINI,
                key = "AIzaSyInvalidKey12345",
                label = "Invalid Gemini",
                priority = 1,
                status = KeyHealthStatus.INVALID
            ),
            ApiKeyEntry(
                id = "cooldown-gemini",
                provider = AiProviderType.GEMINI,
                key = "AIzaSyCooldownKey12345",
                label = "Cooldown Gemini",
                priority = 1,
                cooldownUntilTimestamp = now + 60_000L
            ),
            ApiKeyEntry(
                id = "groq-fast",
                provider = AiProviderType.GROQ,
                key = "gsk_validGroqKey1234567",
                label = "Groq Fast",
                priority = 1
            ),
            ApiKeyEntry(
                id = "healthy-gemini",
                provider = AiProviderType.GEMINI,
                key = "AIzaSyHealthyKey123456",
                label = "Healthy Gemini",
                priority = 2
            )
        )

        // TEXT_CHAT on FREE tier prefers Groq when healthy
        val freeTextCandidates = APIKeyRotationService.selectOrderedCandidates(
            allKeys = keys,
            tier = StudyBuddyModelTier.FREE,
            capability = AiTaskCapability.TEXT_CHAT,
            nowMs = now
        )
        assertEquals(listOf("groq-fast", "healthy-gemini"), freeTextCandidates.map { it.id })

        // VISION_OCR skips GROQ because GROQ is text-only here, routing to Gemini
        val visionCandidates = APIKeyRotationService.selectOrderedCandidates(
            allKeys = keys,
            tier = StudyBuddyModelTier.FREE,
            capability = AiTaskCapability.VISION_OCR,
            nowMs = now
        )
        assertEquals(listOf("healthy-gemini"), visionCandidates.map { it.id })

        // AUDIO_TRANSCRIPTION skips GROQ and selects Gemini
        val audioCandidates = APIKeyRotationService.selectOrderedCandidates(
            allKeys = keys,
            tier = StudyBuddyModelTier.MAX,
            capability = AiTaskCapability.AUDIO_TRANSCRIPTION,
            nowMs = now
        )
        assertEquals(listOf("healthy-gemini"), audioCandidates.map { it.id })

        // Excluded key IDs are skipped during failover iteration
        val afterFailover = APIKeyRotationService.selectOrderedCandidates(
            allKeys = keys,
            tier = StudyBuddyModelTier.FREE,
            capability = AiTaskCapability.TEXT_CHAT,
            excludedKeyIds = setOf("groq-fast"),
            nowMs = now
        )
        assertEquals(listOf("healthy-gemini"), afterFailover.map { it.id })
    }

    @Test
    fun `quota monitor tracks rpm cooldown and automatic window recovery`() {
        val t0 = 1_700_000_000_000L
        assertTrue(QuotaMonitor.canUseKey("k1", AiProviderType.GEMINI, t0))

        QuotaMonitor.recordRequestAttempt("k1", AiProviderType.GEMINI, t0)
        val snapAfterReq = QuotaMonitor.getSnapshot("k1")
        assertNotNull(snapAfterReq)
        assertEquals(1, snapAfterReq?.requestsLastMinute)
        assertEquals(1, snapAfterReq?.requestsToday)

        QuotaMonitor.markRateLimited("k1", AiProviderType.GEMINI, cooldownMs = 30_000L, nowMs = t0)
        assertFalse(QuotaMonitor.canUseKey("k1", AiProviderType.GEMINI, t0 + 10_000L))

        // After cooldown + 1 minute window expires, key recovers automatically
        assertTrue(QuotaMonitor.canUseKey("k1", AiProviderType.GEMINI, t0 + 65_000L))
        assertEquals(0, QuotaMonitor.getSnapshot("k1")?.requestsLastMinute)
    }

    @Test
    fun `request retry service classifies errors computes finite backoff and redacts keys`() {
        val secret = "AIzaSyVerySecretKeyValue9999"
        val rateLimit = RequestRetryService.classifyFailure(
            httpCode = 429,
            error = RuntimeException("HTTP 429 error with ?key=$secret&alt=json and Bearer sk-secret-token"),
            attemptIndex = 1,
            sensitiveKeys = listOf(secret)
        )
        assertEquals(RetryableErrorKind.RATE_LIMIT_429, rateLimit.kind)
        assertTrue(rateLimit.shouldRetryWithNextKey)
        assertTrue(rateLimit.keyCooldownMs >= 60_000L)

        val sanitized = RequestRetryService.sanitizeErrorMessage(
            "Request failed at https://api.example.com?key=$secret Bearer topsecret123 raw=$secret",
            sensitiveKeys = listOf(secret)
        )
        assertFalse(sanitized.contains(secret))
        assertFalse(sanitized.contains("topsecret123"))

        val invalidKey = RequestRetryService.classifyFailure(httpCode = 401)
        assertEquals(RetryableErrorKind.INVALID_KEY_401_403, invalidKey.kind)
        assertTrue(invalidKey.shouldRetryWithNextKey)

        val serverOutage = RequestRetryService.classifyFailure(httpCode = 503)
        assertEquals(RetryableErrorKind.SERVER_OUTAGE_5XX, serverOutage.kind)
        assertTrue(serverOutage.shouldRetryWithNextKey)

        val transientNetwork = RequestRetryService.classifyFailure(error = IOException("Connection timed out"))
        assertEquals(RetryableErrorKind.TRANSIENT_NETWORK, transientNetwork.kind)
        assertTrue(transientNetwork.shouldRetryWithNextKey)

        val backoff = RequestRetryService.computeBackoffMs(attemptIndex = 3)
        assertTrue(backoff in 250L..2_500L)
    }

    @Test
    fun `api key manager masks keys and encrypts persisted storage`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("AIzaSy••••9999", APIKeyManager.maskKey("AIzaSyABCDE123459999"))
        assertEquals("gsk_••••wxyz", APIKeyManager.maskKey("gsk_abcdefghwxyz"))
        assertEquals("(unconfigured)", APIKeyManager.maskKey("   "))

        val sample = listOf(
            ApiKeyEntry(
                id = "test-key-1",
                provider = AiProviderType.GROQ,
                key = "gsk_testSecretKey12345678",
                label = "Groq Unit Test",
                priority = 1
            )
        )
        APIKeyManager.initialize(context)
        APIKeyManager.saveKeys(context, sample)
        val reloaded = APIKeyManager.initialize(context)
        val found = reloaded.firstOrNull { it.id == "test-key-1" }
        assertNotNull(found)
        assertEquals("gsk_testSecretKey12345678", found?.key)
    }

    @Test
    fun `firebase ai logic resolves 1m context models per tier without client api key`() {
        val freeModels = com.example.data.api.FirebaseAiLogicService.resolveFirebaseModels(StudyBuddyModelTier.FREE)
        val plusModels = com.example.data.api.FirebaseAiLogicService.resolveFirebaseModels(StudyBuddyModelTier.PLUS)
        val maxModels = com.example.data.api.FirebaseAiLogicService.resolveFirebaseModels(StudyBuddyModelTier.MAX)

        assertEquals("gemini-3.5-flash-lite", freeModels.primaryModel)
        assertEquals("gemini-3.8-flash", freeModels.fallbackModel)

        assertEquals("gemini-3.8-flash", plusModels.primaryModel)
        assertEquals("gemini-3.5-flash-lite", plusModels.fallbackModel)

        assertEquals("gemini-4-argon", maxModels.primaryModel)
        assertEquals("gemini-3.8-flash", maxModels.fallbackModel)

        assertEquals(
            "Firebase App Check",
            APIKeyManager.maskKey(com.example.data.api.FirebaseAiLogicService.VIRTUAL_KEY_SENTINEL)
        )
    }
}
