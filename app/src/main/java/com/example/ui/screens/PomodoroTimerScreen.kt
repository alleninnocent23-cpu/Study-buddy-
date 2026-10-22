package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.PomodoroConfig
import com.example.viewmodel.PomodoroMode
import com.example.viewmodel.PomodoroState

@Composable
fun PomodoroTimerScreen(
    state: PomodoroState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onSwitchMode: (PomodoroMode) -> Unit,
    onUpdateConfig: (work: Int, shortBreak: Int, longBreak: Int, cycles: Int, autoBreaks: Boolean, autoStudy: Boolean) -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showConfigDialog by remember { mutableStateOf(false) }

    // Check Android 13+ Notification permission
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    // Format MM:SS
    val minutes = state.totalSecondsRemaining / 60
    val seconds = state.totalSecondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Progress fraction (1.0 down to 0.0)
    val progressFraction = if (state.initialTotalSeconds > 0) {
        (state.totalSecondsRemaining.toFloat() / state.initialTotalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 500),
        label = "pomodoro_progress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pomodoro Study Focus",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Structured study intervals with push notification alerts",
                    color = TextMutedGray,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { showConfigDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ChatGptPillDark)
                    .testTag("pomodoro_open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Timer Settings",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification status banner
        NotificationStatusCard(
            hasPermission = hasNotificationPermission,
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Mode Switcher Tabs (Study Focus, Short Break, Long Break)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PomodoroModeTab(
                    title = "Study (${state.config.workMinutes}m)",
                    isSelected = state.mode == PomodoroMode.WORK,
                    onClick = { onSwitchMode(PomodoroMode.WORK) },
                    modifier = Modifier.weight(1f)
                )
                PomodoroModeTab(
                    title = "Break (${state.config.shortBreakMinutes}m)",
                    isSelected = state.mode == PomodoroMode.SHORT_BREAK,
                    onClick = { onSwitchMode(PomodoroMode.SHORT_BREAK) },
                    modifier = Modifier.weight(1f)
                )
                PomodoroModeTab(
                    title = "Long (${state.config.longBreakMinutes}m)",
                    isSelected = state.mode == PomodoroMode.LONG_BREAK,
                    onClick = { onSwitchMode(PomodoroMode.LONG_BREAK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Circular Timer View
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circular canvas ring
            val ringColor = when (state.mode) {
                PomodoroMode.WORK -> PureWhite
                PomodoroMode.SHORT_BREAK -> Color(0xFF64B5F6)
                PomodoroMode.LONG_BREAK -> Color(0xFF81C784)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Background track
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Active progress sweep
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inside Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Mode Tag Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ChatGptPillDark,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = when (state.mode) {
                            PomodoroMode.WORK -> "DEEP FOCUS"
                            PomodoroMode.SHORT_BREAK -> "SHORT BREAK"
                            PomodoroMode.LONG_BREAK -> "LONG BREAK"
                        },
                        color = ringColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Digital Timer Numbers
                Text(
                    text = timeFormatted,
                    color = PureWhite,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1.5).sp
                )

                // Session Round Indicator (e.g. Session 2 of 4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    for (i in 1..state.config.cyclesBeforeLongBreak) {
                        val isDone = i < state.currentCycleInRound
                        val isCurrent = i == state.currentCycleInRound && state.mode == PomodoroMode.WORK
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone -> PureWhite
                                        isCurrent -> ringColor
                                        else -> Color.White.copy(alpha = 0.25f)
                                    }
                                )
                        )
                    }
                }

                Text(
                    text = "Round ${state.currentCycleInRound} of ${state.config.cyclesBeforeLongBreak}",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Minute Adjustments (-1m, +1m, +5m)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onAdjustMinutes(-1) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                modifier = Modifier.height(34.dp)
            ) {
                Text("-1 min", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onAdjustMinutes(1) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                modifier = Modifier.height(34.dp)
            ) {
                Text("+1 min", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onAdjustMinutes(5) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                modifier = Modifier.height(34.dp)
            ) {
                Text("+5 min", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Timer Actions (Play/Pause, Reset, Skip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, BorderLight, CircleShape)
                    .testTag("pomodoro_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Primary Play / Pause Button
            Button(
                onClick = {
                    if (state.isRunning) onPause() else onStart()
                },
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = TrueBlack
                ),
                modifier = Modifier
                    .height(60.dp)
                    .width(180.dp)
                    .testTag("pomodoro_toggle_play_button")
            ) {
                Icon(
                    imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isRunning) "Pause" else "Start",
                    tint = TrueBlack,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isRunning) "Pause" else "Start Focus",
                    color = TrueBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Skip Button
            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, BorderLight, CircleShape)
                    .testTag("pomodoro_skip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip Session",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Presets Row
        Text(
            text = "Focus Presets",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PresetCard(
                title = "Classic",
                subtitle = "25m / 5m",
                isSelected = state.config.workMinutes == 25 && state.config.shortBreakMinutes == 5,
                onClick = {
                    onUpdateConfig(25, 5, 15, 4, state.config.autoStartBreaks, state.config.autoStartStudy)
                },
                modifier = Modifier.weight(1f)
            )
            PresetCard(
                title = "Deep Focus",
                subtitle = "50m / 10m",
                isSelected = state.config.workMinutes == 50 && state.config.shortBreakMinutes == 10,
                onClick = {
                    onUpdateConfig(50, 10, 20, 3, state.config.autoStartBreaks, state.config.autoStartStudy)
                },
                modifier = Modifier.weight(1f)
            )
            PresetCard(
                title = "Sprint",
                subtitle = "15m / 3m",
                isSelected = state.config.workMinutes == 15 && state.config.shortBreakMinutes == 3,
                onClick = {
                    onUpdateConfig(15, 3, 10, 4, state.config.autoStartBreaks, state.config.autoStartStudy)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Study Statistics Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            backgroundColor = DarkSurface,
            borderColor = BorderLight
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Today's Study Progress",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.totalFocusMinutesToday}m / ${state.targetDailyMinutes}m Goal",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val dailyProgress = (state.totalFocusMinutesToday.toFloat() / state.targetDailyMinutes.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(dailyProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PureWhite)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Completed Sessions", value = "${state.completedWorkSessions}")
                    StatItem(label = "Total Focus Time", value = "${state.totalFocusMinutesToday} min")
                    StatItem(label = "Current Cycle", value = "${state.currentCycleInRound}/${state.config.cyclesBeforeLongBreak}")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Config Dialog Modal
    if (showConfigDialog) {
        PomodoroConfigDialog(
            config = state.config,
            onDismiss = { showConfigDialog = false },
            onSave = { w, sb, lb, c, ab, as_ ->
                onUpdateConfig(w, sb, lb, c, ab, as_)
                showConfigDialog = false
            }
        )
    }
}

@Composable
private fun PomodoroModeTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PureWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) TrueBlack else TextMutedGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun NotificationStatusCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (hasPermission) DarkSurface else Color(0xFF1E1700),
        border = BorderStroke(1.dp, if (hasPermission) BorderSubtle else Color(0xFFFFB300).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasPermission) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (hasPermission) PureWhite else Color(0xFFFFB300),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasPermission) "Push Notifications Active" else "Enable Push Notifications",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (hasPermission) {
                        "Your phone will alert with chimes and vibration when study or break time finishes."
                    } else {
                        "Allow notifications so you are alerted when it's time to study or rest."
                    },
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
            }
            if (!hasPermission) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ChatGptPillDark else DarkSurface,
        border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMutedGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = value, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextMutedGray, fontSize = 11.sp)
    }
}

/**
 * Custom Configuration Dialog for configuring Pomodoro intervals.
 */
@Composable
private fun PomodoroConfigDialog(
    config: PomodoroConfig,
    onDismiss: () -> Unit,
    onSave: (work: Int, shortBreak: Int, longBreak: Int, cycles: Int, autoBreaks: Boolean, autoStudy: Boolean) -> Unit
) {
    var workMin by remember { mutableStateOf(config.workMinutes) }
    var shortBreakMin by remember { mutableStateOf(config.shortBreakMinutes) }
    var longBreakMin by remember { mutableStateOf(config.longBreakMinutes) }
    var cycles by remember { mutableStateOf(config.cyclesBeforeLongBreak) }
    var autoBreaks by remember { mutableStateOf(config.autoStartBreaks) }
    var autoStudy by remember { mutableStateOf(config.autoStartStudy) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Customize Study Intervals",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Work duration
                IntervalStepper(
                    label = "Study Focus Duration",
                    value = workMin,
                    unit = "min",
                    onValueChange = { workMin = it.coerceIn(5, 90) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Short break
                IntervalStepper(
                    label = "Short Break Duration",
                    value = shortBreakMin,
                    unit = "min",
                    onValueChange = { shortBreakMin = it.coerceIn(1, 30) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Long break
                IntervalStepper(
                    label = "Long Break Duration",
                    value = longBreakMin,
                    unit = "min",
                    onValueChange = { longBreakMin = it.coerceIn(5, 60) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cycles count
                IntervalStepper(
                    label = "Sessions Before Long Break",
                    value = cycles,
                    unit = "cycles",
                    onValueChange = { cycles = it.coerceIn(1, 8) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(16.dp))

                // Auto-start breaks toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Start Breaks", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Automatically start break timer when study finishes", color = TextMutedGray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoBreaks,
                        onCheckedChange = { autoBreaks = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TrueBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = TextMutedGray,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-start study toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Start Study Sessions", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Automatically start focus session when break finishes", color = TextMutedGray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoStudy,
                        onCheckedChange = { autoStudy = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TrueBlack,
                            checkedTrackColor = PureWhite,
                            uncheckedThumbColor = TextMutedGray,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMutedGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(workMin, shortBreakMin, longBreakMin, cycles, autoBreaks, autoStudy)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Settings", color = TrueBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalStepper(
    label: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "$value $unit", color = TextMutedGray, fontSize = 12.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onValueChange(value - 5) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TrueBlack)
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PureWhite, modifier = Modifier.size(16.dp))
            }

            Text(
                text = "$value",
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onValueChange(value + 5) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TrueBlack)
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = PureWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}
