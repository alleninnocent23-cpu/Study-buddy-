package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.input.pointer.pointerInput
import java.util.Locale
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.example.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptBlueWave
import com.example.ui.theme.ChatGptDrawerBg
import com.example.ui.theme.ChatGptIconGray
import com.example.ui.theme.ChatGptInputPlaceholder
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusPillBg
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.ChatGptPlusTextBlue
import com.example.ui.theme.ChatGptUserBubble
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.MessageSender

/**
 * Top bar matching the exact design in the user screenshots:
 * - Empty state: [Menu button] [✦ Get Plus pill] [Chat bubble button]
 * - Active state: [Menu button] ... [Pill with (Edit pencil) and (⋮ dots)]
 */
@Composable
fun ChatGptTopBar(
    hasMessages: Boolean,
    onMenuClick: () -> Unit,
    onGetPlusClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onClearChat: () -> Unit,
    onSelectSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenuOverflow by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left 2-bar menu button
        TwoBarMenuButton(
            onClick = onMenuClick,
            modifier = Modifier.testTag("topbar_menu_button")
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!hasMessages) {
            // Middle: ✦ Get Plus pill
            GetPlusPill(
                onClick = onGetPlusClick,
                modifier = Modifier.testTag("topbar_get_plus_pill")
            )

            Spacer(modifier = Modifier.weight(1f))

            // Right: Circular button with chat bubble icon
            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ChatGptPillDark)
                    .testTag("topbar_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "New Chat",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // Right: Pill containing Edit pencil & ⋮ overflow dots
            Box {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ChatGptPillDark,
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = onNewChatClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_edit_pencil_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "New Chat",
                                tint = PureWhite,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { showMenuOverflow = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_overflow_dots_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenuOverflow,
                    onDismissRequest = { showMenuOverflow = false },
                    modifier = Modifier.background(ChatGptDrawerBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Conversation", color = PureWhite) },
                        onClick = {
                            showMenuOverflow = false
                            onNewChatClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Filter School Subject", color = PureWhite) },
                        onClick = {
                            showMenuOverflow = false
                            onSelectSubject()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Study Buddy Plus", color = ChatGptPlusTextBlue) },
                        onClick = {
                            showMenuOverflow = false
                            onGetPlusClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Messages", color = Color(0xFFFF5252)) },
                        onClick = {
                            showMenuOverflow = false
                            onClearChat()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Exact 2-bar menu button matching the top-left button in screenshots
 */
@Composable
fun TwoBarMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(ChatGptPillDark)
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val stroke = 2.dp.toPx()
            val cap = StrokeCap.Round
            // Top bar
            drawLine(
                color = Color.White,
                start = Offset(2.dp.toPx(), 6.dp.toPx()),
                end = Offset(16.dp.toPx(), 6.dp.toPx()),
                strokeWidth = stroke,
                cap = cap
            )
            // Bottom bar
            drawLine(
                color = Color.White,
                start = Offset(2.dp.toPx(), 12.dp.toPx()),
                end = Offset(16.dp.toPx(), 12.dp.toPx()),
                strokeWidth = stroke,
                cap = cap
            )
        }
    }
}

/**
 * ✦ Get Plus pill button matching the center of Screenshot 1
 */
@Composable
fun GetPlusPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = ChatGptPlusPillBg,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ChatGptPlusStarBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "Get Plus",
                color = ChatGptPlusTextBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Empty screen prompt suggestions from Screenshot 1:
 * - [🖼️] Create an image or sticker
 * - [✏️] Write or edit
 */
@Composable
fun ChatGptEmptyPrompts(
    onCreateImage: () -> Unit,
    onWriteEdit: () -> Unit,
    onScanHomework: () -> Unit,
    onQuizMe: () -> Unit,
    onPomodoroFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Option 1: Create an image or sticker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreateImage() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Create an image or sticker",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Option 2: Write or edit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWriteEdit() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Write or edit",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Option 3: Scan homework or diagram
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onScanHomework() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Scan homework or math worksheet",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Option 4: Quiz me on a topic
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onQuizMe() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Quiz me on STEM concepts",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Option 5: Pomodoro Study Focus Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPomodoroFocus() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HourglassBottom,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Start Pomodoro study timer",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
fun formatVoiceDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

/**
 * Bottom Input Pill Capsule:
 * - Left: Study Buddy avatar + (+) attachment button
 * - Center: TextField with "Ask Study Buddy" / "Reply to Study Buddy"
 * - Right: Mic dictation icon + Voice Note Recorder Button (Black background with white circular border ring and white waveform)
 * - Recording State: Transforms into the Voice Note Capsule (Black background, white border, circular white ring with recording dot, white animated waveform bars, live 0:0X timer, cancel and send actions)
 */
@Composable
fun ChatGptInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    hasActiveMessages: Boolean,
    attachedImage: Bitmap?,
    attachedFileName: String?,
    onClearAttachment: () -> Unit,
    onAddAttachment: () -> Unit,
    onVoiceDictation: () -> Unit,
    onOpenVoiceMode: () -> Unit,
    onHoldStartRecording: () -> Unit = {},
    onHoldFinishRecording: () -> Unit = {},
    onHoldCancelRecording: () -> Unit = {},
    isRecordingAudio: Boolean = false,
    recordingDurationSeconds: Int = 0,
    onSend: () -> Unit,
    isListening: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // If an attachment is ready, show sleek preview chip above input
        AnimatedVisibility(visible = attachedImage != null || attachedFileName != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ChatGptPillDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (attachedImage != null) {
                        Image(
                            bitmap = attachedImage.asImageBitmap(),
                            contentDescription = "Attachment",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attachedFileName ?: "Attached image",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Ready to send with question",
                            color = ChatGptInputPlaceholder,
                            fontSize = 10.sp
                        )
                    }
                    IconButton(onClick = onClearAttachment, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = PureWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (isRecordingAudio) {
            // Live Voice Note Recording Capsule:
            // Stadium pill shape, black background, crisp white border,
            // circular white ring with pulsing recording indicator, animated white waveform,
            // white live timer (e.g. 0:01, 0:02, 0:05), and cancel/send actions
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = TrueBlack,
                border = BorderStroke(1.5.dp, PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("voice_note_recording_capsule")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel / Discard Button
                    IconButton(
                        onClick = onHoldCancelRecording,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_voice_cancel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel voice note",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Circular ring with recording indicator
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, PureWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Animated White Audio Waveform Bars
                    VoiceNoteWaveform(
                        isPlaying = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp),
                        barColor = PureWhite
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Live Duration Timer in PureWhite (e.g. 0:01, 0:02, 0:05)
                    Text(
                        text = formatVoiceDuration(recordingDurationSeconds),
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button (White circular button with dark arrow)
                    IconButton(
                        onClick = onHoldFinishRecording,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .testTag("chat_voice_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send Voice Note",
                            tint = TrueBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Main Dark Input Capsule
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = ChatGptPillDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Study Buddy Bot Image at the left side of the type bar
                    Image(
                        painter = painterResource(id = R.drawable.ic_study_buddy_avatar),
                        contentDescription = "Study Buddy",
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("chat_input_bot_image"),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // + Plus Button
                    IconButton(
                        onClick = onAddAttachment,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chat_input_plus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add attachment",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Middle Text Field with flexible weight
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = if (hasActiveMessages) "Reply to Study Buddy" else "Ask Study Buddy",
                                color = ChatGptInputPlaceholder,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(PureWhite),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (text.isNotBlank() || attachedImage != null) {
                                        onSend()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_text_input")
                        )
                    }

                    // Mic Icon Button for dictation
                    IconButton(
                        onClick = onVoiceDictation,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("chat_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) Color(0xFFEF4444) else PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Voice Note Recorder / Send Button
                    if (text.isNotBlank() || attachedImage != null) {
                        // Send Arrow Button
                        IconButton(
                            onClick = onSend,
                            enabled = !isLoading,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .testTag("chat_send_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = TrueBlack,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Send",
                                    tint = TrueBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Voice Note Recorder Button styled in reference design:
                        // Black background with white circular border ring and white waveform!
                        // - Tap: Begins recording voice note / Study Talk
                        // - Long-press / Hold: Records voice note while held and sends on release
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TrueBlack)
                                .border(2.dp, PureWhite, CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            onHoldStartRecording()
                                        },
                                        onLongPress = {
                                            onHoldStartRecording()
                                        },
                                        onPress = {
                                            val released = tryAwaitRelease()
                                            if (isRecordingAudio) {
                                                if (released) {
                                                    onHoldFinishRecording()
                                                } else {
                                                    onHoldCancelRecording()
                                                }
                                            }
                                        }
                                    )
                                }
                                .testTag("chat_voice_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveformIcon(
                                color = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4 vertical audio wave bars matching the voice button: ||||
 */
@Composable
fun WaveformIcon(
    color: Color = PureWhite,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barW = 2.4.dp.toPx()
        val spacing = (w - (barW * 4)) / 3

        val barHeights = listOf(h * 0.45f, h * 0.95f, h * 0.65f, h * 0.40f)

        barHeights.forEachIndexed { i, barH ->
            val x = i * (barW + spacing) + barW / 2
            val top = (h - barH) / 2
            val bottom = top + barH
            drawLine(
                color = color,
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = barW,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Rhythmic vertical audio waveform matching the reference image:
 * Vertical bars of varying staggered heights in PureWhite with rounded caps.
 */
@Composable
fun VoiceNoteWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = PureWhite
) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )

    // Baseline heights modeled precisely from the user's reference image:
    // Pattern: short, tall, short, medium, tall, short, medium, tall, short, medium, short, tall, medium, tall, short, short
    val baselineFractions = remember {
        listOf(0.35f, 0.85f, 0.40f, 0.65f, 0.80f, 0.45f, 0.75f, 0.85f, 0.40f, 0.70f, 0.50f, 0.80f, 0.60f, 0.95f, 0.45f, 0.40f)
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = 2.4.dp.toPx()
        val count = baselineFractions.size
        val availableSpacing = (totalWidth - (barWidth * count)) / (count - 1).coerceAtLeast(1)

        for (i in 0 until count) {
            val baseH = baselineFractions[i]
            val effectiveH = if (isPlaying) {
                val wave = kotlin.math.sin(phase + i * 0.6f).toFloat()
                (baseH * (0.65f + 0.35f * wave)).coerceIn(0.25f, 1.0f)
            } else {
                baseH
            }

            val actualBarHeight = totalHeight * effectiveH
            val x = i * (barWidth + availableSpacing) + barWidth / 2f
            val top = (totalHeight - actualBarHeight) / 2f
            val bottom = top + actualBarHeight

            drawLine(
                color = barColor,
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Stadium pill Voice Note capsule matching user's reference image:
 * - Black background
 * - Crisp white border
 * - Circular white ring button on the left with play / pause icon
 * - Center vertical audio waveform bars in pure white
 * - Right duration timestamp in pure white (e.g. "0:05")
 */
@Composable
fun VoiceNoteCapsule(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    durationText: String = "0:05",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = TrueBlack,
        border = BorderStroke(1.5.dp, PureWhite),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left circular button with white border ring (matching the ring in the reference image)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(2.dp, PureWhite, CircleShape)
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop Voice Note" else "Play Voice Note",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center vertical waveform bars in PureWhite
            VoiceNoteWaveform(
                isPlaying = isPlaying,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp),
                barColor = PureWhite
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Right duration timestamp in PureWhite
            Text(
                text = durationText,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Individual Chat Message Row:
 * - User: Right-aligned bubble, OR Stadium Voice Note Capsule if message was recorded
 * - Assistant: Left-aligned pure text with formatted markdown, formulas, diagrams, and action icons!
 */
@Composable
fun ChatGptMessageItem(
    message: ChatMessage,
    isSpeakingThis: Boolean,
    isPlayingVoiceNote: Boolean = false,
    onTogglePlayVoiceNote: () -> Unit = {},
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit,
    onLike: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (message.sender == MessageSender.USER) {
        // User Message (Right Aligned Bubble / Voice Note Capsule)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (message.isVoiceNote) {
                // Stadium capsule matching user reference image:
                // Black background, white border, left circular ring with play/stop,
                // center white waveform bars, right duration like "0:05"
                Column(horizontalAlignment = Alignment.End) {
                    VoiceNoteCapsule(
                        isPlaying = isPlayingVoiceNote,
                        onTogglePlay = onTogglePlayVoiceNote,
                        durationText = formatVoiceDuration(message.voiceDurationSeconds),
                        modifier = Modifier.widthIn(min = 230.dp, max = 295.dp)
                    )
                    if (message.text.isNotBlank() && message.text != "Voice note explanation request") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = TextMutedGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.text,
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ChatGptUserBubble,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        if (message.attachedImage != null) {
                            Image(
                                bitmap = message.attachedImage.asImageBitmap(),
                                contentDescription = "Attached homework",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(bottom = 6.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = message.text,
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    } else {
        // Assistant Message (Left Aligned, Transparent Background directly on black)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // Rich Formatted Message Content: Headings, Bullet Lists, Mathematical Equations, and Code Blocks
            AssistantRichMessageContent(
                text = message.text
            )

            // Action Icons Row matching Screenshot 3:
            // [📋 Copy] [👍 Like] [👎 Dislike] [🔊 Volume / Speak] [🔗 Share] [⋮ More]
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Copy icon
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy explanation",
                    tint = ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Study Buddy Explanation", message.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                )

                // 2. Thumbs Up
                Icon(
                    imageVector = if (message.isLiked == true) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = if (message.isLiked == true) PureWhite else ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onLike(true) }
                )

                // 3. Thumbs Down
                Icon(
                    imageVector = if (message.isLiked == false) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = if (message.isLiked == false) PureWhite else ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onLike(false) }
                )

                // 4. Speaker / Read Aloud icon
                Icon(
                    imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeakingThis) "Stop audio" else "Read aloud",
                    tint = if (isSpeakingThis) PureWhite else ChatGptIconGray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            if (isSpeakingThis) onStopSpeak() else onSpeak()
                        }
                )

                // 5. Share icon
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Explanation"))
                        }
                )

                // 6. Overflow More Options icon
                var showMsgMenu by remember { mutableStateOf(false) }
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = ChatGptIconGray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showMsgMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMsgMenu,
                        onDismissRequest = { showMsgMenu = false },
                        modifier = Modifier.background(ChatGptDrawerBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Full Text", color = PureWhite) },
                            onClick = {
                                showMsgMenu = false
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Study Buddy", message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Read Aloud", color = PureWhite) },
                            onClick = {
                                showMsgMenu = false
                                onSpeak()
                            }
                        )
                    }
                }
            }
        }
    }
}

fun cleanMathSymbols(str: String): String {
    return str
        .replace("\\times", "×")
        .replace("\\cdot", "·")
        .replace("\\div", "÷")
        .replace("\\pm", "±")
        .replace("\\approx", "≈")
        .replace("\\neq", "≠")
        .replace("\\le", "≤")
        .replace("\\ge", "≥")
        .replace("\\sqrt", "√")
        .replace("\\to", "→")
        .replace("\\rightarrow", "→")
        .replace("\\text", "")
        .replace("\\mathrm", "")
        .replace("\\mathbf", "")
        .replace("{", "")
        .replace("}", "")
        .replace("\\ ", " ")
        .replace("\\", "")
        .trim()
}

data class MathFraction(
    val prefix: String,
    val numerator: String,
    val denominator: String,
    val suffix: String
)

fun parseMathFraction(raw: String): MathFraction? {
    val fracRegex = Regex("""^(.*?)\\frac\{([^}]+)\}\{([^}]+)\}(.*)$""")
    val match = fracRegex.find(raw)
    if (match != null) {
        return MathFraction(
            prefix = match.groupValues[1].replace(Regex("""^\$\$|\$\$$"""), "").trim(),
            numerator = match.groupValues[2].trim(),
            denominator = match.groupValues[3].trim(),
            suffix = match.groupValues[4].replace(Regex("""^\$\$|\$\$$"""), "").trim()
        )
    }
    return null
}

@Composable
fun MathEquationView(
    rawEquation: String,
    modifier: Modifier = Modifier
) {
    val clean = rawEquation.trim()
        .removePrefix("$$").removeSuffix("$$")
        .removePrefix("\\[").removeSuffix("\\]")
        .trim()

    val fraction = remember(clean) { parseMathFraction(clean) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (fraction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (fraction.prefix.isNotBlank()) {
                    Text(
                        text = cleanMathSymbols(fraction.prefix) + " ",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = cleanMathSymbols(fraction.numerator),
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            color = PureWhite
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.widthIn(min = 36.dp),
                        thickness = 1.5.dp,
                        color = PureWhite
                    )
                    Text(
                        text = cleanMathSymbols(fraction.denominator),
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            color = PureWhite
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (fraction.suffix.isNotBlank()) {
                    Text(
                        text = " " + cleanMathSymbols(fraction.suffix),
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                    )
                }
            }
        } else {
            Text(
                text = cleanMathSymbols(clean),
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    color = PureWhite
                )
            )
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF14171E),
        border = BorderStroke(1.dp, Color(0xFF282C37)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E222C))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifEmpty { "code" },
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = PureWhite,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy code",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFE6EDF3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AssistantRichMessageContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val codeBlockRegex = Regex("""```([a-zA-Z]*)\n([\s\S]*?)```""")
        var lastIndex = 0
        val matches = codeBlockRegex.findAll(text).toList()

        if (matches.isEmpty()) {
            RenderNonCodeBlocks(text)
        } else {
            matches.forEach { match ->
                val preText = text.substring(lastIndex, match.range.first)
                if (preText.isNotBlank()) {
                    RenderNonCodeBlocks(preText)
                }
                val lang = match.groupValues[1]
                val code = match.groupValues[2].trimEnd()
                CodeBlockView(language = lang, code = code)
                lastIndex = match.range.last + 1
            }
            if (lastIndex < text.length) {
                val postText = text.substring(lastIndex)
                if (postText.isNotBlank()) {
                    RenderNonCodeBlocks(postText)
                }
            }
        }
    }
}

@Composable
private fun RenderNonCodeBlocks(rawText: String) {
    val lines = rawText.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.isEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            i++
            continue
        }

        // Check for display equation $$...$$ or \frac{...}{...}
        if (line.startsWith("$$") || line.contains("\\frac{")) {
            MathEquationView(rawEquation = line)
            i++
            continue
        }

        // Bullet point: • or -
        if (line.startsWith("•") || line.startsWith("- ") || line.startsWith("* ")) {
            val content = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Text(
                    text = "• ",
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildFormattedText(content),
                    color = PureWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
            i++
            continue
        }

        // Headings, numbered list items, answers, regular paragraphs
        val isHeading = line.startsWith("**") && line.endsWith("**") && line.length > 4 && !line.contains("→")
        val isNumberedList = Regex("""^\d+\.\s+""").containsMatchIn(line)
        val isAnswer = line.startsWith("Answer:", ignoreCase = true) || line.startsWith("**Answer:", ignoreCase = true)

        if (isHeading) {
            Text(
                text = buildFormattedText(line),
                color = PureWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        } else if (isAnswer) {
            Text(
                text = buildFormattedText(line),
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else if (isNumberedList) {
            Text(
                text = buildFormattedText(line),
                color = PureWhite,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        } else {
            Text(
                text = buildFormattedText(line),
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 23.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        i++
    }
}

fun buildFormattedText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PureWhite)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

/**
 * Immersive ChatGPT Voice Mode Dialog when tapping the blue waveform button
 */
@Composable
fun VoiceModeDialog(
    isSpeaking: Boolean,
    isListening: Boolean,
    voiceGender: com.example.util.VoiceGender = com.example.util.VoiceGender.MALE,
    onVoiceGenderChange: (com.example.util.VoiceGender) -> Unit = {},
    onClose: () -> Unit,
    onToggleMic: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = TrueBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close X
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Study Talk • Audio Q&A",
                            color = PureWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verbal AI questions with audio explanations",
                            color = ChatGptBlueWave,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ChatGptPillDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Persona Switcher (Nigerian Male / Nigerian Female)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ChatGptPillDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isMale = voiceGender == com.example.util.VoiceGender.MALE
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMale) PureWhite else Color.Transparent,
                        modifier = Modifier.clickable { onVoiceGenderChange(com.example.util.VoiceGender.MALE) }
                    ) {
                        Text(
                            text = "Nigerian Male (Clear)",
                            color = if (isMale) TrueBlack else TextMutedGray,
                            fontSize = 12.sp,
                            fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isMale) PureWhite else Color.Transparent,
                        modifier = Modifier.clickable { onVoiceGenderChange(com.example.util.VoiceGender.FEMALE) }
                    ) {
                        Text(
                            text = "Nigerian Female",
                            color = if (!isMale) TrueBlack else TextMutedGray,
                            fontSize = 12.sp,
                            fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Pulsing Audio Orb (Signature ChatGPT Voice Visualizer)
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Glow Rings
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        drawCircle(
                            color = ChatGptBlueWave.copy(alpha = ringAlpha * 0.4f),
                            radius = (size.width / 2) * pulseScale
                        )
                        drawCircle(
                            color = ChatGptBlueWave.copy(alpha = ringAlpha),
                            radius = (size.width / 2) * 0.8f
                        )
                    }

                    // Inner Fluid Orb
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(PureWhite, ChatGptBlueWave, Color(0xFF0F2B70))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        WaveformIcon(color = PureWhite, modifier = Modifier.size(46.dp))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = when {
                        isSpeaking -> "Study Buddy is responding with audio..."
                        isListening -> "Listening to your question..."
                        else -> "Tap the mic and speak your question"
                    },
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Powered by Gemini with real-time Nigerian voice audio feedback",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Mic Button
                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ChatGptBlueWave else ChatGptPillDark)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Toggle Mic",
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // End Call (X) Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ChatGptPillDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "End Voice Mode",
                            tint = PureWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sleek "Get Plus" Modal matching the ChatGPT subscription design
 */
@Composable
fun GetPlusDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ChatGptPillDark, RoundedCornerShape(20.dp)),
            color = DarkSurface
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ChatGptPlusStarBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Study Buddy Plus",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Supercharge your school studies with our most capable AI tutor model:",
                    color = TextMutedGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val features = listOf(
                    "Unlimited homework camera scans & OCR solving",
                    "Advanced STEM step-by-step reasoning with interactive diagrams",
                    "Real-time ultra-low latency voice mode with neural voices",
                    "Offline high school & AP quiz generator and study notes",
                    "Zero rate limits & fastest response times"
                )

                features.forEach { feat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ChatGptPlusStarBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feat,
                            color = PureWhite,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upgrade Button
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(26.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Upgrade to Study Buddy Plus",
                            color = TrueBlack,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
