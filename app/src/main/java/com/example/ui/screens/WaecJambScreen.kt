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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
    onSelectYear: (Int?) -> Unit,
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
                    onSelectYear = onSelectYear,
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
    onSelectYear: (Int?) -> Unit,
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (String) -> Unit,
    onSelectMode: (ExamPrepMode) -> Unit,
    onSelectQuestionCount: (Int) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    val availableTopics = WaecJambQuestionBank.allTopicsBySubject[state.config.subject]
        ?: listOf("All Topics")

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
            text = "Choose your exam, year, and subject to begin practicing",
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

        Spacer(modifier = Modifier.height(18.dp))

        // --- SUBJECT SELECTOR ---
        Text(
            text = "SUBJECT",
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
            WaecJambQuestionBank.allSubjects.forEach { sub ->
                val isSel = state.config.subject == sub
                Surface(
                    onClick = { onSelectSubject(sub) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) PureWhite else DarkSurface,
                    border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle),
                    modifier = Modifier.testTag("subject_chip_$sub")
                ) {
                    Text(
                        text = sub,
                        color = if (isSel) TrueBlack else PureWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- YEAR FILTER (1997 - 2026) ---
        Text(
            text = "PAST PAPER YEAR (1997 - 2026)",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        val popularYears = listOf(null, 2024, 2023, 2022, 2021, 2020, 2018, 2015, 2010, 2005, 2000, 1999)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            popularYears.forEach { yr ->
                val isSel = state.config.selectedYear == yr
                val label = if (yr == null) "All Years (1997-2026)" else "$yr"
                Surface(
                    onClick = { onSelectYear(yr) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) PureWhite else DarkSurface,
                    border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle)
                ) {
                    Text(
                        text = label,
                        color = if (isSel) TrueBlack else PureWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
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

        // --- QUESTION COUNT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "NUMBER OF QUESTIONS",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 10, 15, 20, 40).forEach { cnt ->
                    val isSel = state.config.questionCount == cnt
                    Surface(
                        onClick = { onSelectQuestionCount(cnt) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) PureWhite else DarkSurface,
                        border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle)
                    ) {
                        Text(
                            text = "$cnt",
                            color = if (isSel) TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- NEXT / START EXAM BUTTON ---
        Button(
            onClick = onNext,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("waec_jamb_next_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack
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
            } else {
                Text(
                    text = if (state.config.mode == ExamPrepMode.PRACTICE) "NEXT • START PRACTICE" else "NEXT • BEGIN MOCK EXAM",
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
                    text = "${state.config.examType.code} • ${state.config.subject}",
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

        Spacer(modifier = Modifier.height(8.dp))

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

        // Bottom Navigation Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = state.currentQuestionIndex > 0,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous", fontSize = 12.sp)
            }

            if (state.currentQuestionIndex < state.questions.size - 1) {
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = TrueBlack)
                ) {
                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = onSubmit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = TrueBlack),
                    modifier = Modifier.testTag("submit_exam_button")
                ) {
                    Text("Submit Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = Color(0xFF67EEF8),
            modifier = Modifier.size(54.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

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
