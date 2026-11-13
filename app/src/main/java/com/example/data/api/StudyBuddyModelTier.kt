package com.example.data.api

/**
 * The three StudyBuddy AI model tiers displayed in the Model Selector panel.
 * Every description, feature bullet, and backend routing parameter reflects actual
 * implemented provider/model selection and request configuration.
 */
enum class StudyBuddyModelTier(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val description: String,
    val features: List<String>,
    val requiresSubscription: Boolean,
    val temperature: Double,
    val maxOutputTokens: Int,
    val maxHistoryTurns: Int,
    val thinkingBudgetTokens: Int
) {
    FREE(
        id = "studybuddy_free",
        displayName = "StudyBuddy Free",
        shortLabel = "Free",
        description = "Your everyday AI study assistant.",
        features = listOf(
            "General AI conversations",
            "Homework assistance",
            "Educational explanations",
            "Basic mathematics solving",
            "Basic image understanding where supported",
            "Free AI providers"
        ),
        requiresSubscription = false,
        temperature = 0.45,
        maxOutputTokens = 2048,
        maxHistoryTurns = 4,
        thinkingBudgetTokens = 0
    ),
    PLUS(
        id = "studybuddy_plus",
        displayName = "StudyBuddy Plus",
        shortLabel = "Plus",
        description = "Advanced learning and productivity assistant.",
        features = listOf(
            "More capable models",
            "Advanced math solving",
            "Detailed step-by-step explanations",
            "Stronger reasoning",
            "Larger context windows",
            "Improved image understanding",
            "Priority provider selection when available"
        ),
        requiresSubscription = true,
        temperature = 0.35,
        maxOutputTokens = 4096,
        maxHistoryTurns = 8,
        thinkingBudgetTokens = 1024
    ),
    MAX(
        id = "studybuddy_max",
        displayName = "StudyBuddy Max",
        shortLabel = "Max",
        description = "The most capable StudyBuddy experience.",
        features = listOf(
            "Strongest integrated AI models",
            "Advanced reasoning capabilities",
            "Complex academic problem solving",
            "Large-context support including 1M-context models where available",
            "Advanced image understanding",
            "Intelligent model selection",
            "Priority routing when supported"
        ),
        requiresSubscription = true,
        temperature = 0.25,
        maxOutputTokens = 8192,
        maxHistoryTurns = 14,
        thinkingBudgetTokens = 4096
    );

    companion object {
        fun fromId(id: String?): StudyBuddyModelTier {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: FREE
        }
    }
}

enum class AiTaskCapability {
    TEXT_CHAT,
    VISION_OCR,
    AUDIO_TRANSCRIPTION,
    STRUCTURED_JSON
}

data class ResolvedModelConfig(
    val tier: StudyBuddyModelTier,
    val provider: AiProviderType,
    val modelId: String,
    val fallbackModelId: String?,
    val temperature: Double,
    val maxOutputTokens: Int,
    val maxHistoryTurns: Int,
    val thinkingBudgetTokens: Int,
    val systemPromptDirective: String
)
