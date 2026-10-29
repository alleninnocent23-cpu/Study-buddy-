package com.example.data.model

enum class ExamType(val code: String, val displayName: String, val subtitle: String) {
    WAEC("WAEC", "WAEC (West African Examinations Council)", "O'Level & A'Level WASSCE Past Papers (1997 - 2026)"),
    JAMB("JAMB", "JAMB (Joint Admissions and Matriculation Board)", "UTME & Direct Entry CBT Past Questions (1997 - 2026)")
}

enum class ExamPrepMode(val displayName: String, val description: String) {
    PRACTICE("Smart Practice Mode", "Instant answers, step-by-step solutions & why other options are wrong"),
    MOCK_EXAM("Timed Mock Exam", "Simulate real exam hall conditions with timer, marking scheme & report")
}

enum class ExamSession(val code: String, val displayName: String) {
    ALL("ALL", "All Exam Sessions"),
    MAY_JUNE("MAY_JUNE", "May/June (School Candidates)"),
    NOV_DEC("NOV_DEC", "Nov/Dec (Private Candidates - GCE)"),
    UTME_ANNUAL("UTME_ANNUAL", "Annual UTME CBT Session")
}

data class WaecJambQuestion(
    val id: String,
    val exam: ExamType,
    val year: Int,
    val session: String,
    val subject: String,
    val topic: String,
    val subtopic: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val whyOthersAreWrong: Map<String, String> = emptyMap(),
    val relatedConcepts: List<String> = emptyList(),
    val frequencyData: String = "Appears in 65% of past papers • High-Yield",
    val userSelectedIndex: Int? = null,
    val isFlagged: Boolean = false
) {
    val correctAnswerLetter: String
        get() = when (correctIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> "A"
        }
}

data class WaecJambConfig(
    val examType: ExamType = ExamType.WAEC,
    val subject: String = "Mathematics",
    val facultyTrack: String = "Science",
    val yearStart: Int = 1997,
    val yearEnd: Int = 2026,
    val selectedYear: Int? = null,
    val session: ExamSession = ExamSession.ALL,
    val topic: String = "All Topics",
    val difficulty: String = "Mixed",
    val questionCount: Int = 10,
    val mode: ExamPrepMode = ExamPrepMode.PRACTICE,
    val timedMinutes: Int = 15
)

enum class ExamScreenStage {
    GET_STARTED,
    SELECTION_SETUP,
    ACTIVE_EXAM,
    EXAM_RESULTS
}

data class WaecJambState(
    val stage: ExamScreenStage = ExamScreenStage.GET_STARTED,
    val config: WaecJambConfig = WaecJambConfig(),
    val questions: List<WaecJambQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val secondsRemaining: Int = 15 * 60,
    val totalTimeSeconds: Int = 15 * 60,
    val isTimerActive: Boolean = false,
    val isSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val filterSearchQuery: String = ""
) {
    val currentQuestion: WaecJambQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val answeredCount: Int
        get() = questions.count { it.userSelectedIndex != null }

    val correctCount: Int
        get() = questions.count { it.userSelectedIndex != null && it.userSelectedIndex == it.correctIndex }

    val scorePercentage: Int
        get() = if (questions.isNotEmpty()) ((correctCount.toFloat() / questions.size.toFloat()) * 100).toInt() else 0

    val waecGrade: String
        get() = when (scorePercentage) {
            in 75..100 -> "A1 (Distinction - Excellent)"
            in 70..74 -> "B2 (Very Good)"
            in 65..69 -> "B3 (Good)"
            in 60..64 -> "C4 (Credit)"
            in 55..59 -> "C5 (Credit)"
            in 50..54 -> "C6 (Credit)"
            in 45..49 -> "D7 (Pass)"
            in 40..44 -> "E8 (Pass)"
            else -> "F9 (Fail)"
        }

    val jambScoreEstimate: Int
        get() = (scorePercentage * 4) // Out of 400
}
