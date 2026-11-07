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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.WebSearchResultImage
import com.example.util.FileCategory
import com.example.util.GeneratedFileInfo
import com.example.util.UniversalFileGenerator
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
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
    onQuickReply: ((String) -> Unit)? = null,
    onGenerateFile: ((String) -> Unit)? = null,
    onGenerateImage: (() -> Unit)? = null,
    onDownloadWebImage: ((WebSearchResultImage) -> Unit)? = null,
    onCopyImageLink: ((String) -> Unit)? = null,
    onRetryImageSearch: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFullscreenImage by remember { mutableStateOf<WebSearchResultImage?>(null) }

    if (selectedFullscreenImage != null) {
        FullscreenImageViewerDialog(
            image = selectedFullscreenImage!!,
            onDismiss = { selectedFullscreenImage = null },
            onDownload = {
                onDownloadWebImage?.invoke(selectedFullscreenImage!!)
            },
            onCopyLink = {
                onCopyImageLink?.invoke(selectedFullscreenImage!!.originalUrl)
            }
        )
    }

    if (message.sender == MessageSender.USER) {
        // User Message (Right Aligned Bubble / Voice Note Capsule)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (message.isVoiceNote) {
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

            // Real-Time Web Image Search Gallery (diagrams, photos, illustrations)
            if (message.searchImages.isNotEmpty() || message.isSearchingImages) {
                Spacer(modifier = Modifier.height(12.dp))
                WebImageGalleryView(
                    images = message.searchImages,
                    isLoading = message.isSearchingImages,
                    onImageClick = { selectedFullscreenImage = it },
                    onDownload = { img -> onDownloadWebImage?.invoke(img) },
                    onCopyLink = { url -> onCopyImageLink?.invoke(url) },
                    onRetry = {
                        val queryFallback = message.text.lines().firstOrNull()?.take(40) ?: "diagram"
                        onRetryImageSearch?.invoke(queryFallback)
                    }
                )
            }

            // AI Generated Educational Concept Diagram (if requested)
            if (message.generatedAiImageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                EducationalDiagramAttachment(
                    imageUrl = message.generatedAiImageUrl
                )
            }

            // Universal Downloadable File Attachment Card (if requested)
            if (message.generatedFile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                UniversalFileAttachmentCard(
                    fileInfo = message.generatedFile,
                    onDownload = { UniversalFileGenerator.saveToDownloads(context, message.generatedFile) },
                    onOpen = { UniversalFileGenerator.openFile(context, message.generatedFile) },
                    onShare = { UniversalFileGenerator.shareFile(context, message.generatedFile) }
                )
            }

            // Quick Study Track Selector if tutor asks at the end: WAEC, JAMB, or just learning
            if (message.text.contains("Are you studying for WAEC, JAMB, or just learning", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TrueBlack,
                        border = BorderStroke(1.dp, PureWhite),
                        modifier = Modifier
                            .clickable { onQuickReply?.invoke("I'm studying for WAEC!") }
                            .testTag("chat_track_waec_chip")
                    ) {
                        Text(
                            text = "🎓 WAEC",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TrueBlack,
                        border = BorderStroke(1.dp, PureWhite),
                        modifier = Modifier
                            .clickable { onQuickReply?.invoke("I'm studying for JAMB!") }
                            .testTag("chat_track_jamb_chip")
                    ) {
                        Text(
                            text = "⚡ JAMB",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TrueBlack,
                        border = BorderStroke(1.dp, Color(0xFF666666)),
                        modifier = Modifier
                            .clickable { onQuickReply?.invoke("Just learning for now!") }
                            .testTag("chat_track_learning_chip")
                    ) {
                        Text(
                            text = "💡 Just learning",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Action Icons Row:
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
                            text = { Text("Export as PDF (.pdf)", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMsgMenu = false
                                onGenerateFile?.invoke("pdf")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as Word (.docx)", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMsgMenu = false
                                onGenerateFile?.invoke("docx")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as Excel (.xlsx)", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMsgMenu = false
                                onGenerateFile?.invoke("xlsx")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as HTML (.html)", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMsgMenu = false
                                onGenerateFile?.invoke("html")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Generate AI Diagram", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ChatGptPlusStarBlue, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMsgMenu = false
                                onGenerateImage?.invoke()
                            }
                        )
                    }
                }
            }

            // EXACTLY THREE SMART FOLLOW-UP SUGGESTIONS (Clean, minimal, text-based)
            if (message.followUpSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    message.followUpSuggestions.take(3).forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickReply?.invoke(suggestion) }
                                .padding(vertical = 5.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = suggestion,
                                color = Color(0xFFD4D4D4),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Universal Downloadable File Attachment Card inside Chat
 */
@Composable
fun UniversalFileAttachmentCard(
    fileInfo: GeneratedFileInfo,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconBgColor, fileIcon) = when (fileInfo.extension.lowercase(Locale.US)) {
        "pdf" -> Color(0xFFEF4444) to Icons.Default.PictureAsPdf
        "docx", "doc" -> Color(0xFF2563EB) to Icons.Outlined.Description
        "xlsx", "xls", "csv" -> Color(0xFF10B981) to Icons.Default.TableChart
        "pptx", "ppt" -> Color(0xFFF97316) to Icons.Default.Slideshow
        "html", "py", "java", "js", "ts", "json", "sql" -> Color(0xFF8B5CF6) to Icons.Default.Code
        "zip" -> Color(0xFFF59E0B) to Icons.Default.FolderZip
        else -> Color(0xFF6B7280) to Icons.Outlined.Description
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Type Icon Box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor.copy(alpha = 0.2f))
                        .border(1.dp, iconBgColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = iconBgColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // File Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileInfo.fileName,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${fileInfo.extension.uppercase()} ${fileInfo.category.label} • ${fileInfo.formattedSize}",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: [Download] [Open] [Share]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download Button (Primary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDownload() }
                        .testTag("download_file_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = TrueBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download",
                            color = TrueBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Open with External App Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TrueBlack,
                    border = BorderStroke(1.dp, Color(0xFF444444)),
                    modifier = Modifier
                        .clickable { onOpen() }
                        .testTag("open_file_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = PureWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Open",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Share Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TrueBlack,
                    border = BorderStroke(1.dp, Color(0xFF444444)),
                    modifier = Modifier
                        .clickable { onShare() }
                        .testTag("share_file_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = PureWhite,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI Concept Diagram Attachment Card inside Chat
 */
@Composable
fun EducationalDiagramAttachment(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ChatGptPlusStarBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Visual Concept Diagram",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val diagContext = androidx.compose.ui.platform.LocalContext.current
            val diagModel = remember(imageUrl) {
                ImageRequest.Builder(diagContext)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = diagModel,
                contentDescription = "Generated educational diagram",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 260.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TrueBlack),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Processes inline LaTeX expressions ($...$ and \(...\)) into mathematically clean Unicode typography.
 */
fun formatInlineMath(text: String): String {
    // 1. Matches \( ... \) LaTeX inline math
    var result = Regex("""\\\(([\s\S]*?)\\\)""").replace(text) { match ->
        MathRenderer.formatLatexToReadableMath(match.groupValues[1])
    }
    // 2. Matches $...$ inline math (ignoring solitary currency expressions)
    result = Regex("""(?<!\\)\$([^\$\n]+?)\$(?!\d)""").replace(result) { match ->
        val inner = match.groupValues[1].trim()
        MathRenderer.formatLatexToReadableMath(inner)
    }
    // 3. Fallback: transform unbracketed LaTeX scientific symbols into clean Unicode
    val standaloneLatexSyms = listOf(
        "\\times" to "×",
        "\\cdot" to "·",
        "\\div" to "÷",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\approx" to "≈",
        "\\neq" to "≠",
        "\\ne" to "≠",
        "\\le" to "≤",
        "\\leq" to "≤",
        "\\ge" to "≥",
        "\\geq" to "≥",
        "\\degree" to "°",
        "\\to" to "→",
        "\\rightarrow" to "→",
        "\\leftarrow" to "←",
        "\\pi" to "π",
        "\\theta" to "θ",
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\lambda" to "λ",
        "\\mu" to "μ",
        "\\Delta" to "Δ",
        "\\Omega" to "Ω",
        "\\infty" to "∞"
    )
    for ((tex, sym) in standaloneLatexSyms) {
        result = result.replace(tex, sym)
    }
    if (result.contains("\\frac{") || result.contains("\\sqrt{") || result.contains("^{") || result.contains("_{")) {
        result = MathRenderer.formatLatexToReadableMath(result)
    }
    return result
}

data class ParsedMarkdownTable(
    val headers: List<String>,
    val rows: List<List<String>>
)

fun parseMarkdownTable(lines: List<String>): ParsedMarkdownTable? {
    if (lines.size < 2) return null
    val splitCells = { rawLine: String ->
        rawLine.trim()
            .removePrefix("|").removeSuffix("|")
            .split("|")
            .map { it.trim() }
    }
    val rawHeader = splitCells(lines[0])
    if (rawHeader.isEmpty()) return null

    val dataRows = mutableListOf<List<String>>()
    for (idx in 1 until lines.size) {
        val line = lines[idx].trim()
        // Skip separator rows like |---|:---:|---|
        if (line.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()) {
            continue
        }
        val cells = splitCells(line)
        if (cells.isNotEmpty()) {
            dataRows.add(cells)
        }
    }
    if (dataRows.isEmpty()) return null
    return ParsedMarkdownTable(headers = rawHeader, rows = dataRows)
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

        // 1. Markdown Table Detection (e.g. for number-base conversions, comparisons, truth tables)
        if (line.contains("|") && (line.startsWith("|") || line.endsWith("|") || (i + 1 < lines.size && lines[i + 1].contains("---")))) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().contains("|")) {
                tableLines.add(lines[i].trim())
                i++
            }
            val parsed = parseMarkdownTable(tableLines)
            if (parsed != null && parsed.headers.isNotEmpty() && parsed.rows.isNotEmpty()) {
                MathTableView(headers = parsed.headers, rows = parsed.rows)
            } else {
                tableLines.forEach { tLine ->
                    Text(text = buildFormattedText(tLine), color = PureWhite, fontSize = 14.sp)
                }
            }
            continue
        }

        // 2. Block Mathematical Expressions: $$, \[, \begin{equation}, \begin{align}
        if (line.startsWith("$$") || line.startsWith("\\[") || line.startsWith("\\begin{equation}") || line.startsWith("\\begin{align}")) {
            val isSingleLine = (line.startsWith("$$") && line.endsWith("$$") && line.length > 2) ||
                               (line.startsWith("\\[") && line.endsWith("\\]") && line.length > 2)
            if (isSingleLine) {
                MathEquationView(rawEquation = line)
                i++
                continue
            } else {
                val blockLines = mutableListOf<String>()
                val closingToken = when {
                    line.startsWith("$$") -> "$$"
                    line.startsWith("\\[") -> "\\]"
                    line.startsWith("\\begin{equation}") -> "\\end{equation}"
                    else -> "\\end{align}"
                }
                blockLines.add(line)
                i++
                while (i < lines.size) {
                    val curr = lines[i].trim()
                    blockLines.add(curr)
                    i++
                    if (curr.endsWith(closingToken) || curr == closingToken) {
                        break
                    }
                }
                MathEquationView(rawEquation = blockLines.joinToString("\n"))
                continue
            }
        }

        // 3. Standalone mathematical formula lines (e.g. Speed = \frac{Distance}{Time} or \frac{100}{20} = 5)
        if (line.contains("\\frac{") || (line.contains("\\sqrt{") && line.contains("=")) || line.contains("\\int_{")) {
            MathEquationView(rawEquation = line)
            i++
            continue
        }

        // 4. Bullet points: • or - or *
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

        // 5. Headings: #, ##, ###, ####, #####, ###### (Never display raw # signs!)
        val headingMatch = Regex("""^(#{1,6})\s*(.*)$""").find(line)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val headingText = headingMatch.groupValues[2].trim()
            val (fontSize, topPad) = when (level) {
                1 -> 20.sp to 14.dp
                2 -> 18.sp to 10.dp
                3 -> 16.sp to 8.dp
                else -> 15.sp to 6.dp
            }
            Text(
                text = buildFormattedText(headingText),
                color = PureWhite,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = topPad, bottom = 4.dp)
            )
            i++
            continue
        }

        // 6. Numbered list items, bold titles, answers, regular paragraphs
        val isHeading = line.startsWith("**") && line.endsWith("**") && line.length > 4 && !line.contains("→")
        val isNumberedList = Regex("""^\d+\.\s+""").containsMatchIn(line)
        val isAnswer = line.startsWith("Answer:", ignoreCase = true) || line.startsWith("**Answer:", ignoreCase = true) || line.startsWith("Final Answer:", ignoreCase = true)

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

fun cleanMarkdownArtifacts(raw: String): String {
    var s = raw
    // 1. Strip raw HTML tags
    s = s.replace(Regex("""<(details|summary|b|i|u|br|p|span|div)[^>]*>|</(details|summary|b|i|u|br|p|span|div)>""", RegexOption.IGNORE_CASE), " ")
    // 2. Strip leading heading markers like ### or ##
    s = s.replace(Regex("""^#{1,6}\s*"""), "")
    // 3. Remove stray triple and double hashes inside text
    s = s.replace("###", "")
    s = s.replace("##", "")
    // 4. Strip template syntax
    s = s.replace(Regex("""\$\{.*?\}|\{\{.*?\}\}"""), "")
    return s.trim()
}

fun buildFormattedText(text: String): AnnotatedString {
    val cleaned = cleanMarkdownArtifacts(text)
    val mathProcessed = formatInlineMath(cleaned)
    return buildAnnotatedString {
        // Handle inline monospace code `code`
        val codeParts = mathProcessed.split("`")
        codeParts.forEachIndexed { cIdx, codePart ->
            if (cIdx % 2 == 1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF67E8F9),
                        background = Color(0xFF1E222C)
                    )
                ) {
                    append(" $codePart ")
                }
            } else {
                // Handle bold **bold**
                val boldParts = codePart.split("**")
                boldParts.forEachIndexed { bIdx, boldPart ->
                    if (bIdx % 2 == 1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PureWhite)) {
                            append(boldPart)
                        }
                    } else {
                        // Handle italic *italic*
                        val italicParts = boldPart.split(Regex("(?<=\\s|^)\\*|\\*(?=\\s|$)"))
                        if (italicParts.size > 1) {
                            italicParts.forEachIndexed { iIdx, itPart ->
                                if (iIdx % 2 == 1) {
                                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = PureWhite)) {
                                        append(itPart)
                                    }
                                } else {
                                    append(itPart)
                                }
                            }
                        } else {
                            append(boldPart)
                        }
                    }
                }
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
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
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ChatGptPillDark, RoundedCornerShape(20.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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

/**
 * Responsive Web Image Gallery displayed directly within StudyBuddy chat.
 * Shows retrieved educational diagrams and real web images with attribution,
 * source website, download, copy link, and fullscreen viewing.
 */
@Composable
fun WebImageGalleryView(
    images: List<WebSearchResultImage>,
    isLoading: Boolean,
    onImageClick: (WebSearchResultImage) -> Unit,
    onDownload: (WebSearchResultImage) -> Unit,
    onCopyLink: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: Category label + verified source
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = ChatGptPlusStarBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Web Images & Scientific Diagrams",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (images.isNotEmpty()) {
                    Text(
                        text = "${images.size} results",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading && images.isEmpty()) {
                // Loading indicator while searching web
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Searching verified educational sources...",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                }
            } else if (images.isEmpty()) {
                // No images found or error state with retry
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "No web diagrams found for this concept.",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry image search",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // Horizontal scrolling gallery of cards
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(images, key = { it.originalUrl + it.title }) { image ->
                        WebImageCard(
                            image = image,
                            onClick = { onImageClick(image) },
                            onDownload = { onDownload(image) },
                            onCopyLink = { onCopyLink(image.originalUrl) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Web Image Card inside the gallery:
 * - High-resolution loaded thumbnail
 * - Title and attribution
 * - Source badge (e.g. Wikimedia Commons)
 * - Tap to fullscreen
 * - Download and copy link actions
 */
@Composable
fun WebImageCard(
    image: WebSearchResultImage,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        modifier = modifier
            .widthIn(min = 180.dp, max = 240.dp)
            .width(210.dp)
            .clickable { onClick() }
    ) {
        Column {
            // Image with Coil AsyncImage + placeholder / zoom button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF0D0D0D))
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageModel = remember(image.thumbnailUrl, image.originalUrl) {
                    val targetUrl = image.thumbnailUrl.ifBlank { image.originalUrl }
                    ImageRequest.Builder(context)
                        .data(targetUrl)
                        .crossfade(true)
                        .setHeader("User-Agent", "StudyBuddy-EducationalApp/1.0 (Android; contact: educational-app@studybuddy.ng)")
                        .build()
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = image.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Zoom overlay button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "View full size",
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Source site badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = image.sourceWebsite,
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Info and controls
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = image.title,
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                val authorText = image.author?.takeIf { it.isNotBlank() }
                val licenseText = image.license?.takeIf { it.isNotBlank() } ?: "Open Access"
                Text(
                    text = if (authorText != null) "By $authorText" else licenseText,
                    color = TextMutedGray,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions row: Download & Copy link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download icon button (min 48dp touch target)
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download image",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy link button
                    IconButton(
                        onClick = onCopyLink,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Copy image link",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Image Viewer Dialog with zoomable display,
 * complete attribution details, direct download button, and copy link option.
 */
@Composable
fun FullscreenImageViewerDialog(
    image: WebSearchResultImage,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TrueBlack,
            border = BorderStroke(1.dp, Color(0xFF333333)),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = image.title,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-resolution image preview
                val dialogContext = androidx.compose.ui.platform.LocalContext.current
                val fullImageModel = remember(image.originalUrl, image.thumbnailUrl) {
                    val targetUrl = image.originalUrl.ifBlank { image.thumbnailUrl }
                    ImageRequest.Builder(dialogContext)
                        .data(targetUrl)
                        .crossfade(true)
                        .setHeader("User-Agent", "StudyBuddy-EducationalApp/1.0 (Android; contact: educational-app@studybuddy.ng)")
                        .build()
                }
                AsyncImage(
                    model = fullImageModel,
                    contentDescription = image.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 340.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0D0D0D)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Attribution & License info
                val authorStr = image.author?.takeIf { it.isNotBlank() }
                if (authorStr != null) {
                    Text(
                        text = "Author: $authorStr",
                        color = PureWhite,
                        fontSize = 11.sp
                    )
                }
                val licenseStr = image.license?.takeIf { it.isNotBlank() } ?: "Educational / Creative Commons"
                Text(
                    text = "License: $licenseStr • Source: ${image.sourceWebsite}",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Download & Copy link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureWhite,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onDownload() }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = TrueBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Image",
                                color = TrueBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TrueBlack,
                        border = BorderStroke(1.dp, Color(0xFF444444)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onCopyLink() }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy Link",
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

