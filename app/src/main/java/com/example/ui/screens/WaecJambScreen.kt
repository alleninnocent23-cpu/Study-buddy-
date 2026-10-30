package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ExamPrepMode
import com.example.data.model.ExamScreenStage
import com.example.data.model.ExamType
import com.example.data.model.WaecJambQuestion
import com.example.data.model.WaecJambState
import com.example.data.repository.WaecJambQuestionBank
import com.example.ui.components.GlassCard
import com.example.ui.components.LoopingVideoPlayer
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

@Composable
fun WaecJambScreen(
    state: WaecJambState,
    isSpeaking: Boolean,
    onProceedFromGetStarted: () -> Unit,
    onBackToGetStarted: () -> Unit,
    onExitExamPrep: () -> Unit,
    onSelectExam: (ExamType) -> Unit,
    onSelectSubject: (String) -> Unit,
    onToggleSubject: (String) -> Unit = {},
    onSetSelectedSubjects: (List<String>) -> Unit = {},
    onSelectFacultyTrack: (String) -> Unit = {},
    onSelectYear: (Int?) -> Unit,
    onSelectYearRange: (start: Int, end: Int) -> Unit = { _, _ -> },
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (String) -> Unit,
    onSelectMode: (ExamPrepMode) -> Unit,
    onSelectQuestionCount: (Int) -> Unit,
    onStartExam: () -> Unit,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPrevQuestion: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onToggleFlagQuestion: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onRetakeExam: () -> Unit,
    onResetToSetup: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.stage) {
                    ExamScreenStage.GET_STARTED -> {
                        ExamGetStartedView(
                            onGetStarted = onProceedFromGetStarted,
                            onExit = onExitExamPrep
                        )
                    }
                    ExamScreenStage.SELECTION_SETUP -> {
                        ExamSelectionSetupView(
                            state = state,
                            onBack = onBackToGetStarted,
                            onSelectExam = onSelectExam,
                            onSelectSubject = onSelectSubject,
                            onToggleSubject = onToggleSubject,
                            onSetSelectedSubjects = onSetSelectedSubjects,
                            onSelectFacultyTrack = onSelectFacultyTrack,
                            onSelectYear = onSelectYear,
                            onSelectYearRange = onSelectYearRange,
                            onSelectTopic = onSelectTopic,
                            onSelectDifficulty = onSelectDifficulty,
                            onSelectMode = onSelectMode,
                            onSelectQuestionCount = onSelectQuestionCount,
                            onNext = onStartExam
                        )
                    }
                    ExamScreenStage.ACTIVE_EXAM -> {
                        ActiveExamView(
                            state = state,
                            isSpeaking = isSpeaking,
                            onSelectAnswer = onSelectAnswer,
                            onNext = onNextQuestion,
                            onPrev = onPrevQuestion,
                            onJump = onJumpToQuestion,
                            onToggleFlag = onToggleFlagQuestion,
                            onSubmit = onSubmitExam,
                            onExit = onResetToSetup,
                            onPlayAudio = onPlayAudio,
                            onStopAudio = onStopAudio
                        )
                    }
                    ExamScreenStage.EXAM_RESULTS -> {
                        ExamResultsView(
                            state = state,
                            onRetake = onRetakeExam,
                            onNewExam = onResetToSetup
                        )
                    }
                }
            }

            // Bottom bar to navigate from WAEC and JAMB to help study
            if (state.stage != ExamScreenStage.GET_STARTED) {
                WaecJambBottomStudyBar(
                    currentExam = state.config.examType,
                    onSelectExam = onSelectExam
                )
            }
        }
    }
}

/**
 * PAGE 1: GET STARTED VIEW FOR WAEC & JAMB
 * Uses the image of the bot holding the book (waec_book_bot.jpg)
 * and has a prominent "GET STARTED" button.
 */
