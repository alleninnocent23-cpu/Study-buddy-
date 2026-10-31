package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.ChatGptEmptyPrompts
import com.example.ui.components.ChatGptInputBar
import com.example.ui.components.ChatGptMessageItem
import com.example.ui.components.ChatGptTopBar
import com.example.ui.components.GetPlusDialog
import com.example.ui.components.SubjectPersonalizationBadge
import com.example.ui.components.SubjectSelectionDialog
import com.example.ui.components.SubjectSelectionStrip
import com.example.R
import com.example.ui.components.LoopingVideoPlayer
import com.example.ui.components.VoiceModeDialog
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.TutorState

@Composable
fun AssignmentHelperScreen(
    state: TutorState,
    isSpeaking: Boolean,
    isListening: Boolean,
    attachedImage: Bitmap? = null,
    attachedFileName: String? = null,
    onSelectSubject: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onSendChatMessage: (String?) -> Unit,
    onAttachImage: (Bitmap) -> Unit = {},
    onAttachFile: (String) -> Unit = {},
    onClearAttachment: () -> Unit = {},
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onPlayAnswer: (String) -> Unit,
    onStopAnswerVoice: () -> Unit,
    onClearChat: () -> Unit,
    onOpenDrawer: () -> Unit,
    onScanHomeworkShortcut: () -> Unit,
    onQuizShortcut: () -> Unit,
    onPomodoroShortcut: () -> Unit = {},
    onLikeMessage: (String, Boolean) -> Unit,
    voiceGender: com.example.util.VoiceGender = com.example.util.VoiceGender.MALE,
    onVoiceGenderChange: (com.example.util.VoiceGender) -> Unit = {},
    isRecordingAudio: Boolean = false,
    recordingDurationSeconds: Int = 0,
    currentlyPlayingVoiceNoteId: String? = null,
    onTogglePlayVoiceNote: (String, String) -> Unit = { _, _ -> },
    onHoldStartRecording: () -> Unit = {},
    onHoldFinishRecording: () -> Unit = {},
    onHoldCancelRecording: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showAttachmentPicker by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }
    var showGetPlusDialog by remember { mutableStateOf(false) }
    var showVoiceModeDialog by remember { mutableStateOf(false) }
    var showSubjectFilterDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // Auto-scroll when keyboard opens (matching video behavior)
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    LaunchedEffect(imeInsets.getBottom(density)) {
        if (state.messages.isNotEmpty() && imeInsets.getBottom(density) > 0) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // Activity launchers for photo & file selection
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            onAttachImage(bitmap)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { onAttachImage(it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "Document"
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            onAttachFile(fileName)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoice()
        }
    }

    // Modal dialogs
    if (showGetPlusDialog) {
        GetPlusDialog(onDismiss = { showGetPlusDialog = false })
    }

    if (showVoiceModeDialog) {
        VoiceModeDialog(
            isSpeaking = isSpeaking,
            isListening = isListening,
            voiceGender = voiceGender,
            onVoiceGenderChange = onVoiceGenderChange,
            onClose = {
                showVoiceModeDialog = false
                onStopVoice()
                onStopAnswerVoice()
            },
            onToggleMic = {
                if (isListening) onStopVoice() else onStartVoice()
            }
        )
    }

    if (showSubjectFilterDialog) {
        SubjectSelectionDialog(
            currentSubject = state.selectedSubject,
            onSelectSubject = {
                onSelectSubject(it)
                showSubjectFilterDialog = false
            },
            onDismiss = { showSubjectFilterDialog = false }
        )
    }

    // Attachment Picker Modal
    if (showAttachmentPicker) {
        Dialog(onDismissRequest = { showAttachmentPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add to Assignment",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAttachmentPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        color = TrueBlack
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Add Photo / Screenshot", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Select math problems, worksheets, diagrams", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Option 2: Camera
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                showCameraCapture = true
                            },
                        color = TrueBlack
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Take Camera Photo", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Snap textbook or homework page", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Option 3: Document
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                filePickerLauncher.launch("*/*")
                            },
                        color = TrueBlack
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Add Document / File", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Attach PDF or study notes", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ChatGPT Top Bar (matching Screenshot 1 when empty, Screenshot 2 when chatting)
            ChatGptTopBar(
                hasMessages = state.messages.isNotEmpty(),
                onMenuClick = onOpenDrawer,
                onGetPlusClick = { showGetPlusDialog = true },
                onNewChatClick = onClearChat,
                onClearChat = onClearChat,
                onSelectSubject = { showSubjectFilterDialog = true }
            )

            // Subject / Field of Study Selection Strip
            SubjectSelectionStrip(
                selectedSubject = state.selectedSubject,
                onSelectSubject = { onSelectSubject(it) },
                onOpenFullSelector = { showSubjectFilterDialog = true }
            )

            // Middle Content Area
            if (state.messages.isEmpty()) {
                // Screenshot 1: Blank OLED Canvas with suggestion rows at the bottom
                Spacer(modifier = Modifier.height(8.dp))

                SubjectPersonalizationBadge(
                    selectedSubject = state.selectedSubject,
                    onOpenSelector = { showSubjectFilterDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                ChatGptEmptyPrompts(
                    onCreateImage = {
                        onSendChatMessage("Generate an educational concept diagram for my STEM notes")
                    },
                    onWriteEdit = {
                        onSendChatMessage("Help me write and edit a strong 5-paragraph essay outline")
                    },
                    onScanHomework = onScanHomeworkShortcut,
                    onQuizMe = onQuizShortcut,
                    onPomodoroFocus = onPomodoroShortcut
                )
            } else {
                // Screenshot 2: Message stream with user bubble and assistant answer with action icons
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            ChatGptMessageItem(
                                message = msg,
                                isSpeakingThis = isSpeaking,
                                isPlayingVoiceNote = (msg.id == currentlyPlayingVoiceNoteId),
                                onTogglePlayVoiceNote = { onTogglePlayVoiceNote(msg.id, msg.text) },
                                onSpeak = { onPlayAnswer(msg.text) },
                                onStopSpeak = onStopAnswerVoice,
                                onLike = { liked -> onLikeMessage(msg.id, liked) }
                            )
                        }

                        // Loading indicator
                        if (state.isLoading) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = PureWhite,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Study Buddy is solving...",
                                            color = TextMutedGray,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Minimized cute robot video typing on laptop (stops automatically when answer arrives)
                                    Box(
                                        modifier = Modifier
                                            .size(width = 110.dp, height = 125.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TrueBlack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LoopingVideoPlayer(
                                            videoRawResId = R.raw.studybuddy_solving_bot,
                                            hasBorder = false,
                                            backgroundColor = Color.Transparent,
                                            showMuteBadge = false,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Error notice if any
                        if (state.error != null) {
                            item {
                                Text(
                                    text = state.error,
                                    color = TextMutedGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Floating Circular Down Arrow Button (matching Screenshot 2: pinned right above typing bar when scrolled up)
                    val showScrollToBottom by remember {
                        derivedStateOf {
                            listState.canScrollForward
                        }
                    }

                    if (showScrollToBottom) {
                        Surface(
                            onClick = {
                                scope.launch {
                                    if (state.messages.isNotEmpty()) {
                                        listState.animateScrollToItem(state.messages.size - 1)
                                    }
                                }
                            },
                            shape = CircleShape,
                            color = Color(0xFF242424),
                            border = BorderStroke(1.dp, Color(0xFF424242)),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .size(42.dp)
                                .testTag("scroll_to_bottom_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Scroll to bottom",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Input Bar (matching both screenshots)
            ChatGptInputBar(
                text = state.topicInput,
                onTextChange = onTopicChange,
                hasActiveMessages = state.messages.isNotEmpty(),
                attachedImage = attachedImage,
                attachedFileName = attachedFileName,
                onClearAttachment = onClearAttachment,
                onAddAttachment = { showAttachmentPicker = true },
                onVoiceDictation = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        if (isListening) onStopVoice() else onStartVoice()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onOpenVoiceMode = {
                    showVoiceModeDialog = true
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        onStartVoice()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onHoldStartRecording = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        onHoldStartRecording()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onHoldFinishRecording = onHoldFinishRecording,
                onHoldCancelRecording = onHoldCancelRecording,
                isRecordingAudio = isRecordingAudio,
                recordingDurationSeconds = recordingDurationSeconds,
                onSend = { onSendChatMessage(null) },
                isListening = isListening,
                isLoading = state.isLoading,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        }

        // Floating Scroll to Bottom Button matching Screenshot 2
        val showScrollToBottom by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex < (state.messages.size - 2).coerceAtLeast(0)
            }
        }
        AnimatedVisibility(
            visible = showScrollToBottom && state.messages.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
                .imePadding()
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(state.messages.size - 1)
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222630))
                    .border(1.dp, Color(0xFF383D4A), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Scroll to bottom",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    CameraCaptureDialog(
        isOpen = showCameraCapture,
        onDismiss = { showCameraCapture = false },
        onPhotoCaptured = { bitmap ->
            onAttachImage(bitmap)
        }
    )
}
