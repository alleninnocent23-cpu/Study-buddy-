package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.QuizState

@Composable
fun QuizGeneratorScreen(
    state: QuizState,
    onUpdateConfig: (subject: String?, topic: String?, difficulty: String?, questionCount: Int?) -> Unit,
    onGenerateQuiz: () -> Unit,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPrevQuestion: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetryQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "History", "Literature")
    val difficulties = listOf("Middle School", "High School", "College / AP")
    val questionCountOptions = listOf(10, 25, 50)

    var reviewFilter by remember { mutableStateOf("all") } // "all", "incorrect", "correct"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, BorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI 50-Question Quiz Generator",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Interactive question-by-question testing with instant score overview",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Configuration Card (shown before generating or expandable when desired)
        if (state.questions.isEmpty() || !state.isSubmitted) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                borderColor = BorderLight,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Select Academic Subject",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { sub ->
                            val isSel = state.subject == sub
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onUpdateConfig(sub, null, null, null) },
                                color = if (isSel) PureWhite else TrueBlack
                            ) {
                                Text(
                                    text = sub,
                                    color = if (isSel) TrueBlack else PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. Specific Topic or Chapter",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = state.topic,
                        onValueChange = { onUpdateConfig(null, it, null, null) },
                        placeholder = { Text("e.g. Quadratic Equations, Newton's Laws...", color = TextMutedGray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            cursorColor = PureWhite,
                            focusedContainerColor = TrueBlack,
                            unfocusedContainerColor = TrueBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_topic_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Count Selector Row (10, 25, 50 questions)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Number of Questions",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2838),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChatGptPlusStarBlue)
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
                        questionCountOptions.forEach { count ->
                            val isSel = state.questionCount == count
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onUpdateConfig(null, null, null, count) },
                                color = if (isSel) PureWhite else TrueBlack
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "4. Academic Difficulty",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        difficulties.forEach { diff ->
                            val isSel = state.difficulty == diff
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onUpdateConfig(null, null, diff, null) },
                                color = if (isSel) PureWhite else TrueBlack
                            ) {
                                Text(
                                    text = diff,
                                    color = if (isSel) TrueBlack else PureWhite,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Generate Quiz Button
                    Button(
                        onClick = onGenerateQuiz,
                        enabled = !state.isGenerating && state.topic.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack,
                            disabledContainerColor = Color(0xFF333333),
                            disabledContentColor = TextMutedGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("quiz_generate_button")
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(color = TrueBlack, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating ${state.questionCount} Questions...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate ${state.questionCount}-Question Quiz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Interactive Question Pager Mode (One question at a time with Next)
        if (state.questions.isNotEmpty() && !state.isSubmitted) {
            val totalQuestions = state.questions.size
            val currentIndex = state.currentQuestionIndex.coerceIn(0, totalQuestions - 1)
            val currentQuestion = state.questions[currentIndex]
            val answeredCount = state.userAnswers.size

            Spacer(modifier = Modifier.height(16.dp))

            // Quiz Status & Progress Bar
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = BorderLight,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of $totalQuestions",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$answeredCount / $totalQuestions answered",
                            color = if (answeredCount == totalQuestions) ChatGptPlusStarBlue else TextMutedGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / totalQuestions.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PureWhite,
                        trackColor = Color(0xFF222222),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Question jump row (1..50)
                    Text(
                        text = "Quick Question Navigation:",
                        color = TextMutedGray,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(state.questions) { idx, _ ->
                            val isCurrent = idx == currentIndex
                            val isAnswered = state.userAnswers.containsKey(idx)
                            val pillBg = when {
                                isCurrent -> PureWhite
                                isAnswered -> Color(0xFF253328)
                                else -> TrueBlack
                            }
                            val pillBorder = when {
                                isCurrent -> PureWhite
                                isAnswered -> Color(0xFF4CAF50)
                                else -> BorderSubtle
                            }
                            val pillTextColor = when {
                                isCurrent -> TrueBlack
                                isAnswered -> Color(0xFF81C784)
                                else -> TextMutedGray
                            }

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pillBg)
                                    .border(1.dp, pillBorder, RoundedCornerShape(6.dp))
                                    .clickable { onJumpToQuestion(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = pillTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent || isAnswered) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Question Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PureWhite,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Q${currentIndex + 1}. ${currentQuestion.question}",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options A, B, C, D
                    currentQuestion.options.forEachIndexed { optIdx, optText ->
                        val isSelected = state.userAnswers[currentIndex] == optIdx

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PureWhite else BorderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectAnswer(currentIndex, optIdx)
                                }
                                .testTag("quiz_opt_$optIdx"),
                            color = if (isSelected) Color(0xFF262626) else TrueBlack
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, if (isSelected) PureWhite else TextSubtle, CircleShape)
                                        .background(if (isSelected) PureWhite else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TrueBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = optText,
                                    color = if (isSelected) PureWhite else Color(0xFFDDDDDD),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigation Buttons: [Previous] [Next / Submit]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onPrevQuestion,
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (currentIndex > 0) BorderLight else BorderSubtle)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (currentIndex > 0) PureWhite else TextSubtle)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", color = if (currentIndex > 0) PureWhite else TextSubtle, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        if (currentIndex < totalQuestions - 1) {
                            Button(
                                onClick = onNextQuestion,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = TrueBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quiz_next_button")
                            ) {
                                Text("Next Question", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrueBlack)
                            }
                        } else {
                            Button(
                                onClick = onSubmitQuiz,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = TrueBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quiz_submit_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = TrueBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Optional early submit bar if user wants to finish anytime
                    if (currentIndex < totalQuestions - 1 && answeredCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Submit Quiz Now ($answeredCount of $totalQuestions answered)",
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { onSubmitQuiz() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Comprehensive Score Overview Section (Shown after Submit)
        if (state.isSubmitted && state.questions.isNotEmpty()) {
            val totalQuestions = state.questions.size
            val percentage = if (totalQuestions > 0) (state.score * 100) / totalQuestions else 0
            val mistakesCount = totalQuestions - state.score

            val performanceTier = when {
                percentage >= 90 -> "Grade A - Outstanding Mastery!"
                percentage >= 80 -> "Grade B - Strong Academic Knowledge!"
                percentage >= 70 -> "Grade C - Solid Effort, Review Key Concepts"
                percentage >= 60 -> "Grade D - Passing, Study Mistakes Below"
                else -> "Needs Revision - Review Detailed Solutions Below"
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Overview Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PureWhite,
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E261E))
                                .border(2.dp, PureWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quiz Score Overview",
                                color = TextMutedGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${state.score} / $totalQuestions",
                                color = PureWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$percentage% Score - $performanceTier",
                                color = if (percentage >= 70) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Score statistics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = TrueBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Correct", color = TextMutedGray, fontSize = 11.sp)
                                Text("${state.score}", color = Color(0xFF81C784), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = TrueBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Incorrect", color = TextMutedGray, fontSize = 11.sp)
                                Text("$mistakesCount", color = if (mistakesCount > 0) Color(0xFFE57373) else PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = TrueBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", color = TextMutedGray, fontSize = 11.sp)
                                Text("$totalQuestions", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Retake Quiz / Build New Quiz
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRetryQuiz,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retake Quiz", color = PureWhite, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onGenerateQuiz,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = TrueBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Quiz", color = TrueBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Question-by-Question Review with Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Detailed Answer & Explanation Review",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Filter chips: All, Mistakes, Correct
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("all" to "All", "incorrect" to "Mistakes", "correct" to "Correct").forEach { (key, label) ->
                        val isSel = reviewFilter == key
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) PureWhite else TrueBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle),
                            modifier = Modifier.clickable { reviewFilter = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) TrueBlack else TextMutedGray,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filtered questions review list
            val filteredQuestions = state.questions.filterIndexed { idx, q ->
                val isCorrect = state.userAnswers[idx] == q.correctIndex
                when (reviewFilter) {
                    "incorrect" -> !isCorrect
                    "correct" -> isCorrect
                    else -> true
                }
            }

            if (filteredQuestions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (reviewFilter == "incorrect") "No mistakes! You answered all questions correctly." else "No questions matching this filter.",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                state.questions.forEachIndexed { qIdx, question ->
                    val isCorrect = state.userAnswers[qIdx] == question.correctIndex
                    val shouldDisplay = when (reviewFilter) {
                        "incorrect" -> !isCorrect
                        "correct" -> isCorrect
                        else -> true
                    }

                    if (shouldDisplay) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            cornerRadius = 10.dp,
                            borderColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFF7F2B2B),
                            backgroundColor = DarkSurface
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF81C784) else Color(0xFFE57373),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Question ${qIdx + 1}",
                                        color = if (isCorrect) Color(0xFF81C784) else Color(0xFFE57373),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = question.question,
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Options
                                question.options.forEachIndexed { optIdx, optText ->
                                    val isSelectedByUser = state.userAnswers[qIdx] == optIdx
                                    val isTheCorrectOption = question.correctIndex == optIdx

                                    val optBorderColor = when {
                                        isTheCorrectOption -> Color(0xFF81C784)
                                        isSelectedByUser && !isTheCorrectOption -> Color(0xFFE57373)
                                        else -> BorderSubtle
                                    }

                                    val optBg = when {
                                        isTheCorrectOption -> Color(0xFF1B2E1E)
                                        isSelectedByUser && !isTheCorrectOption -> Color(0xFF2E1B1B)
                                        else -> TrueBlack
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, optBorderColor, RoundedCornerShape(6.dp)),
                                        color = optBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = optText,
                                                color = if (isTheCorrectOption || isSelectedByUser) PureWhite else TextMutedGray,
                                                fontSize = 12.sp,
                                                fontWeight = if (isTheCorrectOption || isSelectedByUser) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isTheCorrectOption) {
                                                Text(
                                                    text = "Correct Answer",
                                                    color = Color(0xFF81C784),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (isSelectedByUser) {
                                                Text(
                                                    text = "Your Choice",
                                                    color = Color(0xFFE57373),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Explanation
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp)),
                                    color = TrueBlack
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Explanation & Solution",
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.explanation,
                                            color = TextMutedGray,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
