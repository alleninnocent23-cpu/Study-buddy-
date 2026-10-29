package com.example.data.repository

import com.example.data.local.QuizEntity
import com.example.data.local.StudySessionEntity
import com.example.data.model.Badge
import com.example.data.model.BadgeCategory
import com.example.data.model.UserRewardsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RewardsRepository {

    fun computeRewards(
        sessions: List<StudySessionEntity>,
        quizzes: List<QuizEntity>,
        examsCompleted: Int = 0,
        examDistinctions: Int = 0,
        previouslyUnlockedBadgeIds: Set<String> = emptySet()
    ): Pair<UserRewardsState, Badge?> {
        val sessionsCount = sessions.size
        val quizQuestionsCount = quizzes.sumOf { it.totalQuestions }
        val quizDistinctions = quizzes.count { q ->
            q.totalQuestions > 0 && (q.score.toFloat() / q.totalQuestions.toFloat()) >= 0.8f
        }
        val totalDistinctions = quizDistinctions + examDistinctions

        val sessionSubjects = sessions.map { it.subject }.filter { it.isNotBlank() }
        val quizSubjects = quizzes.map { it.subject }.filter { it.isNotBlank() }
        val distinctSubjects = (sessionSubjects + quizSubjects).distinct().size

        val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

        val rawBadges = listOf(
            // --- STUDY SESSIONS ---
            Badge(
                id = "BADGE_FIRST_SESSION",
                title = "First Step",
                description = "Complete your 1st AI study chat session",
                iconEmoji = "🌱",
                category = BadgeCategory.STUDY_SESSIONS,
                currentProgress = sessionsCount,
                requiredProgress = 1,
                isUnlocked = sessionsCount >= 1,
                xpReward = 50,
                unlockedDate = if (sessionsCount >= 1) dateFormatted else null
            ),
            Badge(
                id = "BADGE_CURIOUS_MIND",
                title = "Curious Mind",
                description = "Complete 5 AI study sessions",
                iconEmoji = "🧠",
                category = BadgeCategory.STUDY_SESSIONS,
                currentProgress = sessionsCount,
                requiredProgress = 5,
                isUnlocked = sessionsCount >= 5,
                xpReward = 100,
                unlockedDate = if (sessionsCount >= 5) dateFormatted else null
            ),
            Badge(
                id = "BADGE_DEDICATED_SCHOLAR",
                title = "Dedicated Scholar",
                description = "Complete 15 AI study sessions",
                iconEmoji = "🎓",
                category = BadgeCategory.STUDY_SESSIONS,
                currentProgress = sessionsCount,
                requiredProgress = 15,
                isUnlocked = sessionsCount >= 15,
                xpReward = 200,
                unlockedDate = if (sessionsCount >= 15) dateFormatted else null
            ),
            Badge(
                id = "BADGE_MASTER_RESEARCHER",
                title = "Master Researcher",
                description = "Complete 30 AI study sessions",
                iconEmoji = "🔬",
                category = BadgeCategory.STUDY_SESSIONS,
                currentProgress = sessionsCount,
                requiredProgress = 30,
                isUnlocked = sessionsCount >= 30,
                xpReward = 350,
                unlockedDate = if (sessionsCount >= 30) dateFormatted else null
            ),

            // --- QUIZ MASTERY ---
            Badge(
                id = "BADGE_QUIZ_NOVICE",
                title = "Quiz Apprentice",
                description = "Complete 10 quiz questions",
                iconEmoji = "⚡",
                category = BadgeCategory.QUIZ_MASTERY,
                currentProgress = quizQuestionsCount,
                requiredProgress = 10,
                isUnlocked = quizQuestionsCount >= 10,
                xpReward = 75,
                unlockedDate = if (quizQuestionsCount >= 10) dateFormatted else null
            ),
            Badge(
                id = "BADGE_QUIZ_EXPERT",
                title = "Knowledge Gladiator",
                description = "Complete 25 quiz questions",
                iconEmoji = "🎯",
                category = BadgeCategory.QUIZ_MASTERY,
                currentProgress = quizQuestionsCount,
                requiredProgress = 25,
                isUnlocked = quizQuestionsCount >= 25,
                xpReward = 150,
                unlockedDate = if (quizQuestionsCount >= 25) dateFormatted else null
            ),
            Badge(
                id = "BADGE_QUIZ_CENTURION",
                title = "Centurion Thinker",
                description = "Complete 50 quiz questions",
                iconEmoji = "🏆",
                category = BadgeCategory.QUIZ_MASTERY,
                currentProgress = quizQuestionsCount,
                requiredProgress = 50,
                isUnlocked = quizQuestionsCount >= 50,
                xpReward = 300,
                unlockedDate = if (quizQuestionsCount >= 50) dateFormatted else null
            ),
            Badge(
                id = "BADGE_QUIZ_MASTER",
                title = "Grand Quizmaster",
                description = "Complete 100 quiz questions across sessions",
                iconEmoji = "🌟",
                category = BadgeCategory.QUIZ_MASTERY,
                currentProgress = quizQuestionsCount,
                requiredProgress = 100,
                isUnlocked = quizQuestionsCount >= 100,
                xpReward = 500,
                unlockedDate = if (quizQuestionsCount >= 100) dateFormatted else null
            ),

            // --- WAEC & JAMB EXAMS ---
            Badge(
                id = "BADGE_EXAM_PIONEER",
                title = "WAEC & JAMB Pioneer",
                description = "Complete your 1st official past paper exam practice",
                iconEmoji = "👑",
                category = BadgeCategory.EXAM_PREP,
                currentProgress = examsCompleted,
                requiredProgress = 1,
                isUnlocked = examsCompleted >= 1,
                xpReward = 100,
                unlockedDate = if (examsCompleted >= 1) dateFormatted else null
            ),
            Badge(
                id = "BADGE_EXAM_HERO",
                title = "Exam Hall Hero",
                description = "Complete 5 WAEC or JAMB exam sessions",
                iconEmoji = "🎖️",
                category = BadgeCategory.EXAM_PREP,
                currentProgress = examsCompleted,
                requiredProgress = 5,
                isUnlocked = examsCompleted >= 5,
                xpReward = 350,
                unlockedDate = if (examsCompleted >= 5) dateFormatted else null
            ),

            // --- SPECIAL MILESTONES ---
            Badge(
                id = "BADGE_HIGH_ACHIEVER",
                title = "Distinction Scholar",
                description = "Score 80%+ (WAEC A1 Grade) on any quiz or exam",
                iconEmoji = "💯",
                category = BadgeCategory.SPECIAL,
                currentProgress = totalDistinctions,
                requiredProgress = 1,
                isUnlocked = totalDistinctions >= 1,
                xpReward = 150,
                unlockedDate = if (totalDistinctions >= 1) dateFormatted else null
            ),
            Badge(
                id = "BADGE_MULTI_SUBJECT",
                title = "Polymath Explorer",
                description = "Study or take quizzes in 3 or more subjects",
                iconEmoji = "🌐",
                category = BadgeCategory.SPECIAL,
                currentProgress = distinctSubjects,
                requiredProgress = 3,
                isUnlocked = distinctSubjects >= 3,
                xpReward = 150,
                unlockedDate = if (distinctSubjects >= 3) dateFormatted else null
            )
        )

        val totalXp = rawBadges.filter { it.isUnlocked }.sumOf { it.xpReward } + (sessionsCount * 20) + (quizQuestionsCount * 5)

        val rank = when {
            totalXp >= 1500 -> "Grand Academic Luminary 🌟"
            totalXp >= 1000 -> "Senior Scholar 🎓"
            totalXp >= 600 -> "Elite Academician 🏛️"
            totalXp >= 300 -> "Knowledge Seeker 🧠"
            totalXp >= 100 -> "Apprentice Learner 📖"
            else -> "Novice Scholar 🌱"
        }

        // Find newly unlocked badge (was not previously unlocked but is now unlocked)
        val newlyUnlocked = rawBadges.firstOrNull { badge ->
            badge.isUnlocked && !previouslyUnlockedBadgeIds.contains(badge.id)
        }

        val state = UserRewardsState(
            totalSessionsCompleted = sessionsCount,
            totalQuizQuestionsAnswered = quizQuestionsCount,
            totalExamsCompleted = examsCompleted,
            totalDistinctionsEarned = totalDistinctions,
            distinctSubjectsCount = distinctSubjects,
            totalXp = totalXp,
            rankTitle = rank,
            badges = rawBadges,
            newlyUnlockedBadge = newlyUnlocked
        )

        return Pair(state, newlyUnlocked)
    }
}
