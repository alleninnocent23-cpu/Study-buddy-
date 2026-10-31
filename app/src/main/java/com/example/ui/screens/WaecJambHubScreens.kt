package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExamPrepMode
import com.example.data.model.ExamType
import com.example.data.model.WaecJambState
import com.example.data.repository.WaecJambQuestionBank
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import kotlin.math.roundToInt

enum class WaecHubSubView {
    WAEC_HUB,     // Screen 4 from reference image: WAEC Success hero + 4 action cards + Pass WAEC banner
    JAMB_HUB,     // Screen 6 from reference image: JAMB 2026 hero + 4 action cards
    SUBJECTS,     // Screen 5 from reference image: WAEC - Subjects with filter chips and subject list
    FORMULAS      // Screen 8 from reference image: Mathematics Formulas & Science Reference
}

/**
 * SCREEN 4: WAEC HUB VIEW (Black and White aesthetic)
 * Matching the exact visual layout from the user's reference image:
 * - Header: WAEC, "Past questions, solutions & more", 3-dots menu
 * - Hero Card: "WAEC Success", "Practice past questions, learn from detailed explanations and boost your score!"
 * - 4 Option Cards:
 *   1. "Past Questions" -> "Real WAEC questions (with answers)"
 *   2. "Detailed Explanations" -> "Step-by-step solutions"
 *   3. "Subject-wise" -> "Choose your subject" (switches to Screen 5)
 *   4. "Exam Tips" -> "Proven tips for success"
 * - Bottom Promotional Card: "Pass WAEC with Confidence!" (graduation cap)
 * - Complete 1997-2026 drag line slider & 4-10 subjects selector
 * - Center "START WAEC PRACTICE" button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaecHubView(
    state: WaecJambState,
    onOpenSubjects: () -> Unit,
    onOpenSubjectDialog: () -> Unit,
    onToggleSubject: (String) -> Unit,
    onSelectYearRange: (start: Int, end: Int) -> Unit,
    onSelectMode: (ExamPrepMode) -> Unit,
    onStartExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showExplanationsInfoDialog by remember { mutableStateOf(false) }

    var sliderRange by remember(state.config.yearStart, state.config.yearEnd) {
        mutableStateOf(state.config.yearStart.toFloat()..state.config.yearEnd.toFloat())
    }

    if (showTipsDialog) {
        ExamTipsModal(
            examTitle = "WAEC WASSCE Success Strategy",
            tips = listOf(
                "Aim for A1: 75% and above grants distinction across all Nigerian & West African Universities.",
                "Cover the General Mathematics core: Algebra, Trigonometry, and Geometry make up 65% of Paper 1 & 2.",
                "English Comprehension & Summary: Read the passage twice before tackling summary sentences.",
                "Show all working steps: WAEC theory markers award method marks (M-marks) even if final calculation is flawed.",
                "Time management: Spend no more than 1.5 minutes per objective question in Paper 1."
            ),
            onDismiss = { showTipsDialog = false }
        )
    }

    if (showExplanationsInfoDialog) {
        ExamTipsModal(
            examTitle = "WAEC Step-by-Step Study Mode",
            tips = listOf(
                "Practice Mode active: Every question gives instant step-by-step working upon answering.",
                "Learn why other options are wrong: Avoid common pitfalls caught by WAEC chief examiners.",
                "Speech audio enabled: Tap the speaker icon to listen to detailed AI explanations verbally.",
                "Frequency indicators: Highlights questions tested repeatedly between 1997 and 2026."
            ),
            onDismiss = { showExplanationsInfoDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WAEC",
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Past questions, solutions & more",
                    color = TextMutedGray,
                    fontSize = 12.sp
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("WAEC Exam Tips", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            showTipsDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View Subject-wise List", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOpenSubjects()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Switch to Smart Practice", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onSelectMode(ExamPrepMode.PRACTICE)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- HERO BANNER CARD (WAEC SUCCESS) ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141414),
            border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Crown / Trophy Icon Box in Black & White
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF222222))
                        .border(1.dp, Color(0xFF404040), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "WAEC Success",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WAEC Success",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Practice past questions, learn from detailed explanations and boost your score!",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4 ACTION CARDS (MATCHING REFERENCE SCREEN 4) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Past Questions
            ActionOptionCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Past Questions",
                subtitle = "Real WAEC questions (with answers)",
                onClick = onStartExam
            )

            // 2. Detailed Explanations
            ActionOptionCard(
                icon = Icons.Default.Lightbulb,
                title = "Detailed Explanations",
                subtitle = "Step-by-step solutions",
                onClick = {
                    onSelectMode(ExamPrepMode.PRACTICE)
                    showExplanationsInfoDialog = true
                }
            )

            // 3. Subject-wise
            ActionOptionCard(
                icon = Icons.Default.Tune,
                title = "Subject-wise",
                subtitle = "Choose your subject",
                onClick = onOpenSubjects
            )

            // 4. Exam Tips
            ActionOptionCard(
                icon = Icons.Default.AutoAwesome,
                title = "Exam Tips",
                subtitle = "Proven tips for success",
                onClick = { showTipsDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MOTIVATIONAL PASS WAEC BANNER ---
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF141414),
            border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pass WAEC with Confidence!",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "1997 — 2026 Authentic Syllabus Database",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .border(1.dp, Color(0xFF404040), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Graduation Cap",
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- PRACTICE CONFIGURATION (BLACK & WHITE) ---
        Text(
            text = "PRACTICE SETUP (1997 – 2026)",
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4 - 10 Subjects Input Box
        Surface(
            onClick = onOpenSubjectDialog,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF141414),
            border = BorderStroke(1.dp, Color(0xFF333333)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("waec_subjects_input_capsule")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Subjects to Practice (Tap to Select 4 – 10)",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${state.config.selectedSubjects.size} subjects selected • ${if (state.config.selectedSubjects.size in 4..10) "Valid combo" else "Min 4 required"}",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PureWhite
                ) {
                    Text(
                        text = "${state.config.selectedSubjects.size}/10",
                        color = TrueBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Active Subject Chips
        if (state.config.selectedSubjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.config.selectedSubjects.forEach { sub ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, PureWhite)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub,
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
                                    .size(13.dp)
                                    .clickable { onToggleSubject(sub) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DRAG LINE YEAR RANGE SLIDER (1997 - 2026) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Year Range (Drag Line)",
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF222222),
                border = BorderStroke(1.dp, Color(0xFF404040))
            ) {
                Text(
                    text = "${sliderRange.start.roundToInt()} — ${sliderRange.endInclusive.roundToInt()} (${sliderRange.endInclusive.roundToInt() - sliderRange.start.roundToInt() + 1} Yrs)",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                onSelectYearRange(range.start.roundToInt(), range.endInclusive.roundToInt())
            },
            valueRange = 1997f..2026f,
            steps = 28,
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("waec_year_range_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1997 (Earliest)", color = TextSubtle, fontSize = 10.sp)
            Text("2026 (Curriculum Verified)", color = TextSubtle, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- START BUTTON (PURE WHITE IN CENTER) ---
        Button(
            onClick = onStartExam,
            enabled = state.config.selectedSubjects.size in 4..10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("waec_start_practice_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack,
                disabledContainerColor = Color(0xFF333333),
                disabledContentColor = Color(0xFF777777)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "START WAEC PRACTICE • ${state.config.selectedSubjects.size} SUBJECTS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * SCREEN 6: JAMB HUB VIEW (Black and White aesthetic)
 * Matching reference image Screen 6:
 * - Header: JAMB, "Practice. Prepare. Score Higher.", 3-dots menu
 * - Hero Card: "JAMB 2026", "Get ready with past questions, syllabus, and expert tips."
 * - 4 Option Cards:
 *   1. "Past Questions" -> "Real JAMB questions (with answers)"
 *   2. "Syllabus" -> "View the official syllabus"
 *   3. "Practice Tests" -> "Timed tests & review"
 *   4. "Study Guide" -> "Tips and exam strategies"
 * - Quick Faculty Track inputs: Science, Art, Commercial
 * - Drag line Year Range Slider (1997 - 2026)
 * - Center "START JAMB CBT PRACTICE" button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JambHubView(
    state: WaecJambState,
    onOpenSubjects: () -> Unit,
    onOpenSubjectDialog: (track: String) -> Unit,
    onSelectFacultyTrack: (String) -> Unit,
    onToggleSubject: (String) -> Unit,
    onSelectYearRange: (start: Int, end: Int) -> Unit,
    onSelectMode: (ExamPrepMode) -> Unit,
    onStartExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var showSyllabusDialog by remember { mutableStateOf(false) }
    var showStudyGuideDialog by remember { mutableStateOf(false) }

    var sliderRange by remember(state.config.yearStart, state.config.yearEnd) {
        mutableStateOf(state.config.yearStart.toFloat()..state.config.yearEnd.toFloat())
    }

    if (showSyllabusDialog) {
        ExamTipsModal(
            examTitle = "JAMB UTME 2026 Official Syllabus",
            tips = listOf(
                "Use of English (Compulsory for ALL): 60 questions, includes comprehension passage, sentence interpretation, and antonyms/synonyms.",
                "Compulsory Novel: 'The Life Changer' / prescribed literary text tested under English Language.",
                "Science Faculty: Mathematics, English, Physics, Chemistry (or Biology for Medicine/Agriculture).",
                "Art & Humanities: English, Literature in English, Government, CRS/IRS or History.",
                "Commercial & Social Sciences: English, Mathematics, Economics, Commerce or Financial Accounting.",
                "Total Marks: 400 marks. Target 280+ for competitive professional courses."
            ),
            onDismiss = { showSyllabusDialog = false }
        )
    }

    if (showStudyGuideDialog) {
        ExamTipsModal(
            examTitle = "JAMB CBT Exam Strategy & Tips",
            tips = listOf(
                "Speed is paramount: 180 questions in 120 minutes (40 seconds per question).",
                "Answer familiar questions first: Never spend more than 1 minute on any tough calculation.",
                "Use keyboard shortcut keys: 'A', 'B', 'C', 'D' to select options; 'N' for Next, 'P' for Previous, 'S' for Submit.",
                "Negative marking does NOT exist in JAMB: Never leave any question unanswered.",
                "CBT System Review: Flag doubtful questions and revisit them in the last 15 minutes."
            ),
            onDismiss = { showStudyGuideDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "JAMB",
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Practice. Prepare. Score Higher.",
                    color = TextMutedGray,
                    fontSize = 12.sp
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("JAMB Official Syllabus", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            showSyllabusDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("CBT Strategy Guide", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            showStudyGuideDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View All Subjects", color = PureWhite, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOpenSubjects()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- HERO BANNER CARD (JAMB 2026) ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141414),
            border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Crest Emblem in Black & White
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .border(1.5.dp, Color(0xFF444444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "JAMB Crest",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "JAMB 2026",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Get ready with past questions, syllabus, and expert tips.",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4 ACTION CARDS (MATCHING REFERENCE SCREEN 6) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Past Questions
            ActionOptionCard(
                icon = Icons.Default.Description,
                title = "Past Questions",
                subtitle = "Real JAMB questions (with answers)",
                onClick = onStartExam
            )

            // 2. Syllabus
            ActionOptionCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Syllabus",
                subtitle = "View the official syllabus",
                onClick = { showSyllabusDialog = true }
            )

            // 3. Practice Tests
            ActionOptionCard(
                icon = Icons.Default.Timer,
                title = "Practice Tests",
                subtitle = "Timed tests & review (400 Marks)",
                onClick = {
                    onSelectMode(ExamPrepMode.MOCK_EXAM)
                    onStartExam()
                }
            )

            // 4. Study Guide
            ActionOptionCard(
                icon = Icons.Default.Stars,
                title = "Study Guide",
                subtitle = "Tips and exam strategies",
                onClick = { showStudyGuideDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- FACULTY TRACKS SELECTION (SCIENCE, ART, COMMERCIAL) ---
        Text(
            text = "JAMB FACULTY TRACK COMBO",
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Science", "Art", "Commercial").forEach { track ->
                val isSelected = state.config.facultyTrack == track
                Surface(
                    onClick = {
                        onSelectFacultyTrack(track)
                        onOpenSubjectDialog(track)
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PureWhite else Color(0xFF141414),
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else Color(0xFF333333)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = track,
                            color = if (isSelected) TrueBlack else PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "4-Subject Combo",
                            color = if (isSelected) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Subject Chips
        if (state.config.selectedSubjects.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.config.selectedSubjects.forEach { sub ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, PureWhite)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub,
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
                                    .size(13.dp)
                                    .clickable { onToggleSubject(sub) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DRAG LINE YEAR RANGE SLIDER (1997 - 2026) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CBT Year Range (Drag Line)",
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF222222),
                border = BorderStroke(1.dp, Color(0xFF404040))
            ) {
                Text(
                    text = "${sliderRange.start.roundToInt()} — ${sliderRange.endInclusive.roundToInt()} (${sliderRange.endInclusive.roundToInt() - sliderRange.start.roundToInt() + 1} Yrs)",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                onSelectYearRange(range.start.roundToInt(), range.endInclusive.roundToInt())
            },
            valueRange = 1997f..2026f,
            steps = 28,
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = PureWhite,
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("jamb_year_range_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1997 (Earliest)", color = TextSubtle, fontSize = 10.sp)
            Text("2026 (Curriculum Verified)", color = TextSubtle, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- START BUTTON (PURE WHITE IN CENTER) ---
        Button(
            onClick = onStartExam,
            enabled = state.config.selectedSubjects.size in 4..10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("jamb_start_practice_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack,
                disabledContainerColor = Color(0xFF333333),
                disabledContentColor = Color(0xFF777777)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "START JAMB CBT PRACTICE • ${state.config.selectedSubjects.size} SUBJECTS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * SCREEN 5: SUBJECTS DIRECTORY VIEW (WAEC / JAMB - Subjects)
 * Matching reference image Screen 5:
 * - Header: Back arrow, "WAEC - Subjects" (or JAMB), 3-dots menu
 * - Horizontal filter chips: [All] [Maths] [English] [Physics] [Chemistry]...
 * - Clean subject cards with rounded square icon, title, "Past questions (200+)", chevron `>`
 */
