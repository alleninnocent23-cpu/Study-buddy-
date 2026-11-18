package com.example.data.api

/**
 * Capability-aware, quota-aware, and health-aware AI API key & provider rotation service.
 * Ensures smooth load balancing, strict exclusion of invalid/exhausted keys,
 * and automatic recovery when cooldown windows expire.
 */
object APIKeyRotationService {

    fun supportsCapability(provider: AiProviderType, capability: AiTaskCapability): Boolean {
        return when (capability) {
            AiTaskCapability.TEXT_CHAT -> true
            AiTaskCapability.STRUCTURED_JSON -> true
            AiTaskCapability.VISION_OCR -> when (provider) {
                AiProviderType.GEMINI,
                AiProviderType.OPENROUTER,
                AiProviderType.CUSTOM_OPENAI -> true
                AiProviderType.GROQ -> false
            }
            AiTaskCapability.AUDIO_TRANSCRIPTION -> provider == AiProviderType.GEMINI
        }
    }

    /**
     * Ranks available keys by:
     * 1. Provider suitability for the requested tier and capability
     * 2. Health & available quota (healthy + non-rate-limited keys first)
     * 3. Configured key priority
     * 4. Least recently used / lowest minute load for gentle round-robin rotation
     */
    fun selectOrderedCandidates(
        allKeys: List<ApiKeyEntry>,
        tier: StudyBuddyModelTier,
        capability: AiTaskCapability,
        excludedKeyIds: Set<String> = emptySet(),
        nowMs: Long = System.currentTimeMillis()
    ): List<ApiKeyEntry> {
        val preferredProviders = listOf(
            AiProviderType.GEMINI,
            AiProviderType.GROQ,
            AiProviderType.OPENROUTER,
            AiProviderType.CUSTOM_OPENAI
        )

        // For Vision/Audio capabilities, ensure Gemini (and vision-capable OpenRouter/OpenAI) take precedence
        val orderedProviders = if (capability == AiTaskCapability.VISION_OCR || capability == AiTaskCapability.AUDIO_TRANSCRIPTION) {
            preferredProviders.filter { supportsCapability(it, capability) }
        } else {
            preferredProviders
        }

        val candidates = allKeys.filter { entry ->
            entry.id !in excludedKeyIds &&
                entry.isEnabled &&
                entry.key.isNotBlank() &&
                entry.status != KeyHealthStatus.INVALID &&
                supportsCapability(entry.provider, capability) &&
                ProviderHealthMonitor.isProviderAvailable(entry.provider, nowMs) &&
                QuotaMonitor.canUseKey(entry.id, entry.provider, nowMs) &&
                nowMs >= entry.cooldownUntilTimestamp
        }

        return candidates.sortedWith(
            compareBy<ApiKeyEntry>(
                { if (it.status == KeyHealthStatus.HEALTHY) 0 else 1 },
                { it.priority },
                {
                    val idx = orderedProviders.indexOf(it.provider)
                    if (idx >= 0) idx else 99
                },
                { if (FirebaseAiLogicService.isVirtualKey(it)) 1 else 0 },
                { QuotaMonitor.getSnapshot(it.id)?.requestsLastMinute ?: 0 },
                { it.lastSuccessTimestamp }
            )
        )
    }

    /**
     * Picks the single best candidate right now, skipping already tried key IDs.
     */
    fun pickNextCandidate(
        allKeys: List<ApiKeyEntry>,
        tier: StudyBuddyModelTier,
        capability: AiTaskCapability,
        excludedKeyIds: Set<String> = emptySet(),
        nowMs: Long = System.currentTimeMillis()
    ): ApiKeyEntry? {
        return selectOrderedCandidates(
            allKeys = allKeys,
            tier = tier,
            capability = capability,
            excludedKeyIds = excludedKeyIds,
            nowMs = nowMs
        ).firstOrNull()
    }
}
