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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GetPlusDialog
import com.example.ui.screens.AssignmentHelperScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeworkScannerScreen
import android.content.Intent
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.screens.PomodoroTimerScreen
import com.example.ui.screens.QuizGeneratorScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptDrawerBg
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.ChatGptPlusTextBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.AppScreen
import com.example.viewmodel.StudyBuddyViewModel
import kotlinx.coroutines.launch

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("OPEN_SCREEN") == "POMODORO") {
            viewModel.navigateTo(AppScreen.POMODORO)
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
    val pomodoroState by viewModel.pomodoroState.collectAsState()

    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val voiceGender by viewModel.voiceGender.collectAsState()

    val savedSessions by viewModel.savedSessions.collectAsState()
    val savedQuizzes by viewModel.savedQuizzes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val attachedImage by viewModel.attachedImage.collectAsState()
    val attachedFileName by viewModel.attachedFileName.collectAsState()

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGetPlusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (activity?.intent?.getStringExtra("OPEN_SCREEN") == "POMODORO") {
            viewModel.navigateTo(AppScreen.POMODORO)
        }
    }

    // Android Back Handler
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.WELCOME) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            when (currentScreen) {
                AppScreen.AUTH -> viewModel.navigateTo(AppScreen.WELCOME)
                AppScreen.SCANNER, AppScreen.QUIZ, AppScreen.HISTORY, AppScreen.POMODORO -> viewModel.navigateTo(AppScreen.TUTOR)
                AppScreen.TUTOR -> {
                    // Back on tutor clears chat if active, else navigates to welcome
                    if (tutorState.messages.isNotEmpty()) {
                        viewModel.clearCurrentChat()
                    } else {
                        viewModel.navigateTo(AppScreen.WELCOME)
                    }
                }
                AppScreen.WELCOME -> {}
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUser = currentUser,
            voiceGender = voiceGender,
            onVoiceGenderChange = { viewModel.setVoiceGender(it) },
            onDismiss = { showSettingsDialog = false },
            onSaveApiKey = { viewModel.saveCustomApiKey(it) },
            onLogout = {
                showSettingsDialog = false
                viewModel.logout()
            }
        )
    }

    if (showGetPlusDialog) {
        GetPlusDialog(onDismiss = { showGetPlusDialog = false })
    }

    // Modal Navigation Drawer (opened by the top-left 2-bar menu button)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.AUTH,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ChatGptDrawerBg,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    // Drawer Top Header: Logo + New Chat Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Buddy",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                viewModel.clearCurrentChat()
                                viewModel.navigateTo(AppScreen.TUTOR)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                                .testTag("drawer_new_chat_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Chat", tint = PureWhite, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Navigation Links
                    DrawerNavItem(
                        title = "Study Buddy Tutor",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        isSelected = currentScreen == AppScreen.TUTOR,
                        onClick = {
                            viewModel.navigateTo(AppScreen.TUTOR)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        title = "Homework Scanner",
                        icon = Icons.Default.CameraAlt,
                        isSelected = currentScreen == AppScreen.SCANNER,
                        onClick = {
                            viewModel.navigateTo(AppScreen.SCANNER)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        title = "Quiz Generator",
                        icon = Icons.Default.Psychology,
                        isSelected = currentScreen == AppScreen.QUIZ,
                        onClick = {
                            viewModel.navigateTo(AppScreen.QUIZ)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        title = "Saved Sessions & History",
                        icon = Icons.Default.History,
                        isSelected = currentScreen == AppScreen.HISTORY,
                        onClick = {
                            viewModel.navigateTo(AppScreen.HISTORY)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerNavItem(
                        title = "Pomodoro Study Timer",
                        icon = Icons.Default.HourglassBottom,
                        isSelected = currentScreen == AppScreen.POMODORO,
                        onClick = {
                            viewModel.navigateTo(AppScreen.POMODORO)
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Persona Selector in Menu Section
                    Text(
                        text = "Voice Persona (Nigerian English)",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isMale = voiceGender == com.example.util.VoiceGender.MALE
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setVoiceGender(com.example.util.VoiceGender.MALE) },
                            color = if (isMale) PureWhite else TrueBlack
                        ) {
                            Text(
                                text = "👨 Male (Clear)",
                                color = if (isMale) TrueBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (!isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setVoiceGender(com.example.util.VoiceGender.FEMALE) },
                            color = if (!isMale) PureWhite else TrueBlack
                        ) {
                            Text(
                                text = "👩 Female",
                                color = if (!isMale) TrueBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Recent Chats Section
                    Text(
                        text = "Recent Study Chats",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (savedSessions.isEmpty()) {
                            item {
                                Text(
                                    text = "No saved study sessions yet",
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            items(savedSessions.take(15)) { session ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.loadSavedSession(session)
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = session.topic,
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Upgrade to Plus Banner in Drawer
                    Surface(
                        onClick = {
                            showGetPlusDialog = true
                            scope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = ChatGptPillDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ChatGptPlusStarBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Upgrade to Plus", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Unlimited scans & diagrams", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Student Profile & Settings Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showSettingsDialog = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PureWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.name ?: "S").take(1).uppercase(),
                                color = TrueBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Alex Vance",
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = currentUser?.grade ?: "Grade 11 STEM",
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextMutedGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TrueBlack)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.fillMaxSize(),
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
                        // The Primary ChatGPT Interface
                        AssignmentHelperScreen(
                            state = tutorState,
                            isSpeaking = isSpeaking,
                            isListening = isListening,
                            attachedImage = attachedImage,
                            attachedFileName = attachedFileName,
                            onSelectSubject = { viewModel.selectSubject(it) },
                            onTopicChange = { viewModel.updateTopicInput(it) },
                            onSendChatMessage = { prompt ->
                                viewModel.sendChatMessage(prompt)
                            },
                            onAttachImage = { viewModel.setAttachedImage(it) },
                            onAttachFile = { viewModel.setAttachedFile(it) },
                            onClearAttachment = { viewModel.clearAttachment() },
                            onStartVoice = { viewModel.startVoiceInput() },
                            onStopVoice = { viewModel.stopVoiceInput() },
                            onPlayAnswer = { viewModel.playExplanation(it) },
                            onStopAnswerVoice = { viewModel.stopExplanationVoice() },
                            onClearChat = { viewModel.clearCurrentChat() },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },
                            onScanHomeworkShortcut = { viewModel.navigateTo(AppScreen.SCANNER) },
                            onQuizShortcut = { viewModel.navigateTo(AppScreen.QUIZ) },
                            onPomodoroShortcut = { viewModel.navigateTo(AppScreen.POMODORO) },
                            onLikeMessage = { id, liked -> viewModel.toggleLikeMessage(id, liked) },
                            voiceGender = voiceGender,
                            onVoiceGenderChange = { viewModel.setVoiceGender(it) }
                        )
                    }

                    AppScreen.SCANNER -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SubScreenTopBar(
                                title = "Homework Scanner",
                                onBack = { viewModel.navigateTo(AppScreen.TUTOR) }
                            )
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
                    }

                    AppScreen.QUIZ -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SubScreenTopBar(
                                title = "Quiz Generator",
                                onBack = { viewModel.navigateTo(AppScreen.TUTOR) }
                            )
                            QuizGeneratorScreen(
                                state = quizState,
                                onUpdateConfig = { s, t, d, c -> viewModel.updateQuizConfig(s, t, d, c) },
                                onGenerateQuiz = { viewModel.generateNewQuiz() },
                                onSelectAnswer = { q, o -> viewModel.selectQuizAnswer(q, o) },
                                onNextQuestion = { viewModel.goToNextQuizQuestion() },
                                onPrevQuestion = { viewModel.goToPrevQuizQuestion() },
                                onJumpToQuestion = { idx -> viewModel.jumpToQuizQuestion(idx) },
                                onSubmitQuiz = { viewModel.submitQuiz() },
                                onRetryQuiz = { viewModel.retryQuiz() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    AppScreen.HISTORY -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SubScreenTopBar(
                                title = "Study History",
                                onBack = { viewModel.navigateTo(AppScreen.TUTOR) }
                            )
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

                    AppScreen.POMODORO -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SubScreenTopBar(
                                title = "Pomodoro Study Timer",
                                onBack = { viewModel.navigateTo(AppScreen.TUTOR) }
                            )
                            PomodoroTimerScreen(
                                state = pomodoroState,
                                onStart = { viewModel.startPomodoro() },
                                onPause = { viewModel.pausePomodoro() },
                                onReset = { viewModel.resetPomodoro() },
                                onSkip = { viewModel.skipToNextPomodoroInterval() },
                                onSwitchMode = { viewModel.switchPomodoroMode(it) },
                                onUpdateConfig = { w, sb, lb, c, ab, as_ ->
                                    viewModel.updatePomodoroConfig(w, sb, lb, c, ab, as_)
                                },
                                onAdjustMinutes = { viewModel.adjustPomodoroMinutes(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ChatGptPillDark else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PureWhite else TextMutedGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                color = if (isSelected) PureWhite else Color(0xFFDDDDDD),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SubScreenTopBar(
    title: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = TrueBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChatGptPillDark)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Chat",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
