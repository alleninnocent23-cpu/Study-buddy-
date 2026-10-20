package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AssignmentHelperScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeworkScannerScreen
import com.example.ui.screens.QuizGeneratorScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.AppScreen
import com.example.viewmodel.StudyBuddyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudyBuddyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                StudyBuddyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudyBuddyApp(viewModel: StudyBuddyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val authState by viewModel.authState.collectAsState()
    val tutorState by viewModel.tutorState.collectAsState()
    val scannerState by viewModel.scannerState.collectAsState()
    val quizState by viewModel.quizState.collectAsState()

    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()

    val savedSessions by viewModel.savedSessions.collectAsState()
    val savedQuizzes by viewModel.savedQuizzes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val attachedImage by viewModel.attachedImage.collectAsState()
    val attachedFileName by viewModel.attachedFileName.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Android Back Handler
    BackHandler(enabled = currentScreen != AppScreen.WELCOME) {
        when (currentScreen) {
            AppScreen.AUTH -> viewModel.navigateTo(AppScreen.WELCOME)
            AppScreen.SCANNER, AppScreen.QUIZ, AppScreen.HISTORY -> viewModel.navigateTo(AppScreen.TUTOR)
            AppScreen.TUTOR -> {
                // If on home tutor, let user press back to exit or go to welcome
                viewModel.navigateTo(AppScreen.WELCOME)
            }
            AppScreen.WELCOME -> {}
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUser = currentUser,
            onDismiss = { showSettingsDialog = false },
            onSaveApiKey = { viewModel.saveCustomApiKey(it) },
            onLogout = {
                showSettingsDialog = false
                viewModel.logout()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack),
        topBar = {
            if (currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.AUTH) {
                StudyBuddyTopBar(
                    studentName = currentUser?.name ?: "Student",
                    grade = currentUser?.grade ?: "High School",
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.AUTH) {
                StudyBuddyBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.WELCOME -> {
                    WelcomeScreen(
                        onGetStarted = { viewModel.navigateTo(AppScreen.AUTH) }
                    )
                }

                AppScreen.AUTH -> {
                    AuthScreen(
                        state = authState,
                        onToggleMode = { viewModel.toggleAuthMode() },
                        onUpdateFields = { n, e, g -> viewModel.updateAuthField(n, e, g) },
                        onSubmit = { viewModel.authenticate() },
                        onDemoLogin = { viewModel.continueAsDemoStudent() },
                        onGoogleLogin = { viewModel.loginWithGoogle(context) },
                        onFacebookLogin = { viewModel.loginWithFacebook(activity) },
                        onBack = { viewModel.navigateTo(AppScreen.WELCOME) }
                    )
                }

                AppScreen.TUTOR -> {
                    AssignmentHelperScreen(
                        state = tutorState,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        attachedImage = attachedImage,
                        attachedFileName = attachedFileName,
                        onSelectSubject = { viewModel.selectSubject(it) },
                        onTopicChange = { viewModel.updateTopicInput(it) },
                        onExplain = { viewModel.explainCurrentTopic() },
                        onAttachImage = { viewModel.setAttachedImage(it) },
                        onAttachFile = { viewModel.setAttachedFile(it) },
                        onClearAttachment = { viewModel.clearAttachment() },
                        onStartVoice = { viewModel.startVoiceInput() },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onPlayAnswer = { viewModel.playExplanation(it) },
                        onStopAnswerVoice = { viewModel.stopExplanationVoice() }
                    )
                }

                AppScreen.SCANNER -> {
                    HomeworkScannerScreen(
                        state = scannerState,
                        isSpeaking = isSpeaking,
                        onImageSelected = { viewModel.setCapturedHomeworkImage(it) },
                        onClearImage = { viewModel.clearScannedImage() },
                        onScan = { note -> viewModel.analyzeHomeworkImage(note) },
                        onPlayAnswer = { viewModel.playExplanation(it) },
                        onStopAnswerVoice = { viewModel.stopExplanationVoice() }
                    )
                }

                AppScreen.QUIZ -> {
                    QuizGeneratorScreen(
                        state = quizState,
                        onUpdateConfig = { s, t, d -> viewModel.updateQuizConfig(s, t, d) },
                        onGenerateQuiz = { viewModel.generateNewQuiz() },
                        onSelectAnswer = { q, o -> viewModel.selectQuizAnswer(q, o) },
                        onSubmitQuiz = { viewModel.submitQuiz() },
                        onRetryQuiz = { viewModel.retryQuiz() }
                    )
                }

                AppScreen.HISTORY -> {
                    HistoryScreen(
                        savedSessions = savedSessions,
                        savedQuizzes = savedQuizzes,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        onSelectSession = { viewModel.loadSavedSession(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onDeleteQuiz = { viewModel.deleteQuiz(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudyBuddyTopBar(
    studentName: String,
    grade: String,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(
                1.dp,
                com.example.ui.theme.BorderSubtle,
                RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            ),
        color = com.example.ui.theme.TrueBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Avatar (Circular ring outline)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .border(1.2.dp, com.example.ui.theme.PureWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.take(1).uppercase(),
                    color = com.example.ui.theme.PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    color = com.example.ui.theme.PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = grade,
                    color = com.example.ui.theme.TextMutedGray,
                    fontSize = 11.sp
                )
            }

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, com.example.ui.theme.BorderLight, CircleShape)
                    .testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = com.example.ui.theme.PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StudyBuddyBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(1.dp, com.example.ui.theme.BorderSubtle, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
        containerColor = com.example.ui.theme.TrueBlack,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(AppScreen.TUTOR, "Tutor", Icons.AutoMirrored.Filled.MenuBook),
            Triple(AppScreen.SCANNER, "Scanner", Icons.Default.DocumentScanner),
            Triple(AppScreen.QUIZ, "Quiz", Icons.Default.Psychology),
            Triple(AppScreen.HISTORY, "History", Icons.Default.History)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.example.ui.theme.TrueBlack,
                    selectedTextColor = com.example.ui.theme.PureWhite,
                    unselectedIconColor = com.example.ui.theme.TextMutedGray,
                    unselectedTextColor = com.example.ui.theme.TextMutedGray,
                    indicatorColor = com.example.ui.theme.PureWhite
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}