@Composable
private fun ExamGetStartedView(
    onGetStarted: () -> Unit,
    onExit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Bar with Back icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- WAEC & JAMB IMAGE: BOT HOLDING THE BOOK (NO BORDER, SEAMLESS) ---
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .background(TrueBlack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.waec_book_bot),
                    contentDescription = "WAEC & JAMB Study Mode Bot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exam Mode Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF14241B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1997 - 2026 EXAM DATABASE ACTIVE",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Headline
            Text(
                text = "WAEC & JAMB STUDY MODE",
                color = PureWhite,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Master WAEC WASSCE and JAMB UTME with verified past questions, full working, and timed CBT mock simulations",
                color = TextMutedGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 340.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Highlights
            Column(
                modifier = Modifier.widthIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExamFeatureBullet("WAEC O'Level & A'Level WASSCE (1997 - 2026)")
                ExamFeatureBullet("JAMB UTME CBT Past Papers with Real Scoring")
                ExamFeatureBullet("Step-by-Step Solutions & Why Others Are Wrong")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // GET STARTED Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .height(52.dp)
                    .testTag("waec_jamb_get_started_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = TrueBlack
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "GET STARTED",
                    color = TrueBlack,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TrueBlack,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExamFeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * PAGE 2: SELECTION & SETUP VIEW (NEXT PAGE)
 * Plays the robot reading video cleanly in the UI (waec_reading_bot.mp4, muted, borderless, no background).
 * Allows selecting between WAEC and JAMB, subject, year (1997-2026), and mode,
 * then clicking NEXT.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExamSelectionSetupView(
    state: WaecJambState,
    onBack: () -> Unit,
    onSelectExam: (ExamType) -> Unit,
    onSelectSubject: (String) -> Unit,
    onToggleSubject: (String) -> Unit,
    onSetSelectedSubjects: (List<String>) -> Unit,
    onSelectFacultyTrack: (String) -> Unit,
    onSelectYear: (Int?) -> Unit,
    onSelectYearRange: (start: Int, end: Int) -> Unit,
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (String) -> Unit,
    onSelectMode: (ExamPrepMode) -> Unit,
    onSelectQuestionCount: (Int) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    val availableTopics = WaecJambQuestionBank.allTopicsBySubject[state.config.subject]
        ?: listOf("All Topics")

    var showSubjectDialog by remember { mutableStateOf(false) }
    var activeDialogTrack by remember { mutableStateOf("All") } // "All", "Science", "Art", "Commercial"

    var sliderRange by remember(state.config.yearStart, state.config.yearEnd) {
        mutableStateOf(state.config.yearStart.toFloat()..state.config.yearEnd.toFloat())
    }

    if (showSubjectDialog) {
        SubjectSelectionDialog(
            track = activeDialogTrack,
            selectedSubjects = state.config.selectedSubjects,
            onDismiss = { showSubjectDialog = false },
            onToggleSubject = onToggleSubject
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with Back icon to return to Get Started page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Intro",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Exam & Subject Setup",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- LOOPING VIDEO PLAYER IN UI: READING ROBOT (NO BORDER, NO BACKGROUND, MUTED) ---
        LoopingVideoPlayer(
            videoRawResId = R.raw.waec_reading_bot,
            hasBorder = false,
            backgroundColor = Color.Transparent,
            showMuteBadge = false,
            modifier = Modifier
                .width(220.dp)
                .height(230.dp)
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "SELECT EXAM & SUBJECT",
            color = PureWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Choose your exam, subject inputs, and drag year range to start",
            color = TextMutedGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- EXAM TYPE SELECTOR: WAEC vs JAMB ---
        Text(
            text = "SELECT EXAM TYPE",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // WAEC Card
            ExamTypeCard(
                title = "WAEC",
                subtitle = "O'Level & A'Level WASSCE",
                years = "1997 - 2026 Past Papers",
                isSelected = state.config.examType == ExamType.WAEC,
                onClick = { onSelectExam(ExamType.WAEC) },
                modifier = Modifier.weight(1f)
            )

            // JAMB Card
            ExamTypeCard(
                title = "JAMB",
                subtitle = "UTME & Direct Entry CBT",
                years = "1997 - 2026 CBT Papers",
                isSelected = state.config.examType == ExamType.JAMB,
                onClick = { onSelectExam(ExamType.JAMB) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4 - 10 SUBJECTS SELECTION SYSTEM (BOTH WAEC & JAMB) ---
        Text(
            text = if (state.config.examType == ExamType.WAEC) "WAEC SUBJECTS (SELECT 4 – 10 TO PRACTICE)" else "JAMB CBT SUBJECTS (SELECT 4 – 10 TO PRACTICE)",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        val selectedCount = state.config.selectedSubjects.size
        val isValidCount = selectedCount in 4..10

        // Status Header with Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.config.examType == ExamType.JAMB)
                    "JAMB UTME CBT requires 4 subjects combo"
                else
                    "WAEC WASSCE spans 7 to 9 subjects",
                color = TextMutedGray,
                fontSize = 11.sp
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when {
                    selectedCount < 4 -> Color(0xFF382314)
                    selectedCount in 4..10 -> Color(0xFF143820)
                    else -> Color(0xFF1E2838)
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        selectedCount < 4 -> Color(0xFFFFB74D)
                        selectedCount in 4..10 -> Color(0xFF4ADE80)
                        else -> ChatGptPlusStarBlue
                    }
                )
            ) {
                Text(
                    text = when {
                        selectedCount < 4 -> "Selected: $selectedCount / 10 (Min 4)"
                        selectedCount in 4..9 -> "✓ $selectedCount / 10 Subjects Active"
                        else -> "Max 10 / 10 Subjects Active"
                    },
                    color = when {
                        selectedCount < 4 -> Color(0xFFFFB74D)
                        selectedCount in 4..10 -> Color(0xFF4ADE80)
                        else -> ChatGptPlusStarBlue
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Subjects Pill Chips with quick remove ✕
        if (state.config.selectedSubjects.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.config.selectedSubjects.forEachIndexed { idx, sub ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, PureWhite)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. $sub",
                                color = TrueBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove $sub",
                                tint = TrueBlack,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onToggleSubject(sub) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Quick Preset Combinations (1-tap setup)
        Text(
            text = "ONE-TAP POPULAR PACKAGES",
            color = TextSubtle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        val relevantPresets = remember(state.config.examType) {
            WaecJambQuestionBank.popularPresets.filter { it.examType == state.config.examType }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            relevantPresets.forEach { preset ->
                val isPresetActive = state.config.selectedSubjects == preset.subjects
                Surface(
                    onClick = { onSetSelectedSubjects(preset.subjects) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPresetActive) Color(0xFF1E2838) else DarkSurface,
                    border = BorderStroke(1.dp, if (isPresetActive) ChatGptPlusStarBlue else BorderSubtle),
                    modifier = Modifier.testTag("preset_${preset.name.replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isPresetActive) ChatGptPlusStarBlue else TextMutedGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preset.name,
                            color = if (isPresetActive) PureWhite else TextMutedGray,
                            fontSize = 11.sp,
                            fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter for Subject Picker: [All] [Science] [Art] [Commercial]
        var selectedPickerCategory by remember { mutableStateOf("All") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14171A), RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("All", "Science", "Art", "Commercial").forEach { cat ->
                val isCatSel = selectedPickerCategory == cat
                Surface(
                    onClick = { selectedPickerCategory = cat },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCatSel) PureWhite else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cat,
                        color = if (isCatSel) TrueBlack else TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = if (isCatSel) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid/Flow of Clickable Subject Chips (Click to select 4-10 subjects)
        val displaySubjects = remember(selectedPickerCategory) {
            when (selectedPickerCategory) {
                "Science" -> WaecJambQuestionBank.scienceSubjects
                "Art" -> WaecJambQuestionBank.artSubjects
                "Commercial" -> WaecJambQuestionBank.commercialSubjects
                else -> WaecJambQuestionBank.allSubjects
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            displaySubjects.forEach { sub ->
                val isSelected = state.config.selectedSubjects.contains(sub)
                val selIndex = state.config.selectedSubjects.indexOf(sub)

                Surface(
                    onClick = { onToggleSubject(sub) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PureWhite else DarkSurface,
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
                    modifier = Modifier.testTag("clickable_subject_chip_$sub")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(TrueBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${selIndex + 1}",
                                    color = PureWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = BorderLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sub,
                            color = if (isSelected) TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- YEAR RANGE DRAG LINE (1997 - 2026) ---
        Text(
            text = "EXAM YEAR RANGE (1997 - 2026)",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        // Header with Drag Line Value Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Drag line to adjust year span",
                color = TextMutedGray,
                fontSize = 11.sp
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.config.yearStart == state.config.yearEnd) {
                            "Year: ${state.config.yearStart}"
                        } else {
                            "${state.config.yearStart} – ${state.config.yearEnd} (${state.config.yearEnd - state.config.yearStart + 1} Years)"
                        },
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // The RangeSlider Drag Line
        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                val start = range.start.roundToInt().coerceIn(1997, 2026)
                val end = range.endInclusive.roundToInt().coerceIn(start, 2026)
                onSelectYearRange(start, end)
            },
            valueRange = 1997f..2026f,
            steps = 28, // 2026 - 1997 - 1 = 28 tick steps
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = BorderSubtle,
                activeTickColor = TrueBlack,
                inactiveTickColor = BorderLight
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("year_range_drag_line")
        )

        // Year Scale Markers below Drag Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1997", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("2005", color = TextSubtle, fontSize = 10.sp)
            Text("2015", color = TextSubtle, fontSize = 10.sp)
            Text("2020", color = TextSubtle, fontSize = 10.sp)
            Text("2026", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Year Range Preset Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(
                Triple("1997 – 2026 (All)", 1997, 2026),
                Triple("2020 – 2026 (Recent)", 2020, 2026),
                Triple("2015 – 2024", 2015, 2024),
                Triple("2010 – 2019", 2010, 2019),
                Triple("2000 – 2009", 2000, 2009),
                Triple("1997 – 1999", 1997, 1999),
                Triple("2024 Only", 2024, 2024)
            )
            presets.forEach { (label, startY, endY) ->
                val isPresetActive = state.config.yearStart == startY && state.config.yearEnd == endY
                Surface(
                    onClick = {
                        sliderRange = startY.toFloat()..endY.toFloat()
                        onSelectYearRange(startY, endY)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPresetActive) PureWhite else DarkSurface,
                    border = BorderStroke(1.dp, if (isPresetActive) PureWhite else BorderSubtle)
                ) {
                    Text(
                        text = label,
                        color = if (isPresetActive) TrueBlack else PureWhite,
                        fontSize = 11.sp,
                        fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- TOPIC FILTER ---
        Text(
            text = "TOPIC & CONCEPT FOCUS",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableTopics.forEach { topic ->
                val isSel = state.config.topic == topic
                Surface(
                    onClick = { onSelectTopic(topic) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) PureWhite else DarkSurface,
                    border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle)
                ) {
                    Text(
                        text = topic,
                        color = if (isSel) TrueBlack else PureWhite,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- STUDY MODE: SMART PRACTICE vs TIMED MOCK EXAM ---
        Text(
            text = "STUDY MODE",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeCard(
                title = "Smart Practice",
                description = "Instant answers & step-by-step reasoning",
                icon = Icons.Default.Lightbulb,
                isSelected = state.config.mode == ExamPrepMode.PRACTICE,
                onClick = { onSelectMode(ExamPrepMode.PRACTICE) },
                modifier = Modifier.weight(1f)
            )

            ModeCard(
                title = "Timed Mock Exam",
                description = "Simulate real exam hall with countdown timer",
                icon = Icons.Default.Timer,
                isSelected = state.config.mode == ExamPrepMode.MOCK_EXAM,
                onClick = { onSelectMode(ExamPrepMode.MOCK_EXAM) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- QUESTION COUNT (MATCHING QUIZ GENERATOR: 10, 25, 50) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NUMBER OF QUESTIONS",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2838),
                border = BorderStroke(1.dp, ChatGptPlusStarBlue)
            ) {
                Text(
                    text = "50-Question Exam Available",
                    color = ChatGptPlusStarBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10, 25, 50).forEach { count ->
                val isSel = state.config.questionCount == count
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onSelectQuestionCount(count) },
                    color = if (isSel) PureWhite else TrueBlack
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (count == 50) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isSel) TrueBlack else ChatGptPlusStarBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "$count Qs",
                            color = if (isSel) TrueBlack else PureWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- NEXT / START EXAM BUTTON ---
        val canStartExam = state.config.selectedSubjects.size in 4..10
        Button(
            onClick = onNext,
            enabled = !state.isLoading && canStartExam,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("waec_jamb_next_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack,
                disabledContainerColor = DarkSurface,
                disabledContentColor = TextSubtle
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = TrueBlack, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.statusMessage ?: "Loading Exam Papers...",
                    color = TrueBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (!canStartExam) {
                Text(
                    text = "SELECT AT LEAST 4 SUBJECTS (${state.config.selectedSubjects.size}/4)",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = if (state.config.mode == ExamPrepMode.PRACTICE)
                        "NEXT • PRACTICE ${state.config.selectedSubjects.size} SUBJECTS"
                    else
                        "NEXT • BEGIN MOCK EXAM (${state.config.selectedSubjects.size} SUBJECTS)",
                    color = TrueBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Bottom Navigation Bar to smoothly navigate between WAEC and JAMB to help study.
 */
@Composable
fun WaecJambBottomStudyBar(
    currentExam: ExamType,
    onSelectExam: (ExamType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("waec_jamb_bottom_study_bar"),
        color = Color(0xFF101215),
        border = BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ChatGptPlusStarBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "EXAM STUDY NAVIGATOR • SWITCH TO COMPARE SYLLABUS",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1E24), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // WAEC Option
                val isWaec = currentExam == ExamType.WAEC
                Surface(
                    onClick = { onSelectExam(ExamType.WAEC) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isWaec) PureWhite else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_bar_waec_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (isWaec) TrueBlack else PureWhite,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "WAEC (WASSCE)",
                                color = if (isWaec) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "O'Level • 1997-2026",
                                color = if (isWaec) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // JAMB Option
                val isJamb = currentExam == ExamType.JAMB
                Surface(
                    onClick = { onSelectExam(ExamType.JAMB) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isJamb) PureWhite else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_bar_jamb_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = if (isJamb) TrueBlack else PureWhite,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "JAMB (UTME)",
                                color = if (isJamb) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CBT Practice • 1997-2026",
                                color = if (isJamb) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamTypeCard(
    title: String,
    subtitle: String,
    years: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PureWhite else DarkSurface,
        border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
        modifier = modifier.testTag("exam_type_card_$title")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = if (isSelected) TrueBlack else PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(TrueBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = if (isSelected) Color(0xFF333333) else TextMutedGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = years,
                color = if (isSelected) Color(0xFF111111) else Color(0xFF4ADE80),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PureWhite else DarkSurface,
        border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TrueBlack else PureWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isSelected) TrueBlack else PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = if (isSelected) Color(0xFF444444) else TextMutedGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * 2. ACTIVE EXAM VIEW
 * Displays question, options A-D, countdown timer, step-by-step solution,
 * reasons why other options are wrong, and concept frequency.
 */
@Composable
private fun ActiveExamView(
    state: WaecJambState,
    isSpeaking: Boolean,
    onSelectAnswer: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onJump: (Int) -> Unit,
    onToggleFlag: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val scrollState = rememberScrollState()
    val isPracticeMode = state.config.mode == ExamPrepMode.PRACTICE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Top Exam Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit Exam", tint = PureWhite, modifier = Modifier.size(18.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${question.exam.code} • ${question.subject}",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Question ${state.currentQuestionIndex + 1} of ${state.questions.size}",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
            }

            // Timer (for Mock Exam) or Flag button
            if (state.config.mode == ExamPrepMode.MOCK_EXAM) {
                val mins = state.secondsRemaining / 60
                val secs = state.secondsRemaining % 60
                val timerColor = if (state.secondsRemaining < 180) Color(0xFFFF5252) else PureWhite
                Row(
                    modifier = Modifier
                        .background(Color(0xFF221111), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = timerColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = timerColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                IconButton(
                    onClick = { onToggleFlag(state.currentQuestionIndex) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (question.isFlagged) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Flag Question",
                        tint = if (question.isFlagged) Color(0xFFFFB300) else PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Multi-Subject Navigation Row (when multiple subjects are being practiced)
        val distinctSubjects = remember(state.questions) {
            state.questions.map { it.subject }.distinct()
        }
        if (distinctSubjects.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                distinctSubjects.forEach { sub ->
                    val isCurrentSub = question.subject.equals(sub, ignoreCase = true)
                    val countForSub = state.questions.count { it.subject.equals(sub, ignoreCase = true) }
                    val answeredForSub = state.questions.count { it.subject.equals(sub, ignoreCase = true) && it.userSelectedIndex != null }
                    Surface(
                        onClick = {
                            val firstIdx = state.questions.indexOfFirst { it.subject.equals(sub, ignoreCase = true) }
                            if (firstIdx >= 0) onJump(firstIdx)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrentSub) PureWhite else DarkSurface,
                        border = BorderStroke(1.dp, if (isCurrentSub) PureWhite else BorderSubtle)
                    ) {
                        Text(
                            text = "$sub ($answeredForSub/$countForSub)",
                            color = if (isCurrentSub) TrueBlack else PureWhite,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrentSub) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Question Details & Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Badges row: Year/Session, Topic, Difficulty, Frequency
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E232A)
                ) {
                    Text(
                        text = "${question.year} ${question.session}",
                        color = Color(0xFF67EEF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurface
                ) {
                    Text(
                        text = question.topic,
                        color = PureWhite,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (question.difficulty.lowercase()) {
                        "easy" -> Color(0xFF143820)
                        "hard" -> Color(0xFF381414)
                        else -> Color(0xFF282814)
                    }
                ) {
                    Text(
                        text = question.difficulty,
                        color = when (question.difficulty.lowercase()) {
                            "easy" -> Color(0xFF4ADE80)
                            "hard" -> Color(0xFFFF6B6B)
                            else -> Color(0xFFFFD166)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Concept Frequency Notice
            Text(
                text = "📊 ${question.frequencyData}",
                color = TextMutedGray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Question Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                borderColor = BorderLight,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = question.question,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )

                    // Audio Readout Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    onStopAudio()
                                } else {
                                    val ttsText = "${question.question}. Options: ${question.options.joinToString(". ")}"
                                    onPlayAudio(ttsText)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read Question Aloud",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options A, B, C, D
            val isAnswered = question.userSelectedIndex != null
            val optionLetters = listOf("A", "B", "C", "D")

            question.options.forEachIndexed { idx, optText ->
                val letter = optionLetters.getOrElse(idx) { "$idx" }
                val isSelected = question.userSelectedIndex == idx
                val isCorrect = question.correctIndex == idx

                val containerColor = when {
                    !isAnswered -> if (isSelected) Color(0xFF22262E) else DarkSurface
                    isPracticeMode && isCorrect -> Color(0xFF10331A)
                    isPracticeMode && isSelected && !isCorrect -> Color(0xFF331414)
                    isSelected -> PureWhite
                    else -> DarkSurface
                }

                val borderColor = when {
                    !isAnswered -> if (isSelected) PureWhite else BorderSubtle
                    isPracticeMode && isCorrect -> Color(0xFF22C55E)
                    isPracticeMode && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> PureWhite
                    else -> BorderSubtle
                }

                val textColor = when {
                    !isPracticeMode && isSelected -> TrueBlack
                    else -> PureWhite
                }

                Surface(
                    onClick = {
                        if (!isPracticeMode || !isAnswered) {
                            onSelectAnswer(state.currentQuestionIndex, idx)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("option_${letter}_${idx}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (!isPracticeMode && isSelected) TrueBlack else Color(0xFF1A1E24),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optText,
                            color = textColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // SMART PRACTICE MODE EXPLANATIONS & WHY OTHERS ARE WRONG
            if (isPracticeMode && isAnswered) {
                Spacer(modifier = Modifier.height(14.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    borderColor = Color(0xFF22C55E).copy(alpha = 0.5f),
                    backgroundColor = Color(0xFF0F1B12)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Correct Answer: Option ${question.correctAnswerLetter}",
                                color = Color(0xFF4ADE80),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = question.explanation,
                            color = PureWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        // Why others are wrong breakdown
                        if (question.whyOthersAreWrong.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0x334ADE80))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "WHY OTHER OPTIONS ARE WRONG:",
                                color = Color(0xFFFFD166),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            question.whyOthersAreWrong.forEach { (opt, reason) ->
                                Text(
                                    text = "• Option $opt: $reason",
                                    color = Color(0xFFDDDDDD),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // XP Meter Bar in Active Exam
        val answeredExamCount = state.questions.count { it.userSelectedIndex != null }
        val totalExamQuestions = state.questions.size
        val earnedExamXp = answeredExamCount * 10
        val totalPossibleExamXp = totalExamQuestions * 10
        val examXpFraction = if (totalPossibleExamXp > 0) earnedExamXp.toFloat() / totalPossibleExamXp.toFloat() else 0f

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF141923),
            border = BorderStroke(1.dp, Color(0xFF2B3A52)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "XP METER",
                    color = Color(0xFFFBBF24),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { examXpFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFF0D121B)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "+$earnedExamXp / $totalPossibleExamXp XP",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Navigation Controls with Centered Submit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Previous Button (Left) - Black with white border
            OutlinedButton(
                onClick = onPrev,
                enabled = state.currentQuestionIndex > 0,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (state.currentQuestionIndex > 0) PureWhite else BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = TrueBlack,
                    contentColor = if (state.currentQuestionIndex > 0) PureWhite else TextSubtle
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (state.currentQuestionIndex > 0) PureWhite else TextSubtle
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous", color = if (state.currentQuestionIndex > 0) PureWhite else TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            // SUBMIT BUTTON IN THE CENTER - White background with black text/icon
            Button(
                onClick = onSubmit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = TrueBlack
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
                    .testTag("submit_exam_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Submit Exam", color = TrueBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Next Button (Right) - Black with white border
            OutlinedButton(
                onClick = onNext,
                enabled = state.currentQuestionIndex < state.questions.size - 1,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (state.currentQuestionIndex < state.questions.size - 1) PureWhite else BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = TrueBlack,
                    contentColor = if (state.currentQuestionIndex < state.questions.size - 1) PureWhite else TextSubtle
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("next_exam_question_button")
            ) {
                Text("Next", color = if (state.currentQuestionIndex < state.questions.size - 1) PureWhite else TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (state.currentQuestionIndex < state.questions.size - 1) PureWhite else TextSubtle
                )
            }
        }
    }
}

/**
 * 3. EXAM RESULTS & PERFORMANCE TRACKING
 * Calculates score, WAEC grade (A1-F9) or JAMB UTME score,
 * breakdown of topics, review of incorrect questions.
 */
@Composable
private fun ExamResultsView(
    state: WaecJambState,
    onRetake: () -> Unit,
    onNewExam: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row with X Cancel Button ("up the display")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXAM PERFORMANCE REPORT",
                color = TextMutedGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = onNewExam,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .testTag("exam_results_cancel_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel and Exit",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        val isFailed = state.scorePercentage < 50

        // Failure / "Someone Falls" Encouraging Banner: "You can do better" + "Try Again"
        if (isFailed) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF261214),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💪", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "You can do better!",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Score: ${state.scorePercentage}% • Passing target not reached",
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = onNewExam,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Every great scholar faced setbacks before mastery. Review the detailed explanations and solutions below, then take another attempt. You've got this!",
                        color = Color(0xFFFEE2E2),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRetake,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("exam_try_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Try Again", color = TrueBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = Color(0xFF67EEF8),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "EXAM REPORT & ANALYTICS",
            color = PureWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "${state.config.examType.displayName} • ${state.config.subject}",
            color = TextMutedGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Main Score Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            borderColor = BorderLight,
            backgroundColor = DarkSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // XP Earned Banner
                val earnedExamXp = state.correctCount * 10
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF141923),
                    border = BorderStroke(1.dp, Color(0xFF2B3A52)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXAM XP EARNED", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("+$earnedExamXp XP", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "${state.correctCount} / ${state.questions.size}",
                    color = PureWhite,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${state.scorePercentage}% ACCURACY",
                    color = if (state.scorePercentage >= 50) Color(0xFF4ADE80) else Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                if (state.config.examType == ExamType.WAEC) {
                    Text(text = "OFFICIAL WAEC GRADE:", color = TextMutedGray, fontSize = 11.sp)
                    Text(
                        text = state.waecGrade,
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(text = "JAMB UTME ESTIMATED SCORE:", color = TextMutedGray, fontSize = 11.sp)
                    Text(
                        text = "${state.jambScoreEstimate} / 400",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Detailed Question Review
        Text(
            text = "DETAILED QUESTION REVIEW",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        state.questions.forEachIndexed { idx, q ->
            val isCorrect = q.userSelectedIndex == q.correctIndex
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                cornerRadius = 10.dp,
                borderColor = if (isCorrect) Color(0xFF22C55E).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f),
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Q${idx + 1}. [${q.year} ${q.session}]",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isCorrect) "✓ Correct" else "✗ Incorrect",
                            color = if (isCorrect) Color(0xFF4ADE80) else Color(0xFFFF6B6B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = q.question,
                        color = PureWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Correct Answer: Option ${q.correctAnswerLetter} (${q.options.getOrElse(q.correctIndex) { "" }})",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = q.explanation,
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNewExam,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = TrueBlack),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("New Practice", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Clickable input container for Subject selection (mimics an interactive input field).
 */
@Composable
private fun SubjectInputField(
    label: String,
    subject: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, if (isActive) PureWhite else BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isActive) PureWhite.copy(alpha = 0.15f) else TrueBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject,
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Dialog displaying subjects list with search and faculty categorization.
 */
@Composable
private fun SubjectSelectionDialog(
    track: String, // "All", "Science", "Art", "Commercial"
    selectedSubjects: List<String>,
    onDismiss: () -> Unit,
    onToggleSubject: (subject: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val rawList = when (track) {
        "Science" -> WaecJambQuestionBank.scienceSubjects
        "Art" -> WaecJambQuestionBank.artSubjects
        "Commercial" -> WaecJambQuestionBank.commercialSubjects
        else -> WaecJambQuestionBank.allSubjects
    }

    val filteredList = rawList.filter {
        it.contains(searchQuery.trim(), ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .height(540.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (track) {
                                "Science" -> "Select Science Subjects"
                                "Art" -> "Select Art Subjects"
                                "Commercial" -> "Select Commercial Subjects"
                                else -> "Select Exam Subjects (4 - 10)"
                            },
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedSubjects.size} / 10 Selected (Min 4, Max 10)",
                            color = if (selectedSubjects.size in 4..10) Color(0xFF4ADE80) else Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search subject...", color = TextSubtle, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedGray, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderSubtle,
                        cursorColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("subject_search_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Subject List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No subjects found matching '$searchQuery'", color = TextMutedGray, fontSize = 12.sp)
                        }
                    } else {
                        filteredList.forEach { sub ->
                            val isSelected = selectedSubjects.contains(sub)
                            val selIndex = selectedSubjects.indexOf(sub)
                            val subTrack = when {
                                WaecJambQuestionBank.scienceSubjects.contains(sub) -> "Science"
                                WaecJambQuestionBank.artSubjects.contains(sub) -> "Art"
                                WaecJambQuestionBank.commercialSubjects.contains(sub) -> "Commercial"
                                else -> "General"
                            }

                            Surface(
                                onClick = { onToggleSubject(sub) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PureWhite else TrueBlack,
                                border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dialog_subject_item_$sub")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (subTrack) {
                                            "Science" -> Icons.Default.Science
                                            "Art" -> Icons.AutoMirrored.Filled.MenuBook
                                            "Commercial" -> Icons.Default.Paid
                                            else -> Icons.Default.School
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) TrueBlack else TextMutedGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sub,
                                            color = if (isSelected) TrueBlack else PureWhite,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = "$subTrack Track",
                                            color = if (isSelected) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(TrueBlack, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${selIndex + 1}",
                                                color = PureWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = BorderSubtle,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Done / Apply Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "DONE • ${selectedSubjects.size} SUBJECTS SELECTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