@Composable
fun WaecSubjectsDirectoryView(
    examType: ExamType,
    selectedSubjects: List<String>,
    onBack: () -> Unit,
    onToggleSubject: (String) -> Unit,
    onSelectAndStart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilterChip by remember { mutableStateOf("All") }

    val filterChips = listOf("All", "Maths", "English", "Physics", "Chemistry", "Biology", "Economics", "Government", "Commercial")

    val allSubjectEntries = listOf(
        SubjectItemData("Mathematics", "Past questions (200+)", "Maths", "π"),
        SubjectItemData("English Language", "Past questions (180+)", "English", "ABC"),
        SubjectItemData("Physics", "Past questions (170+)", "Physics", "⚛"),
        SubjectItemData("Chemistry", "Past questions (160+)", "Chemistry", "🧪"),
        SubjectItemData("Biology", "Past questions (150+)", "Biology", "🌿"),
        SubjectItemData("Government", "Past questions (120+)", "Government", "🏛"),
        SubjectItemData("Literature in English", "Past questions (100+)", "English", "📖"),
        SubjectItemData("Economics", "Past questions (140+)", "Economics", "📈"),
        SubjectItemData("Commerce", "Past questions (110+)", "Commercial", "💼"),
        SubjectItemData("Financial Accounting", "Past questions (100+)", "Commercial", "💰"),
        SubjectItemData("Further Mathematics", "Past questions (95+)", "Maths", "∫"),
        SubjectItemData("Agricultural Science", "Past questions (105+)", "Biology", "🌾"),
        SubjectItemData("Geography", "Past questions (90+)", "Science", "🌍"),
        SubjectItemData("Computer Studies", "Past questions (85+)", "Science", "💻"),
        SubjectItemData("Civic Education", "Past questions (90+)", "Government", "👥")
    )

    val displayedSubjects = allSubjectEntries.filter { item ->
        val matchesFilter = when (activeFilterChip) {
            "All" -> true
            "Maths" -> item.subject.contains("Math", ignoreCase = true)
            "English" -> item.subject.contains("English", ignoreCase = true) || item.subject.contains("Literature", ignoreCase = true)
            "Physics" -> item.subject.contains("Physics", ignoreCase = true)
            "Chemistry" -> item.subject.contains("Chemistry", ignoreCase = true)
            "Biology" -> item.subject.contains("Biology", ignoreCase = true) || item.subject.contains("Agric", ignoreCase = true)
            "Economics" -> item.subject.contains("Economics", ignoreCase = true)
            "Government" -> item.subject.contains("Government", ignoreCase = true) || item.subject.contains("Civic", ignoreCase = true)
            "Commercial" -> item.category == "Commercial"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() || item.subject.contains(searchQuery.trim(), ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${examType.name} - Subjects",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- FILTER CHIPS ROW (PURE BLACK & WHITE) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterChips.forEach { chip ->
                val isSelected = activeFilterChip == chip
                Surface(
                    onClick = { activeFilterChip = chip },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PureWhite else TrueBlack,
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else Color(0xFF333333))
                ) {
                    Text(
                        text = chip,
                        color = if (isSelected) TrueBlack else TextMutedGray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search subjects...", color = TextSubtle, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedGray, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = PureWhite,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- SUBJECT CARDS LIST ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayedSubjects.forEach { item ->
                val isSelected = selectedSubjects.contains(item.subject)
                Surface(
                    onClick = {
                        onToggleSubject(item.subject)
                        onSelectAndStart(item.subject)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141414),
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else Color(0xFF262626)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rounded square symbol box (Black & White)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF202020))
                                .border(1.dp, Color(0xFF383838), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.symbol,
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.subject,
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.pastQuestionsCount,
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(PureWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = TrueBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class SubjectItemData(
    val subject: String,
    val pastQuestionsCount: String,
    val category: String,
    val symbol: String
)

/**
 * SCREEN 8: FORMULAS & STUDY NOTE VIEW (Black and White aesthetic)
 * Matching reference image Screen 8:
 * - Header: Back arrow, "Mathematics Formulas", 3-dots menu
 * - Sections: Algebra, Geometry, Trigonometry, Mechanics
 * - Real mathematical equations
 */
@Composable
fun StudyNoteFormulaView(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Mathematics Formulas",
                color = PureWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 1: ALGEBRA ---
        FormulaCategoryCard(
            categoryTitle = "Algebra",
            formulas = listOf(
                FormulaRowData("Quadratic formula:", "x = (-b ± √(b² - 4ac)) / (2a)"),
                FormulaRowData("Expansion:", "(a + b)² = a² + 2ab + b²"),
                FormulaRowData("Factorization:", "a² - b² = (a + b)(a - b)")
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- SECTION 2: GEOMETRY ---
        FormulaCategoryCard(
            categoryTitle = "Geometry",
            formulas = listOf(
                FormulaRowData("Area of triangle:", "A = ½ bh"),
                FormulaRowData("Area of circle:", "A = πr²"),
                FormulaRowData("Volume of cube:", "V = s³")
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- SECTION 3: TRIGONOMETRY ---
        FormulaCategoryCard(
            categoryTitle = "Trigonometry",
            formulas = listOf(
                FormulaRowData("Pythagorean Identity:", "sin²θ + cos²θ = 1"),
                FormulaRowData("Tangent:", "tanθ = sinθ / cosθ"),
                FormulaRowData("Double Angle:", "sin(2θ) = 2 sinθ cosθ")
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- SECTION 4: PHYSICS MECHANICS ---
        FormulaCategoryCard(
            categoryTitle = "Physics Mechanics",
            formulas = listOf(
                FormulaRowData("First Equation of Motion:", "v = u + at"),
                FormulaRowData("Second Equation of Motion:", "s = ut + ½ at²"),
                FormulaRowData("Kinetic Energy:", "Ek = ½ mv²"),
                FormulaRowData("Work Done:", "W = Force × Distance")
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Note Button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Back to Study Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FormulaCategoryCard(
    categoryTitle: String,
    formulas: List<FormulaRowData>
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryTitle,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            formulas.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${item.label}",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.formula,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

private data class FormulaRowData(
    val label: String,
    val formula: String
)

/**
 * REUSABLE ACTION OPTION CARD (Matching the 4 cards in Screens 4 & 6 of reference image)
 */
@Composable
private fun ActionOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in rounded square box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF202020))
                    .border(1.dp, Color(0xFF383838), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 5-TAB BOTTOM NAVIGATION BAR (Matching bottom bar across all screens in reference image)
 * [ AI ] [ Study ] [ WAEC ] [ JAMB ] [ StudyNote ] in Black & White
 */
@Composable
fun WaecJambUnifiedBottomNav(
    activeSubView: WaecHubSubView,
    onNavigateToAi: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToWaec: () -> Unit,
    onNavigateToJamb: () -> Unit,
    onNavigateToStudyNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("waec_jamb_unified_bottom_nav"),
        color = TrueBlack,
        border = BorderStroke(1.dp, Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. AI
            BottomNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "AI",
                isSelected = false,
                onClick = onNavigateToAi
            )

            // 2. Study
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Study",
                isSelected = activeSubView == WaecHubSubView.SUBJECTS,
                onClick = onNavigateToStudy
            )

            // 3. WAEC
            BottomNavItem(
                icon = Icons.Default.EmojiEvents,
                label = "WAEC",
                isSelected = activeSubView == WaecHubSubView.WAEC_HUB,
                onClick = onNavigateToWaec
            )

            // 4. JAMB
            BottomNavItem(
                icon = Icons.Default.School,
                label = "JAMB",
                isSelected = activeSubView == WaecHubSubView.JAMB_HUB,
                onClick = onNavigateToJamb
            )

            // 5. StudyNote
            BottomNavItem(
                icon = Icons.Default.Description,
                label = "StudyNote",
                isSelected = activeSubView == WaecHubSubView.FORMULAS,
                onClick = onNavigateToStudyNote
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PureWhite else Color(0xFF666666),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isSelected) PureWhite else Color(0xFF666666),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 2.dp)
                    .background(PureWhite, RoundedCornerShape(1.dp))
            )
        }
    }
}

/**
 * EXAM TIPS / SYLLABUS MODAL
 */
@Composable
private fun ExamTipsModal(
    examTitle: String,
    tips: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, Color(0xFF333333)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = examTitle,
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                tips.forEachIndexed { idx, tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = tip,
                            color = TextMutedGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                    Text("Got It", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
