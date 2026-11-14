package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamType
import com.example.data.model.WaecJambQuestion
import com.example.data.repository.WaecJambQuestionBank
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PastQuestionSearchView(
    initialQuery: String = "",
    initialExamFilter: ExamType? = ExamType.WAEC,
    initialSubjectFilter: String = "All Subjects",
    isSpeaking: Boolean = false,
    onBack: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onPracticeTopic: (exam: ExamType, subject: String, topic: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedExam by remember(initialExamFilter) { mutableStateOf(initialExamFilter) }
    var selectedSubject by remember(initialSubjectFilter) { mutableStateOf(initialSubjectFilter) }
    var selectedTopic by remember { mutableStateOf("All Topics") }

    val availableSubjects = remember { WaecJambQuestionBank.getAllSearchSubjects() }
    val availableTopics = remember(selectedSubject) {
        WaecJambQuestionBank.getTopicsForSubject(selectedSubject)
    }

    val searchResults = remember(searchQuery, selectedExam, selectedSubject, selectedTopic) {
        WaecJambQuestionBank.searchPastQuestions(
            query = searchQuery,
            examFilter = selectedExam,
            subjectFilter = selectedSubject,
            topicFilter = selectedTopic
        )
    }

    val distinctYearsCount = remember(searchResults) {
        searchResults.map { it.year }.distinct().size
    }

    val quickQueries = remember {
        listOf(
            "Physics",
            "Mathematics",
            "Chemistry",
            "Biology",
            "Current Electricity",
            "Quadratic",
            "Genetics",
            "Economics",
            "Government"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(TrueBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .testTag("past_question_search_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Study Hub",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Past Question Search Engine",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Search WAEC & JAMB subjects, topics & years (1997–2026) with explanations",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SEARCH INPUT ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search subject or topic (e.g. Physics, Electricity, Quadratic)...",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("past_question_search_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextMutedGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedContainerColor = Color(0xFF121212),
                unfocusedContainerColor = Color(0xFF121212),
                focusedBorderColor = PureWhite,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("waec_jamb_question_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- EXAM FILTER ROW (ALL / WAEC / JAMB) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                null to "All Exams",
                ExamType.WAEC to "WAEC",
                ExamType.JAMB to "JAMB"
            ).forEach { (examType, label) ->
                val isSelected = selectedExam == examType
                val tagSuffix = examType?.code?.lowercase() ?: "all"
                Surface(
                    onClick = { selectedExam = examType },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PureWhite else Color(0xFF141414),
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else Color(0xFF333333)),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .testTag("search_exam_filter_$tagSuffix")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TrueBlack else PureWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- SUBJECT FILTER CHIPS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            availableSubjects.forEach { subject ->
                val isSelected = selectedSubject == subject
                Surface(
                    onClick = {
                        selectedSubject = subject
                        selectedTopic = "All Topics"
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF2B2B2B) else TrueBlack,
                    border = BorderStroke(1.dp, if (isSelected) PureWhite else Color(0xFF2A2A2A)),
                    modifier = Modifier.testTag("search_subject_chip_${subject.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = subject,
                        color = if (isSelected) PureWhite else TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- TOPIC FILTER CHIPS (SHOWN WHEN SUBJECT CHOSEN OR MATCHING TOPICS EXIST) ---
        if (availableTopics.size > 1) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableTopics.forEach { topic ->
                    val isSelected = selectedTopic == topic
                    Surface(
                        onClick = { selectedTopic = topic },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF1F2937) else Color(0xFF0E0E0E),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF242424)
                        )
                    ) {
                        Text(
                            text = topic,
                            color = if (isSelected) Color(0xFF7DD3FC) else TextSubtle,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- QUICK SUGGESTION CHIPS WHEN SEARCH IS BLANK ---
        if (searchQuery.isBlank() && selectedSubject == "All Subjects") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickQueries.forEach { suggestion ->
                    Surface(
                        onClick = { searchQuery = suggestion },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF18181B),
                        border = BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Text(
                            text = "🔍 $suggestion",
                            color = Color(0xFFE4E4E7),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- LIVE MATCH SUMMARY BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${searchResults.size} Questions Found across $distinctYearsCount Years",
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("past_question_search_count")
            )
            if (searchQuery.isNotBlank() || selectedSubject != "All Subjects" || selectedTopic != "All Topics") {
                Text(
                    text = "Reset filters",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            searchQuery = ""
                            selectedSubject = "All Subjects"
                            selectedTopic = "All Topics"
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- RESULTS LIST ---
        if (searchResults.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMutedGray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching past questions found",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching a subject like Physics, Mathematics, Biology, Chemistry, Economics, or Government, or switch to 'All Exams'.",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQueries.take(5).forEach { chip ->
                            Surface(
                                onClick = {
                                    searchQuery = chip
                                    selectedSubject = "All Subjects"
                                    selectedTopic = "All Topics"
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite
                            ) {
                                Text(
                                    text = chip,
                                    color = TrueBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("past_question_search_results_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                itemsIndexed(
                    items = searchResults,
                    key = { _, item -> item.id }
                ) { index, question ->
                    PastQuestionSearchResultCard(
                        question = question,
                        index = index + 1,
                        isSpeaking = isSpeaking,
                        onPlayAudio = onPlayAudio,
                        onStopAudio = onStopAudio,
                        onPracticeTopic = {
                            onPracticeTopic(question.exam, question.subject, question.topic)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PastQuestionSearchResultCard(
    question: WaecJambQuestion,
    index: Int,
    isSpeaking: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onPracticeTopic: () -> Unit
) {
    var showExplanation by remember(question.id) { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_result_card_${question.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Year, Exam & Subject Badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PureWhite
                ) {
                    Text(
                        text = "${question.exam.code} ${question.year}",
                        color = TrueBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF222222),
                    border = BorderStroke(1.dp, Color(0xFF3A3A3A))
                ) {
                    Text(
                        text = question.subject,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B2432),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = question.topic,
                        color = Color(0xFF7DD3FC),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1F1F1F)
                ) {
                    Text(
                        text = "${question.session} • ${question.difficulty}",
                        color = TextMutedGray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = "Q$index. ${question.question}",
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Answer Options (Correct Answer Highlighted)
            val labels = listOf("A", "B", "C", "D")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEachIndexed { idx, opt ->
                    val isCorrect = idx == question.correctIndex
                    val letter = labels.getOrElse(idx) { "${idx + 1}" }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCorrect) Color(0xFF064E3B).copy(alpha = 0.45f) else Color(0xFF1B1B1B),
                        border = BorderStroke(
                            1.dp,
                            if (isCorrect) Color(0xFF34D399) else Color(0xFF2B2B2B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$letter.",
                                color = if (isCorrect) Color(0xFF34D399) else TextMutedGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                text = opt,
                                color = PureWhite,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCorrect) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct answer",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanation Toggle Header
            Surface(
                onClick = { showExplanation = !showExplanation },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1C1C1E),
                border = BorderStroke(1.dp, Color(0xFF2F2F33)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step-by-Step Explanation (${question.correctAnswerLetter})",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showExplanation) "Hide explanation" else "Show explanation",
                        tint = TextMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showExplanation) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF101216))
                        .border(1.dp, Color(0xFF262A33), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = question.explanation,
                        color = Color(0xFFE5E7EB),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    if (question.whyOthersAreWrong.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF242830))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Why other options are wrong:",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        question.whyOthersAreWrong.forEach { (letter, reason) ->
                            Text(
                                text = "• Option $letter: $reason",
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    if (question.relatedConcepts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Key Concepts: ${question.relatedConcepts.joinToString(" • ")}",
                            color = Color(0xFF7DD3FC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isSpeaking) {
                                    onStopAudio()
                                } else {
                                    onPlayAudio(
                                        "${question.exam.code} ${question.year} ${question.subject}. ${question.question}. The correct answer is Option ${question.correctAnswerLetter}. ${question.explanation}"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeaking) "Stop Audio" else "Listen",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onPracticeTopic,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = TrueBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .heightIn(min = 42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = TrueBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Practice Topic",
                                color = TrueBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
