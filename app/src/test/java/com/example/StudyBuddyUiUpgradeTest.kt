package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.data.api.StudyBuddyModelTier
import com.example.data.local.UserEntity
import com.example.data.model.ExamPrepMode
import com.example.data.model.ExamScreenStage
import com.example.data.model.ExamType
import com.example.data.model.WaecJambConfig
import com.example.data.model.WaecJambQuestion
import com.example.data.model.WaecJambState
import com.example.data.repository.WaecJambQuestionBank
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.screens.AssignmentHelperScreen
import com.example.ui.screens.PastQuestionSearchView
import com.example.ui.screens.PomodoroTimerScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TextbookLibraryScreen
import com.example.ui.screens.WaecHubSubView
import com.example.ui.screens.WaecJambScreen
import com.example.ui.screens.WaecJambUnifiedBottomNav
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PomodoroState
import com.example.viewmodel.TutorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyBuddyUiUpgradeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h780dp")
    fun modelSelectorDialogDisplaysExactThreeModelsAndSelectsTier() {
        var selected = StudyBuddyModelTier.FREE
        var dismissed = false

        composeRule.setContent {
            MyApplicationTheme {
                ModelSelectorDialog(
                    selectedTier = selected,
                    onSelectTier = { selected = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("model_selector_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("model_tier_studybuddy_free").assertIsDisplayed()
        composeRule.onNodeWithTag("model_tier_studybuddy_plus").assertIsDisplayed()
        composeRule.onNodeWithTag("model_tier_studybuddy_max").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithText("StudyBuddy Free").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("StudyBuddy Plus").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("StudyBuddy Max").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("model_tier_studybuddy_plus").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(StudyBuddyModelTier.PLUS, selected)
        assertTrue(dismissed)
    }

    @Test
    fun settingsDialogHidesDeveloperApiKeyControlsFromNormalUsers() {
        var logoutClicked = false
        val user = UserEntity(
            id = 1L,
            name = "Adaobi Okafor",
            email = "adaobi@studybuddy.test",
            grade = "SS3"
        )

        composeRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    currentUser = user,
                    onDismiss = {},
                    onLogout = { logoutClicked = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_api_key_input").assertDoesNotExist()
        composeRule.onNodeWithTag("open_api_management_button").assertDoesNotExist()
        composeRule.onNodeWithText("AI Studio Secrets & Provider Keys").assertDoesNotExist()

        composeRule.onNodeWithText("Adaobi Okafor").assertIsDisplayed()
        composeRule.onNodeWithText("Nigerian Male").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Nigerian Female").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("settings_logout_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(logoutClicked)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp")
    fun assignmentHelperCompactPhoneOpensModelSelectorFromAvatar() {
        var selectedTier = StudyBuddyModelTier.FREE

        composeRule.setContent {
            MyApplicationTheme {
                AssignmentHelperScreen(
                    state = TutorState(),
                    isSpeaking = false,
                    isListening = false,
                    onSelectSubject = {},
                    onTopicChange = {},
                    onSendChatMessage = {},
                    onStartVoice = {},
                    onStopVoice = {},
                    onPlayAnswer = {},
                    onStopAnswerVoice = {},
                    onClearChat = {},
                    onOpenDrawer = {},
                    onScanHomeworkShortcut = {},
                    onQuizShortcut = {},
                    onLikeMessage = { _, _ -> },
                    selectedModelTier = selectedTier,
                    onSelectModelTier = { selectedTier = it }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("active_model_indicator_pill").assertDoesNotExist()
        composeRule.onNodeWithTag("chat_input_bot_image").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("model_selector_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("model_tier_studybuddy_max").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(StudyBuddyModelTier.MAX, selectedTier)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp")
    fun waecJambExamFitsOn320dpPhoneWithoutClipping() {
        var submitted = false

        val sampleQuestion = WaecJambQuestion(
            id = "q-math-1",
            exam = ExamType.WAEC,
            year = 2024,
            session = "May/June",
            subject = "Mathematics",
            topic = "Quadratic Equations",
            subtopic = "Factorization",
            difficulty = "Medium",
            question = "If x^2 - 5x + 6 = 0, what are the values of x?",
            options = listOf("2 and 3", "-2 and -3", "1 and 6", "-1 and 6"),
            correctIndex = 0,
            explanation = "(x - 2)(x - 3) = 0, so x = 2 or x = 3."
        )

        val examState = WaecJambState(
            stage = ExamScreenStage.ACTIVE_EXAM,
            config = WaecJambConfig(
                examType = ExamType.WAEC,
                subject = "Mathematics",
                selectedSubjects = listOf("Mathematics", "English Language", "Physics", "Chemistry"),
                mode = ExamPrepMode.PRACTICE
            ),
            questions = listOf(sampleQuestion, sampleQuestion.copy(id = "q-math-2")),
            currentQuestionIndex = 0
        )

        composeRule.setContent {
            MyApplicationTheme {
                WaecJambScreen(
                    state = examState,
                    isSpeaking = false,
                    onProceedFromGetStarted = {},
                    onBackToGetStarted = {},
                    onExitExamPrep = {},
                    onSelectExam = {},
                    onSelectSubject = {},
                    onSelectYear = {},
                    onSelectTopic = {},
                    onSelectDifficulty = {},
                    onSelectMode = {},
                    onSelectQuestionCount = {},
                    onStartExam = {},
                    onSelectAnswer = { _, _ -> },
                    onNextQuestion = {},
                    onPrevQuestion = {},
                    onJumpToQuestion = {},
                    onToggleFlagQuestion = {},
                    onSubmitExam = { submitted = true },
                    onRetakeExam = {},
                    onResetToSetup = {},
                    onPlayAudio = {},
                    onStopAudio = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("next_exam_question_button").assertIsDisplayed()
        composeRule.onNodeWithTag("submit_exam_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(submitted)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp")
    fun waecJambBottomNavFitsOn320dpPhone() {
        var navigatedStudy = false

        composeRule.setContent {
            MyApplicationTheme {
                WaecJambUnifiedBottomNav(
                    activeSubView = WaecHubSubView.WAEC_HUB,
                    onNavigateToAi = {},
                    onNavigateToStudy = { navigatedStudy = true },
                    onNavigateToWaec = {},
                    onNavigateToJamb = {},
                    onNavigateToStudyNote = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("waec_jamb_unified_bottom_nav").assertIsDisplayed()
        composeRule.onNodeWithText("Study").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(navigatedStudy)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h1000dp")
    fun pomodoroTimerRendersOnTabletLayout() {
        var togglePlayed = false

        composeRule.setContent {
            MyApplicationTheme {
                PomodoroTimerScreen(
                    state = PomodoroState(),
                    onStart = { togglePlayed = true },
                    onPause = {},
                    onReset = {},
                    onSkip = {},
                    onSwitchMode = {},
                    onUpdateConfig = { _, _, _, _, _, _ -> },
                    onAdjustMinutes = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pomodoro_reset_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("pomodoro_skip_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("pomodoro_toggle_play_button").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(togglePlayed)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h780dp")
    fun textbookLibraryHidesWithDiagramsBadge() {
        composeRule.setContent {
            MyApplicationTheme {
                TextbookLibraryScreen(
                    onBack = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Textbook & Subject Library").assertIsDisplayed()
        composeRule.onNodeWithText("WITH DIAGRAMS").assertDoesNotExist()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h780dp")
    fun waecJambSearchEngineReturnsMultiYearQuestionsAndExplanations() {
        val waecPhysics = WaecJambQuestionBank.searchPastQuestions(
            query = "Physics",
            examFilter = ExamType.WAEC
        )
        assertTrue(waecPhysics.isNotEmpty())
        assertTrue(waecPhysics.map { it.year }.distinct().size >= 3)
        assertTrue(waecPhysics.all { it.explanation.isNotBlank() && it.exam == ExamType.WAEC })

        val jambMath = WaecJambQuestionBank.searchPastQuestions(
            query = "Mathematics",
            examFilter = ExamType.JAMB
        )
        assertTrue(jambMath.isNotEmpty())
        assertTrue(jambMath.map { it.year }.distinct().size >= 3)
        assertTrue(jambMath.all { it.explanation.isNotBlank() && it.exam == ExamType.JAMB })

        composeRule.setContent {
            MyApplicationTheme {
                PastQuestionSearchView(
                    initialQuery = "",
                    initialExamFilter = ExamType.WAEC,
                    onBack = {},
                    onPlayAudio = {},
                    onStopAudio = {},
                    onPracticeTopic = { _, _, _ -> }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Past Question Search Engine").assertIsDisplayed()
        composeRule.onNodeWithTag("waec_jamb_question_search_input")
            .performTextReplacement("Physics")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("past_question_search_count").assertIsDisplayed()
        composeRule.onNodeWithTag("past_question_search_results_list").assertIsDisplayed()
        composeRule.onAllNodesWithText("Step-by-Step Explanation (${waecPhysics.first().correctAnswerLetter})")[0]
            .assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h780dp")
    fun waecStudyHubOpensQuestionSearchBar() {
        val setupState = WaecJambState(
            stage = ExamScreenStage.SELECTION_SETUP,
            config = WaecJambConfig(examType = ExamType.WAEC)
        )

        composeRule.setContent {
            MyApplicationTheme {
                WaecJambScreen(
                    state = setupState,
                    isSpeaking = false,
                    onProceedFromGetStarted = {},
                    onBackToGetStarted = {},
                    onExitExamPrep = {},
                    onSelectExam = {},
                    onSelectSubject = {},
                    onSelectYear = {},
                    onSelectTopic = {},
                    onSelectDifficulty = {},
                    onSelectMode = {},
                    onSelectQuestionCount = {},
                    onStartExam = {},
                    onSelectAnswer = { _, _ -> },
                    onNextQuestion = {},
                    onPrevQuestion = {},
                    onJumpToQuestion = {},
                    onToggleFlagQuestion = {},
                    onSubmitExam = {},
                    onRetakeExam = {},
                    onResetToSetup = {},
                    onPlayAudio = {},
                    onStopAudio = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("waec_hub_search_bar").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Past Question Search Engine").assertIsDisplayed()
    }
}
