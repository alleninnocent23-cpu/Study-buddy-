package com.example.data.api

/**
 * Selects concrete model identifiers, parameter budgets, fallback models,
 * and depth directives for StudyBuddy Free, Plus, and Max tiers.
 */
object ModelSelectionService {

    fun resolveForProvider(
        tier: StudyBuddyModelTier,
        provider: AiProviderType,
        capability: AiTaskCapability = AiTaskCapability.TEXT_CHAT
    ): ResolvedModelConfig {
        val (primaryModel, fallbackModel) = when (tier) {
            StudyBuddyModelTier.FREE -> when (provider) {
                AiProviderType.GEMINI -> "gemini-2.5-flash" to "gemini-2.5-flash-lite"
                AiProviderType.OPENROUTER -> when (capability) {
                    AiTaskCapability.VISION_OCR -> "google/gemini-2.0-flash-exp:free" to "google/gemini-2.5-flash"
                    else -> "google/gemini-2.0-flash-exp:free" to "meta-llama/llama-3.1-8b-instruct:free"
                }
                AiProviderType.GROQ -> "openai/gpt-oss-120b" to "llama-3.1-8b-instant"
                AiProviderType.CUSTOM_OPENAI -> "gpt-4o-mini" to null
            }
            StudyBuddyModelTier.PLUS -> when (provider) {
                AiProviderType.GEMINI -> "gemini-2.5-flash" to "gemini-2.0-flash"
                AiProviderType.OPENROUTER -> "google/gemini-2.5-flash" to "openai/gpt-4o-mini"
                AiProviderType.GROQ -> "openai/gpt-oss-120b" to "llama-3.3-70b-versatile"
                AiProviderType.CUSTOM_OPENAI -> "gpt-4o" to "gpt-4o-mini"
            }
            StudyBuddyModelTier.MAX -> when (provider) {
                // Gemini 2.5 Pro supports up to 1M token context & deep reasoning; falls back cleanly to 2.5 Flash
                AiProviderType.GEMINI -> when (capability) {
                    AiTaskCapability.AUDIO_TRANSCRIPTION -> "gemini-2.5-flash" to "gemini-2.5-pro"
                    else -> "gemini-2.5-pro" to "gemini-2.5-flash"
                }
                AiProviderType.OPENROUTER -> "google/gemini-2.5-pro" to "google/gemini-2.5-flash"
                AiProviderType.GROQ -> "openai/gpt-oss-120b" to "llama-3.3-70b-versatile"
                AiProviderType.CUSTOM_OPENAI -> "gpt-4.1" to "gpt-4o"
            }
        }

        val directive = when (tier) {
            StudyBuddyModelTier.FREE ->
                "ACTIVE MODE: StudyBuddy Free. Deliver accurate, clear, concise academic explanations with core worked steps."
            StudyBuddyModelTier.PLUS ->
                "ACTIVE MODE: StudyBuddy Plus. Deliver thorough, detailed step-by-step reasoning, clear examples, and verify all mathematical working."
            StudyBuddyModelTier.MAX ->
                "ACTIVE MODE: StudyBuddy Max. Apply deep analytical reasoning, rigorously verify multi-step derivations, surface edge cases and exam traps, and synthesize complex context completely."
        }

        return ResolvedModelConfig(
            tier = tier,
            provider = provider,
            modelId = primaryModel,
            fallbackModelId = fallbackModel,
            temperature = tier.temperature,
            maxOutputTokens = tier.maxOutputTokens,
            maxHistoryTurns = tier.maxHistoryTurns,
            thinkingBudgetTokens = if (provider == AiProviderType.GEMINI) tier.thinkingBudgetTokens else 0,
            systemPromptDirective = directive
        )
    }
}
