package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LoopingVideoPlayer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

/**
 * Standard Study Buddy Welcome Screen with Sign Up and Log In.
 * Features the looping waving/smiling robot video (borderless, no background, muted).
 */
@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onLogIn: () -> Unit,
    onDemoLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- ANIMATED WAVING ROBOT VIDEO (LOOPING, MUTED, NO BORDER, NO BACKGROUND) ---
            LoopingVideoPlayer(
                videoRawResId = R.raw.welcome_bot,
                hasBorder = false,
                backgroundColor = Color.Transparent,
                showMuteBadge = false,
                modifier = Modifier
                    .width(220.dp)
                    .height(290.dp)
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Active AI Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ChatGptPillDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI STEM TUTOR ONLINE",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Brand Title
            Text(
                text = "Study Buddy",
                color = PureWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Your personal AI study partner for solving homework, mastering difficult concepts, and staying focused.",
                color = TextMutedGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 340.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Feature Highlights
            Column(
                modifier = Modifier.widthIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WelcomeFeatureRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Homework Camera Scanner",
                    subtitle = "Snap textbook problems for instant step-by-step solutions"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Nigerian English Voice Tutoring",
                    subtitle = "Listen to clear, natural spoken explanations"
                )
                WelcomeFeatureRow(
                    icon = Icons.Default.Timer,
                    title = "Pomodoro Focus Timer",
                    subtitle = "Custom study intervals with sound and push notifications"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- SIGN UP & LOG IN BUTTONS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary: Sign Up
                Button(
                    onClick = onSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_sign_up_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Sign up",
                        color = TrueBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TrueBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Secondary: Log In
                OutlinedButton(
                    onClick = onLogIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_log_in_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = "Log in",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick access: Continue as Guest / Demo Student
                TextButton(
                    onClick = onDemoLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_demo_button")
                ) {
                    Text(
                        text = "Continue as Demo Student",
                        color = TextSubtle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(ChatGptPillDark, CircleShape),
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
                text = title,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMutedGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
