package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TrueBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated StudyBuddy Bot mascot for the Welcome screen.
 * Features:
 *  - Organic body movement: floating hover (up & down) and subtle breathing scale
 *  - Waving motion: periodic energetic waving gesture rocking with lively physics
 *  - Winking animation: right eye winks with a glowing cyan arc and twinkling star sparkle
 *  - Interactive tap: tapping the bot triggers an immediate greeting wave and wink!
 */
@Composable
fun AnimatedStudyBuddyBot(
    modifier: Modifier = Modifier,
    size: Dp = 290.dp
) {
    val coroutineScope = rememberCoroutineScope()
    var showTapGreeting by remember { mutableStateOf(false) }

    // --- Infinite Hover & Body Breathing Transitions ---
    val infiniteTransition = rememberInfiniteTransition(label = "study_buddy_bot_movement")

    // Vertical floating (hovering up and down)
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_float_y"
    )

    // Gentle body sway tilt
    val bodySway by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_body_sway"
    )

    // Breathing depth scale
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_breathing"
    )

    // --- Periodic Waving and Winking Loop ---
    var waveAngleOffset by remember { mutableFloatStateOf(0f) }
    var waveBounceOffset by remember { mutableFloatStateOf(0f) }
    val winkProgress = remember { Animatable(0f) }
    val sparkleScale = remember { Animatable(0f) }

    // Coroutine loop driving cyclic waving and winking
    LaunchedEffect(Unit) {
        while (true) {
            // Calm hover phase (1.8 seconds)
            delay(1800)

            // Wave phase: 3 energetic wave oscillations
            val waveDuration = 1200L
            val waveStartTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - waveStartTime < waveDuration) {
                val elapsed = (System.currentTimeMillis() - waveStartTime).toFloat() / waveDuration
                // 3 full sine waves with an ease-in/ease-out envelope
                val envelope = sin(elapsed * PI.toFloat())
                waveAngleOffset = sin(elapsed * 2f * PI.toFloat() * 3f) * 8f * envelope
                waveBounceOffset = -sin(elapsed * PI.toFloat()) * 6f
                delay(16)
            }
            waveAngleOffset = 0f
            waveBounceOffset = 0f

            // Wink phase immediately during/after wave
            // Close eye into cute wink arc
            winkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )

            // Twinkle star sparkle at peak wink
            sparkleScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            delay(160)
            sparkleScale.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 160, easing = LinearEasing)
            )

            // Hold wink briefly, then reopen eye
            delay(80)
            winkProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )

            // Pause before next wave & wink cycle
            delay(2200)
        }
    }

    // Tap handler for on-demand wave and wink
    val onBotTapped = {
        coroutineScope.launch {
            showTapGreeting = true
            // Quick wave burst
            val waveDuration = 900L
            val waveStartTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - waveStartTime < waveDuration) {
                val elapsed = (System.currentTimeMillis() - waveStartTime).toFloat() / waveDuration
                val envelope = sin(elapsed * PI.toFloat())
                waveAngleOffset = sin(elapsed * 2f * PI.toFloat() * 3.5f) * 11f * envelope
                waveBounceOffset = -sin(elapsed * PI.toFloat()) * 8f
                delay(16)
            }
            waveAngleOffset = 0f
            waveBounceOffset = 0f

            // Wink
            winkProgress.animateTo(1f, tween(150))
            sparkleScale.animateTo(1f, tween(180))
            delay(150)
            sparkleScale.animateTo(0f, tween(140))
            winkProgress.animateTo(0f, tween(180))

            delay(1500)
            showTapGreeting = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(TrueBlack)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onBotTapped() }
            .testTag("animated_study_buddy_bot"),
        contentAlignment = Alignment.Center
    ) {
        // Main Bot Character with composite body movement + wave transformations
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Floating hover translation
                    translationY = floatY + waveBounceOffset
                    // Combined body sway and waving angle
                    rotationZ = bodySway + waveAngleOffset
                    // Breathing scale
                    scaleX = breathingScale
                    scaleY = breathingScale
                },
            contentAlignment = Alignment.Center
        ) {
            // Base Robot Image
            Image(
                painter = painterResource(id = R.drawable.study_buddy_welcome),
                contentDescription = "StudyBuddy Bot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Canvas Eye Overlay for the Wink Animation
            // Positioned over the robot's right eye (normalized X=0.584, Y=0.275 in 976x1056 image)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.toPx()
                val canvasHeight = size.toPx() * (1056f / 976f) // Maintain image aspect ratio

                // Right eye center coordinates on canvas
                val eyeCenterX = canvasWidth * 0.584f
                val eyeCenterY = canvasHeight * 0.278f
                val eyeWidth = canvasWidth * 0.082f
                val eyeHeight = canvasHeight * 0.038f

                val currentWink = winkProgress.value
                if (currentWink > 0.01f) {
                    // 1. Visor mask: Matches the dark obsidian visor (#0B0E12) to smoothly cover open eye
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0A0D12),
                                Color(0xFF080A0E),
                                Color(0x00000000)
                            ),
                            center = Offset(eyeCenterX, eyeCenterY),
                            radius = eyeWidth * 0.85f
                        ),
                        topLeft = Offset(eyeCenterX - eyeWidth * 0.65f, eyeCenterY - eyeHeight * 0.8f),
                        size = androidx.compose.ui.geometry.Size(eyeWidth * 1.3f, eyeHeight * 1.6f),
                        alpha = currentWink
                    )

                    // 2. Animated Winking Eye Arc (Glowing Cyan)
                    // Interpolates from flat line to a cheerful winking smile arc
                    val arcDip = eyeHeight * 0.45f * currentWink
                    val winkPath = Path().apply {
                        moveTo(eyeCenterX - eyeWidth * 0.42f, eyeCenterY - eyeHeight * 0.1f)
                        quadraticTo(
                            eyeCenterX,
                            eyeCenterY + arcDip,
                            eyeCenterX + eyeWidth * 0.42f,
                            eyeCenterY - eyeHeight * 0.1f
                        )
                    }

                    // Soft luminous cyan bloom/glow
                    drawPath(
                        path = winkPath,
                        color = Color(0x664FE3FF),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Sharp bright cyan core line
                    drawPath(
                        path = winkPath,
                        color = Color(0xFF67EEF8),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner brilliant highlight
                    drawPath(
                        path = winkPath,
                        color = Color(0xFFE0F7FA),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Winking Star Sparkle (Twinkles at peak wink near eye corner)
                    val currentSparkle = sparkleScale.value
                    if (currentSparkle > 0.05f) {
                        val sparkleX = eyeCenterX + eyeWidth * 0.48f
                        val sparkleY = eyeCenterY - eyeHeight * 0.35f
                        val sparkleRadius = 8.dp.toPx() * currentSparkle

                        // 4-Point Star Sparkle
                        val starPath = Path().apply {
                            moveTo(sparkleX, sparkleY - sparkleRadius)
                            quadraticTo(sparkleX, sparkleY, sparkleX + sparkleRadius, sparkleY)
                            quadraticTo(sparkleX, sparkleY, sparkleX, sparkleY + sparkleRadius)
                            quadraticTo(sparkleX, sparkleY, sparkleX - sparkleRadius, sparkleY)
                            quadraticTo(sparkleX, sparkleY, sparkleX, sparkleY - sparkleRadius)
                            close()
                        }

                        // Sparkle outer glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x995DE6FF), Color(0x00000000)),
                                center = Offset(sparkleX, sparkleY),
                                radius = sparkleRadius * 1.8f
                            ),
                            radius = sparkleRadius * 1.8f,
                            center = Offset(sparkleX, sparkleY)
                        )

                        // Sparkle core
                        drawPath(
                            path = starPath,
                            color = PureWhite
                        )
                    }
                }
            }
        }

        // Tap Speech Bubble Indicator
        if (showTapGreeting) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 8.dp)
                    .background(Color(0xE61A1D24), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Hello! 👋✨",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
