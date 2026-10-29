package com.example.data.model

enum class BadgeCategory(val displayName: String) {
    ALL("All Badges"),
    STUDY_SESSIONS("Study Sessions"),
    QUIZ_MASTERY("Quiz Mastery"),
    EXAM_PREP("WAEC & JAMB"),
    SPECIAL("Special Milestones")
}

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: BadgeCategory,
    val currentProgress: Int,
    val requiredProgress: Int,
    val isUnlocked: Boolean,
    val xpReward: Int = 50,
    val unlockedDate: String? = null
) {
    val progressFraction: Float
        get() = if (requiredProgress > 0) (currentProgress.toFloat() / requiredProgress.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

data class UserRewardsState(
    val totalSessionsCompleted: Int = 0,
    val totalQuizQuestionsAnswered: Int = 0,
    val totalExamsCompleted: Int = 0,
    val totalDistinctionsEarned: Int = 0,
    val distinctSubjectsCount: Int = 0,
    val totalXp: Int = 0,
    val rankTitle: String = "Academic Scholar",
    val badges: List<Badge> = emptyList(),
    val newlyUnlockedBadge: Badge? = null
) {
    val unlockedCount: Int
        get() = badges.count { it.isUnlocked }

    val totalBadgesCount: Int
        get() = badges.size

    val completionRate: Int
        get() = if (totalBadgesCount > 0) ((unlockedCount.toFloat() / totalBadgesCount.toFloat()) * 100).toInt() else 0
}
