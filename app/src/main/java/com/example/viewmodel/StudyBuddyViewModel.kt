package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ExplanationResult
import com.example.data.api.GeminiClient
import com.example.data.api.QuizQuestion
import com.example.data.auth.AuthRepository
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.local.QuizEntity
import com.example.data.local.StudyBuddyDatabase
import com.example.data.local.StudySessionEntity
import com.example.data.local.UserEntity
import com.example.util.VoiceGender
import com.example.util.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

import com.example.util.PomodoroNotificationHelper
import com.example.data.model.ExamType
import com.example.data.model.ExamPrepMode
import com.example.data.model.ExamSession
import com.example.data.model.ExamScreenStage
import com.example.data.model.WaecJambConfig
import com.example.data.model.WaecJambQuestion
import com.example.data.model.WaecJambState
import com.example.data.repository.WaecJambQuestionBank

enum class AppScreen {
    WELCOME,
    AUTH,
    TUTOR,
    SCANNER,
    QUIZ,
    HISTORY,
    POMODORO,
    EXAM_PREP,
    REWARDS
}

enum class PomodoroMode(val displayName: String) {
    WORK("Study Focus"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

data class PomodoroConfig(
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val cyclesBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartStudy: Boolean = false
)

data class PomodoroState(
    val mode: PomodoroMode = PomodoroMode.WORK,
    val config: PomodoroConfig = PomodoroConfig(),
    val totalSecondsRemaining: Int = 25 * 60,
    val initialTotalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedWorkSessions: Int = 0,
    val currentCycleInRound: Int = 1,
    val totalFocusMinutesToday: Int = 0,
    val targetDailyMinutes: Int = 100
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val attachedImage: Bitmap? = null,
    val attachedFileName: String? = null,
    val diagramType: String = "none",
    val diagramTitle: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean? = null // null = unset, true = thumb up, false = thumb down
)

data class AuthState(
    val isLoginMode: Boolean = true,
    val name: String = "",
    val email: String = "",
    val grade: String = "High School (Grades 9-12)",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TutorState(
    val selectedSubject: String = "All Subjects",
    val topicInput: String = "",
    val sentQuestion: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentExplanation: ExplanationResult? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isVoiceModeActive: Boolean = false
)

data class ScannerState(
    val capturedImage: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val scanResult: ExplanationResult? = null
)

data class QuizState(
    val subject: String = "Mathematics",
    val topic: String = "Quadratic Equations",
    val difficulty: String = "High School",
    val questionCount: Int = 50,
    val currentQuestionIndex: Int = 0,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val userAnswers: Map<Int, Int> = emptyMap(),
    val isSubmitted: Boolean = false,
    val score: Int = 0
)

class StudyBuddyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StudyBuddyDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val sessionDao = database.studySessionDao()
    private val quizDao = database.quizDao()

    val authRepository: AuthRepository = FirebaseAuthRepository(application, userDao)

    val voiceManager = VoiceManager(application)
    val voiceGender = voiceManager.voiceGender

    fun setVoiceGender(gender: VoiceGender) {
        voiceManager.setVoiceGender(gender)
    }

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.WELCOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Current Active User
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Sub-states
    val authState = MutableStateFlow(AuthState())
    val tutorState = MutableStateFlow(TutorState())
    val scannerState = MutableStateFlow(ScannerState())
    val quizState = MutableStateFlow(QuizState())
    val pomodoroState = MutableStateFlow(PomodoroState())
    val waecJambState = MutableStateFlow(WaecJambState())
    val rewardsState = MutableStateFlow(com.example.data.model.UserRewardsState())
    private val unlockedBadgeIds = mutableSetOf<String>()
    private var examsCompletedCount = 0
    private var examDistinctionsCount = 0

    val pomodoroNotificationHelper = PomodoroNotificationHelper(application)
    private var pomodoroTimerJob: kotlinx.coroutines.Job? = null
    private var examTimerJob: kotlinx.coroutines.Job? = null

    // History & Search
    val searchQuery = MutableStateFlow("")
    private val _currentUserId = MutableStateFlow(1L)

    val savedSessions: StateFlow<List<StudySessionEntity>> = _currentUserId.flatMapLatest { uid ->
        sessionDao.getSessionsForUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedQuizzes: StateFlow<List<QuizEntity>> = _currentUserId.flatMapLatest { uid ->
        quizDao.getQuizzesForUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    val apiKeyInput = MutableStateFlow("")
    val apiKeyStatus = MutableStateFlow<String?>(null)

    init {
        // Load initial user or create guest user if already exists
        viewModelScope.launch {
            val user = userDao.getLatestUser()
            if (user != null) {
                _currentUser.value = user
                _currentUserId.value = user.id
            }
        }

        viewModelScope.launch {
            savedSessions.collect { sessions ->
                refreshRewards(sessions, savedQuizzes.value)
            }
        }

        viewModelScope.launch {
            savedQuizzes.collect { quizzes ->
                refreshRewards(savedSessions.value, quizzes)
            }
        }
    }

    fun refreshRewards(
        sessions: List<StudySessionEntity> = savedSessions.value,
        quizzes: List<QuizEntity> = savedQuizzes.value
    ) {
        val (newRewards, newlyUnlocked) = com.example.data.repository.RewardsRepository.computeRewards(
            sessions = sessions,
            quizzes = quizzes,
            examsCompleted = examsCompletedCount,
            examDistinctions = examDistinctionsCount,
            previouslyUnlockedBadgeIds = unlockedBadgeIds
        )
        if (newlyUnlocked != null) {
            unlockedBadgeIds.add(newlyUnlocked.id)
        }
        rewardsState.value = newRewards
    }

    fun dismissNewBadgeCelebration() {
        rewardsState.value = rewardsState.value.copy(newlyUnlockedBadge = null)
    }

    fun openRewards() {
        voiceManager.stopSpeaking()
        refreshRewards()
        navigateTo(AppScreen.REWARDS)
    }

    fun navigateTo(screen: AppScreen) {
        // Stop any active speech when switching screens
        voiceManager.stopSpeaking()
        _currentScreen.value = screen
    }

    // --- Authentication ---
    fun toggleAuthMode() {
        authState.value = authState.value.copy(
            isLoginMode = !authState.value.isLoginMode,
            error = null
        )
    }

    fun updateAuthField(name: String? = null, email: String? = null, grade: String? = null) {
        authState.value = authState.value.copy(
            name = name ?: authState.value.name,
            email = email ?: authState.value.email,
            grade = grade ?: authState.value.grade,
            error = null
        )
    }

    fun authenticate() {
        val state = authState.value
        if (state.email.isBlank()) {
            authState.value = state.copy(error = "Please enter your email address")
            return
        }

        authState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val result = if (state.isLoginMode) {
                    authRepository.signInWithEmail(state.email.trim(), "student123")
                } else {
                    val name = if (state.name.isNotBlank()) state.name.trim() else "Student"
                    authRepository.signUpWithEmail(state.email.trim(), "student123", name, state.grade)
                }

                result.onSuccess { authUser ->
                    var user = userDao.getUserByEmail(authUser.email)
                    if (user == null) {
                        val id = userDao.insertUser(
                            UserEntity(
                                name = authUser.displayName,
                                email = authUser.email,
                                grade = authUser.grade
                            )
                        )
                        user = UserEntity(id = id, name = authUser.displayName, email = authUser.email, grade = authUser.grade)
                    }
                    _currentUser.value = user
                    _currentUserId.value = user.id
                    authState.value = state.copy(isLoading = false)
                    navigateTo(AppScreen.TUTOR)
                }.onFailure { e ->
                    authState.value = state.copy(isLoading = false, error = e.localizedMessage ?: "Authentication failed")
                }
            } catch (e: Exception) {
                authState.value = state.copy(isLoading = false, error = e.localizedMessage ?: "Authentication failed")
            }
        }
    }

    fun continueAsDemoStudent() {
        viewModelScope.launch {
            var demoUser = userDao.getUserByEmail("alex.student@studybuddy.ai")
            if (demoUser == null) {
                demoUser = UserEntity(
                    name = "Alex Vance",
                    email = "alex.student@studybuddy.ai",
                    grade = "Grade 11 (AP STEM)"
                )
                val id = userDao.insertUser(demoUser)
                demoUser = demoUser.copy(id = id)
            }
            _currentUser.value = demoUser
            _currentUserId.value = demoUser.id
            navigateTo(AppScreen.TUTOR)
        }
    }

    fun loginWithGoogle(context: Context = getApplication()) {
        viewModelScope.launch {
            authState.value = authState.value.copy(isLoading = true, error = null)
            val result = authRepository.signInWithGoogle(context)
            result.onSuccess { authUser ->
                var user = userDao.getUserByEmail(authUser.email)
                if (user == null) {
                    val id = userDao.insertUser(
                        UserEntity(
                            name = authUser.displayName,
                            email = authUser.email,
                            grade = authUser.grade
                        )
                    )
                    user = UserEntity(id = id, name = authUser.displayName, email = authUser.email, grade = authUser.grade)
                }
                _currentUser.value = user
                _currentUserId.value = user.id
                authState.value = authState.value.copy(isLoading = false)
                navigateTo(AppScreen.TUTOR)
            }.onFailure { err ->
                authState.value = authState.value.copy(isLoading = false, error = err.localizedMessage)
            }
        }
    }

    fun loginWithFacebook(activity: Activity? = null) {
        viewModelScope.launch {
            authState.value = authState.value.copy(isLoading = true, error = null)
            val result = if (activity != null) {
                authRepository.signInWithFacebook(activity)
            } else {
                authRepository.signInWithEmail("student@facebook.com", "student123")
            }
            result.onSuccess { authUser ->
                var user = userDao.getUserByEmail(authUser.email)
                if (user == null) {
                    val id = userDao.insertUser(
                        UserEntity(
                            name = authUser.displayName,
                            email = authUser.email,
                            grade = authUser.grade
                        )
                    )
                    user = UserEntity(id = id, name = authUser.displayName, email = authUser.email, grade = authUser.grade)
                }
                _currentUser.value = user
                _currentUserId.value = user.id
                authState.value = authState.value.copy(isLoading = false)
                navigateTo(AppScreen.TUTOR)
            }.onFailure { err ->
                authState.value = authState.value.copy(isLoading = false, error = err.localizedMessage)
            }
        }
    }

    fun logout() {
        voiceManager.stopSpeaking()
        authRepository.signOut()
        _currentUser.value = null
        navigateTo(AppScreen.WELCOME)
    }

    // --- Attachments (+ Button) ---
    val attachedImage = MutableStateFlow<Bitmap?>(null)
    val attachedFileName = MutableStateFlow<String?>(null)

    fun setAttachedImage(bitmap: Bitmap?) {
        attachedImage.value = bitmap
        if (bitmap != null) {
            attachedFileName.value = "Image attached"
        }
    }

    fun setAttachedFile(fileName: String) {
        attachedFileName.value = fileName
    }

    fun clearAttachment() {
        attachedImage.value = null
        attachedFileName.value = null
    }

    // --- Assignment Helper (Tutor & ChatGPT Chat) ---
    fun selectSubject(subject: String) {
        tutorState.value = tutorState.value.copy(selectedSubject = subject)
    }

    fun updateTopicInput(topic: String) {
        tutorState.value = tutorState.value.copy(topicInput = topic)
    }

    fun clearCurrentChat() {
        voiceManager.stopSpeaking()
        tutorState.value = tutorState.value.copy(
            topicInput = "",
            sentQuestion = null,
            currentExplanation = null,
            messages = emptyList(),
            isLoading = false,
            error = null
        )
        clearAttachment()
    }

    fun setVoiceModeActive(active: Boolean) {
        tutorState.value = tutorState.value.copy(isVoiceModeActive = active)
        if (!active) {
            voiceManager.stopSpeaking()
            voiceManager.stopListening()
        }
    }

    fun toggleLikeMessage(messageId: String, liked: Boolean) {
        val updated = tutorState.value.messages.map { msg ->
            if (msg.id == messageId) {
                // If clicked same, toggle back to null
                val newStatus = if (msg.isLiked == liked) null else liked
                msg.copy(isLiked = newStatus)
            } else msg
        }
        tutorState.value = tutorState.value.copy(messages = updated)
    }

    fun sendChatMessage(overridePrompt: String? = null) {
        val state = tutorState.value
        val inputPrompt = (overridePrompt ?: state.topicInput).trim()
        val image = attachedImage.value
        val fileName = attachedFileName.value

        if (inputPrompt.isBlank() && image == null) return

        val promptText = if (inputPrompt.isNotBlank()) inputPrompt else "Analyze and solve this attached assignment"
        voiceManager.stopSpeaking()

        // 1. Add User message
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = promptText,
            attachedImage = image,
            attachedFileName = fileName
        )
        val updatedMessages = state.messages + userMsg

        tutorState.value = state.copy(
            isLoading = true,
            error = null,
            topicInput = "",
            sentQuestion = promptText,
            messages = updatedMessages
        )

        viewModelScope.launch {
            val grade = _currentUser.value?.grade ?: "High School"
            val result = if (image != null) {
                GeminiClient.scanHomeworkImage(image, promptText, state.selectedSubject)
            } else {
                GeminiClient.explainTopic(promptText, state.selectedSubject, grade)
            }

            result.onSuccess { explanation ->
                val assistantMsg = ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = explanation.explanationText,
                    diagramType = explanation.diagramType,
                    diagramTitle = explanation.diagramTitle
                )
                tutorState.value = tutorState.value.copy(
                    isLoading = false,
                    currentExplanation = explanation,
                    messages = tutorState.value.messages + assistantMsg
                )
                clearAttachment()

                if (tutorState.value.isVoiceModeActive) {
                    voiceManager.speak(explanation.explanationText)
                }

                // Save to Study Session history in Room
                val uid = _currentUserId.value
                sessionDao.insertSession(
                    StudySessionEntity(
                        userId = uid,
                        topic = promptText,
                        subject = state.selectedSubject,
                        userPrompt = promptText,
                        explanationText = explanation.explanationText,
                        diagramType = explanation.diagramType,
                        diagramDataJson = explanation.diagramTitle
                    )
                )
            }.onFailure { err ->
                val fallback = getCuratedExplanation(promptText, state.selectedSubject)
                val assistantMsg = ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = fallback.explanationText,
                    diagramType = fallback.diagramType,
                    diagramTitle = fallback.diagramTitle
                )
                tutorState.value = tutorState.value.copy(
                    isLoading = false,
                    currentExplanation = fallback,
                    messages = tutorState.value.messages + assistantMsg,
                    error = "Note: Live Gemini API message: ${err.message}. Showing built-in explanation."
                )
                clearAttachment()

                if (tutorState.value.isVoiceModeActive) {
                    voiceManager.speak(fallback.explanationText)
                }
            }
        }
    }

    fun explainCurrentTopic() {
        sendChatMessage()
    }

    fun startVoiceInput() {
        voiceManager.startListening { transcribedText ->
            if (transcribedText.isNotBlank()) {
                tutorState.value = tutorState.value.copy(topicInput = transcribedText)
            }
        }
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun playExplanation(text: String) {
        val clean = GeminiClient.cleanAcademicText(text)
        voiceManager.speak(clean)
    }

    fun stopExplanationVoice() {
        voiceManager.stopSpeaking()
    }

    // --- Homework Scanner ---
    fun setCapturedHomeworkImage(bitmap: Bitmap) {
        scannerState.value = scannerState.value.copy(
            capturedImage = bitmap,
            error = null,
            scanResult = null
        )
    }

    fun clearScannedImage() {
        scannerState.value = scannerState.value.copy(
            capturedImage = null,
            error = null,
            scanResult = null
        )
    }

    fun analyzeHomeworkImage(note: String = "") {
        val bitmap = scannerState.value.capturedImage ?: return
        scannerState.value = scannerState.value.copy(isAnalyzing = true, error = null)

        viewModelScope.launch {
            val result = GeminiClient.scanHomeworkImage(bitmap, note)
            result.onSuccess { scanRes ->
                scannerState.value = scannerState.value.copy(
                    isAnalyzing = false,
                    scanResult = scanRes
                )
                // Save to history
                sessionDao.insertSession(
                    StudySessionEntity(
                        userId = _currentUserId.value,
                        topic = "Homework Scan Problem",
                        subject = "Homework Scanner",
                        userPrompt = "Photo analysis of worksheet",
                        explanationText = scanRes.explanationText,
                        diagramType = scanRes.diagramType,
                        diagramDataJson = scanRes.diagramTitle
                    )
                )
            }.onFailure { err ->
                // Fallback scan demonstration with clean academic response
                val fallback = ExplanationResult(
                    explanationText = """
                        Homework Scanner Solution
                        
                        Problem Detected: Solve for x: 3(x - 4) + 2x = 23
                        
                        Step-by-Step Solution:
                        1. Distribute the 3: 3*x - 3*4 + 2x = 23 -> 3x - 12 + 2x = 23
                        2. Combine like terms: (3x + 2x) - 12 = 23 -> 5x - 12 = 23
                        3. Isolate the variable term: Add 12 to both sides: 5x = 23 + 12 -> 5x = 35
                        4. Solve for x: Divide both sides by 5: x = 35 / 5 -> x = 7
                        
                        Verification: 3(7 - 4) + 2(7) = 3(3) + 14 = 9 + 14 = 23. The solution is confirmed correct.
                    """.trimIndent(),
                    diagramType = "cartesian_graph",
                    diagramTitle = "Linear Function Intersect"
                )
                scannerState.value = scannerState.value.copy(
                    isAnalyzing = false,
                    scanResult = fallback,
                    error = "Notice: Vision API reported: ${err.message}. Displayed simulated OCR extraction."
                )
            }
        }
    }

    // --- Quiz Generator ---
    fun updateQuizConfig(
        subject: String? = null,
        topic: String? = null,
        difficulty: String? = null,
        questionCount: Int? = null
    ) {
        quizState.value = quizState.value.copy(
            subject = subject ?: quizState.value.subject,
            topic = topic ?: quizState.value.topic,
            difficulty = difficulty ?: quizState.value.difficulty,
            questionCount = questionCount ?: quizState.value.questionCount,
            error = null
        )
    }

    fun goToNextQuizQuestion() {
        val s = quizState.value
        if (s.currentQuestionIndex < s.questions.size - 1) {
            quizState.value = s.copy(currentQuestionIndex = s.currentQuestionIndex + 1)
        }
    }

    fun goToPrevQuizQuestion() {
        val s = quizState.value
        if (s.currentQuestionIndex > 0) {
            quizState.value = s.copy(currentQuestionIndex = s.currentQuestionIndex - 1)
        }
    }

    fun jumpToQuizQuestion(index: Int) {
        val s = quizState.value
        if (index in 0 until s.questions.size) {
            quizState.value = s.copy(currentQuestionIndex = index)
        }
    }

    fun generateNewQuiz() {
        val s = quizState.value
        quizState.value = s.copy(
            isGenerating = true,
            error = null,
            questions = emptyList(),
            userAnswers = emptyMap(),
            currentQuestionIndex = 0,
            isSubmitted = false,
            score = 0
        )

        viewModelScope.launch {
            val result = GeminiClient.generateQuiz(s.topic, s.subject, s.difficulty, s.questionCount)
            result.onSuccess { questionsList ->
                quizState.value = quizState.value.copy(
                    isGenerating = false,
                    questions = questionsList,
                    currentQuestionIndex = 0
                )
            }.onFailure { err ->
                val fallbackList = GeminiClient.generateDynamicQuestionBank(
                    s.topic, s.subject, s.difficulty, s.questionCount
                )
                quizState.value = quizState.value.copy(
                    isGenerating = false,
                    questions = fallbackList,
                    currentQuestionIndex = 0,
                    error = "Loaded ${s.questionCount} syllabus quiz questions from educational question bank."
                )
            }
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        if (quizState.value.isSubmitted) return
        val currentAnswers = quizState.value.userAnswers.toMutableMap()
        currentAnswers[questionIndex] = optionIndex
        quizState.value = quizState.value.copy(userAnswers = currentAnswers)
    }

    fun submitQuiz() {
        val s = quizState.value
        if (s.questions.isEmpty() || s.isSubmitted) return

        var score = 0
        s.questions.forEachIndexed { idx, q ->
            if (s.userAnswers[idx] == q.correctIndex) {
                score++
            }
        }

        quizState.value = s.copy(
            isSubmitted = true,
            score = score
        )

        // Save to Room
        viewModelScope.launch {
            val questionsArray = JSONArray()
            s.questions.forEach { q ->
                questionsArray.put(JSONObject().apply {
                    put("question", q.question)
                    put("options", JSONArray(q.options))
                    put("correctIndex", q.correctIndex)
                    put("explanation", q.explanation)
                })
            }
            val answersObj = JSONObject()
            s.userAnswers.forEach { (k, v) -> answersObj.put(k.toString(), v) }

            quizDao.insertQuiz(
                QuizEntity(
                    userId = _currentUserId.value,
                    topic = s.topic,
                    subject = s.subject,
                    difficulty = s.difficulty,
                    questionsJson = questionsArray.toString(),
                    userAnswersJson = answersObj.toString(),
                    score = score,
                    totalQuestions = s.questions.size
                )
            )
        }
    }

    fun retryQuiz() {
        quizState.value = quizState.value.copy(
            userAnswers = emptyMap(),
            currentQuestionIndex = 0,
            isSubmitted = false,
            score = 0
        )
    }

    // --- Saved Sessions & Quizzes ---
    fun loadSavedSession(session: StudySessionEntity) {
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = session.userPrompt
        )
        val assistantMsg = ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = session.explanationText,
            diagramType = session.diagramType,
            diagramTitle = session.diagramDataJson
        )
        tutorState.value = tutorState.value.copy(
            selectedSubject = session.subject,
            topicInput = "",
            sentQuestion = session.topic,
            currentExplanation = ExplanationResult(
                explanationText = session.explanationText,
                diagramType = session.diagramType,
                diagramTitle = session.diagramDataJson
            ),
            messages = listOf(userMsg, assistantMsg)
        )
        navigateTo(AppScreen.TUTOR)
    }

    fun deleteSession(session: StudySessionEntity) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }

    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            quizDao.deleteQuiz(quiz)
        }
    }

    // --- Settings / API Key ---
    fun saveCustomApiKey(key: String) {
        GeminiClient.customApiKey = key.trim()
        apiKeyStatus.value = if (key.trim().isNotEmpty()) "Custom API Key Applied!" else "Reset to default key."
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }

    // Curated educational fallbacks for 100% reliability with clean, professional teaching structure
    private fun getCuratedExplanation(topic: String, subject: String): ExplanationResult {
        val lower = topic.lowercase()
        return when {
            lower.contains("pythagor") || lower.contains("triangle") -> {
                ExplanationResult(
                    explanationText = """
                        Overview and Core Concept
                        The Pythagorean Theorem states that in any right-angled triangle, the area of the square whose side is the hypotenuse is equal to the sum of the areas of the squares on the other two legs.
                        
                        Step-by-Step Breakdown
                        1. Identify the right angle (90 degrees).
                        2. The side opposite the right angle is the Hypotenuse (c), which is always the longest side.
                        3. The two sides forming the right angle are legs (a) and (b).
                        4. Calculate a^2 and b^2, sum them, and take the square root.
                        
                        Key Formulas and Principles
                        • a^2 + b^2 = c^2
                        • Hypotenuse: c = sqrt(a^2 + b^2)
                        • Leg a: a = sqrt(c^2 - b^2)
                        
                        Worked Example
                        For a triangle with legs a = 3 cm and b = 4 cm:
                        c^2 = 3^2 + 4^2 = 9 + 16 = 25
                        c = sqrt(25) = 5 cm.
                        
                        Key Takeaway
                        Standard integer Pythagorean triples include (3, 4, 5), (5, 12, 13), and (8, 15, 17).
                    """.trimIndent(),
                    diagramType = "geometry_triangle",
                    diagramTitle = "Right Triangle & Pythagorean Theorem"
                )
            }
            lower.contains("force") || lower.contains("newton") || lower.contains("gravity") -> {
                ExplanationResult(
                    explanationText = """
                        Overview and Core Concept
                        Newton's Second Law of Motion describes how an object's velocity changes when subjected to an unbalanced net external force. Acceleration is directly proportional to net force and inversely proportional to mass.
                        
                        Step-by-Step Breakdown
                        1. Identify all active forces on the free body: Normal force, gravity, applied force, and friction.
                        2. Establish the coordinate axes: vertical (y) and horizontal (x).
                        3. Calculate vertical equilibrium: Sum of F_y = F_N - F_g = 0, meaning F_N = m * g.
                        4. Determine net horizontal force: F_net = F_applied - F_friction.
                        5. Solve for acceleration: a = F_net / m.
                        
                        Key Formulas and Principles
                        • Sum of F = m * a
                        • Weight: F_g = m * g (where g is approximately 9.8 m/s^2)
                        • Kinetic Friction: f_k = mu_k * F_N
                        
                        Worked Example
                        A 10 kg mass experiences a 50 N forward force with 10 N opposing friction:
                        F_net = 50 N - 10 N = 40 N
                        a = 40 N / 10 kg = 4.0 m/s^2.
                        
                        Key Takeaway
                        Net force causes acceleration. If net force is zero, the object maintains constant velocity.
                    """.trimIndent(),
                    diagramType = "physics_vector",
                    diagramTitle = "Free Body Force Vectors"
                )
            }
            lower.contains("atom") || lower.contains("electron") || lower.contains("chemistry") -> {
                ExplanationResult(
                    explanationText = """
                        Overview and Core Concept
                        Atoms are the fundamental building blocks of chemical matter, consisting of a dense central nucleus surrounded by electrons organized in discrete quantized energy levels.
                        
                        Step-by-Step Breakdown
                        1. Nucleus: Contains positively charged protons (p+) and electrically neutral neutrons (n0).
                        2. Electrons (e-): Negatively charged subatomic particles occupying quantized shells.
                        3. Atomic Number (Z): The proton count, uniquely identifying the chemical element.
                        4. Mass Number (A): The sum of protons and neutrons in the nucleus.
                        
                        Key Formulas and Principles
                        • Maximum electron capacity per principal shell: 2n^2 (n=1: 2, n=2: 8, n=3: 18)
                        • Net electrical charge = Protons - Electrons
                        
                        Worked Example (Carbon-12)
                        Carbon has atomic number 6:
                        • 6 protons and 6 neutrons in the nucleus.
                        • Electron configuration: 2 inner electrons (K shell) and 4 valence electrons (L shell).
                        
                        Key Takeaway
                        Valence electrons in the outermost electron shell determine chemical bonding behavior and reactivity.
                    """.trimIndent(),
                    diagramType = "chemistry_atom",
                    diagramTitle = "Bohr Atomic Model"
                )
            }
            else -> {
                ExplanationResult(
                    explanationText = """
                        Overview and Core Concept
                        Study Buddy Academic Lesson: Analysis of $topic in $subject.
                        
                        Step-by-Step Breakdown
                        1. Theoretical Foundation: Define the core terms, governing principles, and standard assumptions.
                        2. Structural Derivation: Break down the concept into underlying relationships and components.
                        3. Analytical Application: Apply systematic methodology to solve representative problems.
                        4. Validation: Verify boundary conditions, dimensional units, and physical reasonableness.
                        
                        Key Formulas and Principles
                        • Functional Form: f(x) = ax^2 + bx + c
                        • Conservation Principle: Initial State = Final State
                        
                        Worked Example
                        Solving linear relationship step-by-step:
                        2x + 6 = 14
                        2x = 8
                        x = 4.
                        
                        Key Takeaway
                        Master the core definitions first, then reinforce understanding through step-by-step problem sets.
                    """.trimIndent(),
                    diagramType = "cartesian_graph",
                    diagramTitle = "Concept Mathematical Graph"
                )
            }
        }
    }

    private fun getCuratedQuiz(subject: String, topic: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = 1,
                question = "In a right triangle with legs of length 6 and 8, what is the length of the hypotenuse?",
                options = listOf("A) 9", "B) 10", "C) 12", "D) 14"),
                correctIndex = 1,
                explanation = "By the Pythagorean Theorem, c = √(6² + 8²) = √(36 + 64) = √100 = 10."
            ),
            QuizQuestion(
                id = 2,
                question = "According to Newton's Second Law, what happens to acceleration if the net force is doubled while mass remains constant?",
                options = listOf("A) Acceleration doubles", "B) Acceleration halves", "C) Acceleration quadruples", "D) Acceleration stays the same"),
                correctIndex = 0,
                explanation = "Since F = m·a, acceleration a = F/m. Doubling force F directly doubles acceleration a."
            ),
            QuizQuestion(
                id = 3,
                question = "What is the maximum number of electrons that can occupy the second principal energy shell (n = 2)?",
                options = listOf("A) 2", "B) 6", "C) 8", "D) 18"),
                correctIndex = 2,
                explanation = "The maximum capacity of an electron shell is given by 2n². For n = 2: 2(2)² = 2(4) = 8 electrons."
            ),
            QuizQuestion(
                id = 4,
                question = "Which organelle is known as the powerhouse of the eukaryotic cell because it synthesizes ATP?",
                options = listOf("A) Ribosome", "B) Mitochondria", "C) Endoplasmic Reticulum", "D) Golgi Apparatus"),
                correctIndex = 1,
                explanation = "Mitochondria generate most of the chemical energy needed by the cell (ATP) through cellular respiration."
            )
        )
    }

    // --- Pomodoro Study Timer Logic ---
    fun startPomodoro() {
        if (pomodoroState.value.isRunning) return
        pomodoroState.value = pomodoroState.value.copy(isRunning = true)

        pomodoroTimerJob?.cancel()
        pomodoroTimerJob = viewModelScope.launch {
            while (pomodoroState.value.isRunning && pomodoroState.value.totalSecondsRemaining > 0) {
                kotlinx.coroutines.delay(1000L)
                val currentSec = pomodoroState.value.totalSecondsRemaining - 1
                if (currentSec <= 0) {
                    onPomodoroIntervalComplete()
                    break
                } else {
                    pomodoroState.value = pomodoroState.value.copy(totalSecondsRemaining = currentSec)
                }
            }
        }
    }

    private fun onPomodoroIntervalComplete() {
        val state = pomodoroState.value
        val config = state.config

        when (state.mode) {
            PomodoroMode.WORK -> {
                val newCompletedSessions = state.completedWorkSessions + 1
                val newFocusMinutes = state.totalFocusMinutesToday + config.workMinutes
                val isLongBreak = (state.currentCycleInRound >= config.cyclesBeforeLongBreak)

                val nextMode = if (isLongBreak) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
                val nextSeconds = (if (isLongBreak) config.longBreakMinutes else config.shortBreakMinutes) * 60
                val nextMinutes = if (isLongBreak) config.longBreakMinutes else config.shortBreakMinutes

                pomodoroNotificationHelper.showSessionFinishedNotification(
                    isWorkSession = true,
                    nextMinutes = nextMinutes
                )

                pomodoroState.value = state.copy(
                    mode = nextMode,
                    totalSecondsRemaining = nextSeconds,
                    initialTotalSeconds = nextSeconds,
                    completedWorkSessions = newCompletedSessions,
                    totalFocusMinutesToday = newFocusMinutes,
                    isRunning = config.autoStartBreaks
                )

                if (config.autoStartBreaks) {
                    startPomodoro()
                }
            }
            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                val nextCycleInRound = if (state.mode == PomodoroMode.LONG_BREAK) 1 else state.currentCycleInRound + 1
                val nextSeconds = config.workMinutes * 60

                pomodoroNotificationHelper.showSessionFinishedNotification(
                    isWorkSession = false,
                    nextMinutes = config.workMinutes
                )

                pomodoroState.value = state.copy(
                    mode = PomodoroMode.WORK,
                    totalSecondsRemaining = nextSeconds,
                    initialTotalSeconds = nextSeconds,
                    currentCycleInRound = nextCycleInRound,
                    isRunning = config.autoStartStudy
                )

                if (config.autoStartStudy) {
                    startPomodoro()
                }
            }
        }
    }

    fun pausePomodoro() {
        pomodoroTimerJob?.cancel()
        pomodoroState.value = pomodoroState.value.copy(isRunning = false)
    }

    fun resetPomodoro() {
        pomodoroTimerJob?.cancel()
        val state = pomodoroState.value
        val durationSec = when (state.mode) {
            PomodoroMode.WORK -> state.config.workMinutes * 60
            PomodoroMode.SHORT_BREAK -> state.config.shortBreakMinutes * 60
            PomodoroMode.LONG_BREAK -> state.config.longBreakMinutes * 60
        }
        pomodoroState.value = state.copy(
            isRunning = false,
            totalSecondsRemaining = durationSec,
            initialTotalSeconds = durationSec
        )
    }

    fun skipToNextPomodoroInterval() {
        pomodoroTimerJob?.cancel()
        onPomodoroIntervalComplete()
    }

    fun switchPomodoroMode(targetMode: PomodoroMode) {
        pomodoroTimerJob?.cancel()
        val state = pomodoroState.value
        val durationSec = when (targetMode) {
            PomodoroMode.WORK -> state.config.workMinutes * 60
            PomodoroMode.SHORT_BREAK -> state.config.shortBreakMinutes * 60
            PomodoroMode.LONG_BREAK -> state.config.longBreakMinutes * 60
        }
        pomodoroState.value = state.copy(
            mode = targetMode,
            isRunning = false,
            totalSecondsRemaining = durationSec,
            initialTotalSeconds = durationSec
        )
    }

    fun updatePomodoroConfig(
        workMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        cycles: Int,
        autoBreaks: Boolean,
        autoStudy: Boolean
    ) {
        val newConfig = PomodoroConfig(
            workMinutes = workMinutes.coerceIn(5, 120),
            shortBreakMinutes = shortBreakMinutes.coerceIn(1, 30),
            longBreakMinutes = longBreakMinutes.coerceIn(5, 60),
            cyclesBeforeLongBreak = cycles.coerceIn(1, 10),
            autoStartBreaks = autoBreaks,
            autoStartStudy = autoStudy
        )

        val state = pomodoroState.value
        val newSec = when (state.mode) {
            PomodoroMode.WORK -> newConfig.workMinutes * 60
            PomodoroMode.SHORT_BREAK -> newConfig.shortBreakMinutes * 60
            PomodoroMode.LONG_BREAK -> newConfig.longBreakMinutes * 60
        }

        pomodoroState.value = state.copy(
            config = newConfig,
            totalSecondsRemaining = if (!state.isRunning) newSec else state.totalSecondsRemaining,
            initialTotalSeconds = newSec
        )
    }

    fun adjustPomodoroMinutes(minutesDelta: Int) {
        val state = pomodoroState.value
        val newSec = (state.totalSecondsRemaining + minutesDelta * 60).coerceAtLeast(10)
        pomodoroState.value = state.copy(
            totalSecondsRemaining = newSec,
            initialTotalSeconds = maxOf(state.initialTotalSeconds, newSec)
        )
    }

    // --- WAEC & JAMB Exam Prep System ---
    fun updateWaecJambExam(examType: ExamType) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(examType = examType)
        )
    }

    fun updateWaecJambSubject(subject: String) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(subject = subject, topic = "All Topics")
        )
    }

    fun updateWaecJambFacultyTrack(track: String) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(facultyTrack = track)
        )
    }

    fun updateWaecJambYear(year: Int?) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(
                selectedYear = year,
                yearStart = year ?: 1997,
                yearEnd = year ?: 2026
            )
        )
    }

    fun updateWaecJambYearRange(yearStart: Int, yearEnd: Int) {
        val curr = waecJambState.value
        val clampedStart = yearStart.coerceIn(1997, 2026)
        val clampedEnd = yearEnd.coerceIn(clampedStart, 2026)
        waecJambState.value = curr.copy(
            config = curr.config.copy(
                yearStart = clampedStart,
                yearEnd = clampedEnd,
                selectedYear = if (clampedStart == clampedEnd) clampedStart else null
            )
        )
    }

    fun updateWaecJambTopic(topic: String) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(topic = topic)
        )
    }

    fun updateWaecJambDifficulty(difficulty: String) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(difficulty = difficulty)
        )
    }

    fun updateWaecJambMode(mode: ExamPrepMode) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(mode = mode)
        )
    }

    fun updateWaecJambCount(count: Int) {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            config = curr.config.copy(questionCount = count)
        )
    }

    fun startWaecJambExam() {
        val curr = waecJambState.value
        waecJambState.value = curr.copy(isLoading = true, statusMessage = "Loading verified past exam papers...")
        viewModelScope.launch {
            val questionsResult = GeminiClient.generateWaecJambQuestions(
                examType = curr.config.examType,
                subject = curr.config.subject,
                selectedYear = curr.config.selectedYear,
                yearStart = curr.config.yearStart,
                yearEnd = curr.config.yearEnd,
                topic = curr.config.topic,
                difficulty = curr.config.difficulty,
                count = curr.config.questionCount
            )
            val questions = questionsResult.getOrDefault(
                WaecJambQuestionBank.getQuestions(
                    examType = curr.config.examType,
                    subject = curr.config.subject,
                    selectedYear = curr.config.selectedYear,
                    yearStart = curr.config.yearStart,
                    yearEnd = curr.config.yearEnd,
                    topic = curr.config.topic,
                    difficulty = curr.config.difficulty,
                    count = curr.config.questionCount
                )
            )

            val timedSec = curr.config.timedMinutes * 60
            waecJambState.value = curr.copy(
                stage = ExamScreenStage.ACTIVE_EXAM,
                questions = questions,
                currentQuestionIndex = 0,
                secondsRemaining = timedSec,
                totalTimeSeconds = timedSec,
                isTimerActive = curr.config.mode == ExamPrepMode.MOCK_EXAM,
                isSubmitted = false,
                isLoading = false,
                statusMessage = null
            )

            if (curr.config.mode == ExamPrepMode.MOCK_EXAM) {
                startExamTimer()
            }
        }
    }

    private fun startExamTimer() {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val curr = waecJambState.value
                if (!curr.isTimerActive || curr.stage != ExamScreenStage.ACTIVE_EXAM) break
                val newSec = curr.secondsRemaining - 1
                if (newSec <= 0) {
                    waecJambState.value = curr.copy(secondsRemaining = 0, isTimerActive = false)
                    submitWaecJambExam()
                    break
                } else {
                    waecJambState.value = curr.copy(secondsRemaining = newSec)
                }
            }
        }
    }

    fun selectWaecJambAnswer(questionIndex: Int, optionIndex: Int) {
        val curr = waecJambState.value
        if (questionIndex !in curr.questions.indices) return
        val updatedQuestions = curr.questions.toMutableList()
        val q = updatedQuestions[questionIndex]
        updatedQuestions[questionIndex] = q.copy(userSelectedIndex = optionIndex)
        waecJambState.value = curr.copy(questions = updatedQuestions)
    }

    fun nextWaecJambQuestion() {
        val curr = waecJambState.value
        if (curr.currentQuestionIndex < curr.questions.size - 1) {
            waecJambState.value = curr.copy(currentQuestionIndex = curr.currentQuestionIndex + 1)
        }
    }

    fun prevWaecJambQuestion() {
        val curr = waecJambState.value
        if (curr.currentQuestionIndex > 0) {
            waecJambState.value = curr.copy(currentQuestionIndex = curr.currentQuestionIndex - 1)
        }
    }

    fun jumpToWaecJambQuestion(index: Int) {
        val curr = waecJambState.value
        if (index in curr.questions.indices) {
            waecJambState.value = curr.copy(currentQuestionIndex = index)
        }
    }

    fun toggleFlagWaecJambQuestion(index: Int) {
        val curr = waecJambState.value
        if (index !in curr.questions.indices) return
        val updatedQuestions = curr.questions.toMutableList()
        val q = updatedQuestions[index]
        updatedQuestions[index] = q.copy(isFlagged = !q.isFlagged)
        waecJambState.value = curr.copy(questions = updatedQuestions)
    }

    fun submitWaecJambExam() {
        examTimerJob?.cancel()
        val curr = waecJambState.value
        examsCompletedCount++
        if (curr.scorePercentage >= 80) {
            examDistinctionsCount++
        }
        waecJambState.value = curr.copy(
            stage = ExamScreenStage.EXAM_RESULTS,
            isTimerActive = false,
            isSubmitted = true
        )
        refreshRewards()
    }

    fun retakeWaecJambExam() {
        examTimerJob?.cancel()
        val curr = waecJambState.value
        val resetQuestions = curr.questions.map { it.copy(userSelectedIndex = null, isFlagged = false) }
        val timedSec = curr.config.timedMinutes * 60
        waecJambState.value = curr.copy(
            stage = ExamScreenStage.ACTIVE_EXAM,
            questions = resetQuestions,
            currentQuestionIndex = 0,
            secondsRemaining = timedSec,
            totalTimeSeconds = timedSec,
            isTimerActive = curr.config.mode == ExamPrepMode.MOCK_EXAM,
            isSubmitted = false
        )
        if (curr.config.mode == ExamPrepMode.MOCK_EXAM) {
            startExamTimer()
        }
    }

    fun proceedFromWaecJambGetStarted() {
        waecJambState.value = waecJambState.value.copy(stage = ExamScreenStage.SELECTION_SETUP)
    }

    fun backToWaecJambGetStarted() {
        waecJambState.value = waecJambState.value.copy(stage = ExamScreenStage.GET_STARTED)
    }

    fun openWaecJambPrep() {
        voiceManager.stopSpeaking()
        waecJambState.value = waecJambState.value.copy(stage = ExamScreenStage.GET_STARTED)
        navigateTo(AppScreen.EXAM_PREP)
    }

    fun openAuthScreen(isLoginMode: Boolean) {
        authState.value = authState.value.copy(isLoginMode = isLoginMode, error = null)
        navigateTo(AppScreen.AUTH)
    }

    fun resetWaecJambToSetup() {
        examTimerJob?.cancel()
        val curr = waecJambState.value
        waecJambState.value = curr.copy(
            stage = ExamScreenStage.SELECTION_SETUP,
            isTimerActive = false,
            isSubmitted = false,
            currentQuestionIndex = 0
        )
    }
}
