package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.QuizState

@Composable
fun QuizGeneratorScreen(
    state: QuizState,
    onUpdateConfig: (subject: String?, topic: String?, difficulty: String?) -> Unit,
    onGenerateQuiz: () -> Unit,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetryQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "History", "Literature")
    val difficulties = listOf("Middle School", "High School", "College / AP")

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
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI Quiz Generator",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Custom multiple-choice testing with mistake analysis",
                    color = TextMutedGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quiz Config Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 10.dp,
            borderColor = BorderLight,
            backgroundColor = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Subject & Topic",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subject row
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
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { onUpdateConfig(sub, null, null) },
                            color = if (isSel) PureWhite else TrueBlack
                        ) {
                            Text(
                                text = sub,
                                color = if (isSel) TrueBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Topic Input
                OutlinedTextField(
                    value = state.topic,
                    onValueChange = { onUpdateConfig(null, it, null) },
                    placeholder = { Text("Topic (e.g. 'Quadratic Equations', 'Newton's Laws')", fontSize = 12.sp, color = TextSubtle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_topic_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Difficulty selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    difficulties.forEach { diff ->
                        val isSel = state.difficulty == diff
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { onUpdateConfig(null, null, diff) },
                            color = if (isSel) PureWhite else TrueBlack
                        ) {
                            Text(
                                text = diff,
                                color = if (isSel) TrueBlack else TextMutedGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onGenerateQuiz,
                    enabled = !state.isGenerating && state.topic.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_generate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(color = TrueBlack, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesizing Questions...", color = TrueBlack, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERATE 4-QUESTION QUIZ", color = TrueBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }

        // Notice or Error
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                color = DarkSurface
            ) {
                Text(
                    text = state.error,
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Score Celebration Banner
        AnimatedVisibility(visible = state.isSubmitted) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = PureWhite,
                    backgroundColor = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(1.dp, PureWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.score == state.questions.size) "Perfect Score! 100%" else "Quiz Completed",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Score: ${state.score} / ${state.questions.size}",
                                color = TextMutedGray,
                                fontSize = 13.sp
                            )
                        }
                        OutlinedButton(
                            onClick = onRetryQuiz,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                            ),
                            modifier = Modifier.testTag("quiz_retry_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", color = PureWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Questions List
        if (state.questions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            state.questions.forEachIndexed { qIdx, question ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    cornerRadius = 10.dp,
                    borderColor = BorderLight,
                    backgroundColor = DarkSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Question Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${qIdx + 1}.  ${question.question}",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options
                        question.options.forEachIndexed { optIdx, optText ->
                            val isSelected = state.userAnswers[qIdx] == optIdx
                            val isCorrect = question.correctIndex == optIdx

                            val borderColor = when {
                                !state.isSubmitted && isSelected -> PureWhite
                                state.isSubmitted && isCorrect -> PureWhite
                                state.isSubmitted && isSelected && !isCorrect -> Color(0xFF666666)
                                else -> BorderSubtle
                            }

                            val bgColor = when {
                                !state.isSubmitted && isSelected -> Color(0xFF242424)
                                state.isSubmitted && isCorrect -> Color(0xFF2A2A2A)
                                state.isSubmitted && isSelected && !isCorrect -> Color(0xFF1F1F1F)
                                else -> TrueBlack
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable(enabled = !state.isSubmitted) {
                                        onSelectAnswer(qIdx, optIdx)
                                    }
                                    .testTag("quiz_q${qIdx}_opt$optIdx"),
                                color = bgColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = optText,
                                        color = if (isSelected) PureWhite else TextMutedGray,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (state.isSubmitted) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Correct",
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Incorrect",
                                                tint = TextMutedGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation of Mistake
                        if (state.isSubmitted) {
                            Spacer(modifier = Modifier.height(10.dp))
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
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
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

            // Submit Button
            if (!state.isSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSubmitQuiz,
                    enabled = state.userAnswers.size == state.questions.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_submit_answers_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "SUBMIT & GRADE QUIZ (${state.userAnswers.size}/${state.questions.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueBlack,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
