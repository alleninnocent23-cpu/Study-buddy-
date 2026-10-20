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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.components.AudioWaveIndicator
import com.example.ui.components.EducationalDiagramView
import com.example.ui.components.ExplanationControlsBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
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
    onExplain: () -> Unit,
    onAttachImage: (Bitmap) -> Unit = {},
    onAttachFile: (String) -> Unit = {},
    onClearAttachment: () -> Unit = {},
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onPlayAnswer: (String) -> Unit,
    onStopAnswerVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAttachmentPicker by remember { mutableStateOf(false) }

    // Light growing animation for the downward fixed input border
    val infiniteTransition = rememberInfiniteTransition(label = "light_pulse")
    val glowSpread by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_spread"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Activity launchers for Image and File picker
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
            var fileName = "Document File"
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            onAttachFile(fileName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoice()
        }
    }

    val subjects = listOf(
        Pair("Mathematics", Icons.Default.Calculate),
        Pair("Physics", Icons.Default.Science),
        Pair("Chemistry", Icons.Default.Biotech),
        Pair("Biology", Icons.Default.Psychology),
        Pair("History", Icons.Default.HistoryEdu),
        Pair("English", Icons.AutoMirrored.Filled.MenuBook),
        Pair("Computer Science", Icons.Default.Code)
    )

    // Attachment Dialog (+ Button Modal)
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

                    // Option 1: Gallery Photo
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
                            Icon(Icons.Default.Image, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Add Photo / Screenshot", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Select math problems, worksheets, diagrams", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Option 2: Camera Capture
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                cameraLauncher.launch(null)
                            },
                        color = TrueBlack
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Take Camera Photo", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Snap notebook or printed textbook page", color = TextMutedGray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Option 3: Document File
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
                            Icon(Icons.Default.Description, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Add Document / File", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Attach PDF, worksheet, notes", color = TextMutedGray, fontSize = 11.sp)
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
        // Scrollable Content Layer (Explanations, Subjects, Diagrams)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 125.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Assignment Helper",
                        color = PureWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Step-by-step school explanations, formulas & diagrams",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                }
                if (isSpeaking) {
                    AudioWaveIndicator(activeColor = PureWhite)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.forEach { (subName, icon) ->
                    val isSelected = state.selectedSubject == subName
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isSelected) PureWhite else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectSubject(subName) }
                            .testTag("subject_pill_$subName"),
                        color = if (isSelected) PureWhite else DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) TrueBlack else TextMutedGray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subName,
                                color = if (isSelected) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Quick Topic Suggestions
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = when (state.selectedSubject) {
                    "Mathematics" -> listOf("Pythagorean Theorem", "Quadratic Formula", "Derivatives of Sin(x)", "Circle Geometry")
                    "Physics" -> listOf("Newton's 2nd Law", "Ohm's Law", "Kinetic Energy", "Wave Interference")
                    "Chemistry" -> listOf("Bohr Model of Atom", "Balancing Redox Reactions", "Periodic Trends", "Acids and Bases")
                    "Biology" -> listOf("Photosynthesis Steps", "Cellular Respiration", "DNA Replication", "Mitosis vs Meiosis")
                    else -> listOf("Key Causes of WWII", "French Revolution", "Literary Devices", "Binary Search Algorithm")
                }

                suggestions.forEach { promptText ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable {
                                onTopicChange(promptText)
                                onExplain()
                            },
                        color = TrueBlack
                    ) {
                        Text(
                            text = promptText,
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
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

            // Active Attachment Preview Chip (if any image/file selected via +)
            AnimatedVisibility(visible = attachedImage != null || attachedFileName != null) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, PureWhite, RoundedCornerShape(10.dp)),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (attachedImage != null) {
                                Image(
                                    bitmap = attachedImage.asImageBitmap(),
                                    contentDescription = "Attachment Thumbnail",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = attachedFileName ?: "Attached homework media",
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Ready to analyze with question",
                                    color = TextMutedGray,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = onClearAttachment) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = PureWhite)
                            }
                        }
                    }
                }
            }

            // Sent Message Bubble (Displays immediately when user sends a question)
            if (state.sentQuestion != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 330.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, PureWhite.copy(alpha = 0.85f), RoundedCornerShape(18.dp)),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Student Question",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.sentQuestion,
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Solving / Loading Status
            if (state.isLoading) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                    color = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = PureWhite,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "StudyBuddy is breaking down the steps and formulas...",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Explanation Output Card (Reduced width size and curved border)
            AnimatedVisibility(visible = state.currentExplanation != null) {
                state.currentExplanation?.let { explanation ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Action row over the answers with + button to add images and files
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .widthIn(max = 480.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "StudyBuddy Explanation",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // + Button over answers to add images and files
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.2.dp, PureWhite, RoundedCornerShape(18.dp))
                                    .clickable { showAttachmentPicker = true }
                                    .testTag("over_answers_add_button"),
                                color = DarkSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "+ Add Image / File",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Answer Card: Reduced width and curved border (24.dp)
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .widthIn(max = 480.dp),
                            cornerRadius = 24.dp,
                            borderColor = BorderLight,
                            backgroundColor = DarkSurface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "StudyBuddy Explanation",
                                        color = PureWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, BorderLight, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = state.selectedSubject.uppercase(),
                                            color = PureWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Explanation Body
                                Text(
                                    text = explanation.explanationText,
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )

                                // Educational Diagram
                                if (explanation.diagramType != "none") {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    EducationalDiagramView(
                                        diagramType = explanation.diagramType,
                                        title = explanation.diagramTitle
                                    )
                                }

                                // MANDATORY REQUIREMENT 6:
                                // "Under every AI explanation, place three controls in this order: Play Answer, Copy Text, and Stop Voice.
                                // The Play Answer button must read the actual explanation aloud, and Stop Voice must immediately stop playback."
                                ExplanationControlsBar(
                                    explanationText = explanation.explanationText,
                                    isSpeaking = isSpeaking,
                                    onPlayAnswer = { onPlayAnswer(explanation.explanationText) },
                                    onStopVoice = onStopAnswerVoice
                                )
                            }
                        }
                    }
                }
            }
        }

        // FIXED DOWNWARD TEXT INPUT BAR WITH ANIMATION OF LIGHT GROWING AROUND IT
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, TrueBlack, TrueBlack)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Reduced width size and curved border with animated growing light
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 500.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = glowSpread.dp,
                        color = PureWhite.copy(alpha = glowAlpha),
                        shape = RoundedCornerShape(26.dp)
                    ),
                color = DarkSurface,
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + BUTTON TO ADD IMAGES AND FILES
                    IconButton(
                        onClick = { showAttachmentPicker = true },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, BorderLight, CircleShape)
                            .testTag("assignment_plus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add image or file",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.topicInput.isEmpty()) {
                            Text(
                                text = "Ask anything or describe homework...",
                                color = TextSubtle,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = state.topicInput,
                            onValueChange = onTopicChange,
                            textStyle = TextStyle(
                                color = PureWhite,
                                fontSize = 13.sp,
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
                                    if (!state.isLoading && (state.topicInput.isNotBlank() || attachedImage != null)) {
                                        onExplain()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("assignment_topic_input")
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Voice Mic
                    IconButton(
                        onClick = {
                            if (isListening) {
                                onStopVoice()
                            } else {
                                val hasAudioPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasAudioPermission) {
                                    onStartVoice()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isListening) PureWhite else Color.Transparent)
                            .testTag("assignment_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) TrueBlack else PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Send Action Button (Solid white circle with black arrow)
                    IconButton(
                        onClick = onExplain,
                        enabled = !state.isLoading && (state.topicInput.isNotBlank() || attachedImage != null),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (!state.isLoading && (state.topicInput.isNotBlank() || attachedImage != null)) PureWhite
                                else Color(0xFF262626)
                            )
                            .testTag("assignment_explain_button")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = TrueBlack,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Explain",
                                tint = if (state.topicInput.isNotBlank() || attachedImage != null) TrueBlack else TextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
