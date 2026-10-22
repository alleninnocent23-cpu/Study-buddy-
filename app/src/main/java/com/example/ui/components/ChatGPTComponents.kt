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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

/**
 * Bottom Input Pill Capsule matching both screenshots:
 * - Left: + button
 * - Center: TextField with "Ask ChatGPT" or "Reply to ChatGPT"
 * - Right: Mic icon + Blue circular voice waveform button (or Send arrow if text typed)
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

        // Main Dark Capsule
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
                // Study Buddy Bot Image at the left side of the type bar (transparent background, no text overlap)
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

                // Middle Text Field with flexible weight so it never overlaps or gets covered
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

                // Mic Icon Button
                IconButton(
                    onClick = onVoiceDictation,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) ChatGptBlueWave else PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Voice Waveform / Send Button
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
                    // Exact Blue Waveform Voice Mode Button from screenshots
                    IconButton(
                        onClick = onOpenVoiceMode,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ChatGptBlueWave)
                            .testTag("chat_voice_mode_button")
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

/**
 * 4 vertical audio wave bars matching the voice button in screenshots: ||||
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
 * Individual Chat Message Row:
 * - User: Right-aligned blue bubble (#132B6E)
 * - Assistant: Left-aligned pure text with formatted markdown, formulas, diagrams, and action icons!
 */
@Composable
fun ChatGptMessageItem(
    message: ChatMessage,
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit,
    onLike: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (message.sender == MessageSender.USER) {
        // User Message (Right Aligned Bubble)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
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
    } else {
        // Assistant Message (Left Aligned, Transparent Background directly on black)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // Main Text (Pure clean text matching Screenshot 2)
            Text(
                text = message.text,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 23.sp
            )

            // Educational Diagram if relevant
            if (message.diagramType != "none" && message.diagramType.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                EducationalDiagramView(
                    diagramType = message.diagramType,
                    title = message.diagramTitle
                )
            }

            // Action Icons Row:
            // [▶ Play / ⏹ Stop Voice] [📋 Copy] [👍 Like] [👎 Dislike] [🔗 Share] [⋮ More]
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Play Voice Button (placed right by Copy button)
                Surface(
                    onClick = {
                        if (isSpeakingThis) onStopSpeak() else onSpeak()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSpeakingThis) ChatGptBlueWave.copy(alpha = 0.25f) else ChatGptPillDark,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isSpeakingThis) "Stop Voice" else "Play Voice",
                            tint = if (isSpeakingThis) ChatGptBlueWave else PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpeakingThis) "Stop" else "Play",
                            color = if (isSpeakingThis) ChatGptBlueWave else PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 2. Copy Button (placed right next to Play button)
                Surface(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Study Buddy Explanation", message.text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = ChatGptPillDark,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Thumbs Up
                Icon(
                    imageVector = if (message.isLiked == true) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = if (message.isLiked == true) PureWhite else ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onLike(true) }
                )

                // 4. Thumbs Down
                Icon(
                    imageVector = if (message.isLiked == false) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = if (message.isLiked == false) PureWhite else ChatGptIconGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onLike(false) }
                )

                // 5. Share
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

                // 6. Overflow dots
                var showMsgMenu by remember { mutableStateOf(false) }
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = ChatGptIconGray,
                        modifier = Modifier
                            .size(17.dp)
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
                    Text(
                        text = "Study Buddy Voice Mode",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        isSpeaking -> "Study Buddy is speaking..."
                        isListening -> "Listening to you..."
                        else -> "Tap the mic and ask anything"
                    },
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "High-fidelity school explanations & problem solving",
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
