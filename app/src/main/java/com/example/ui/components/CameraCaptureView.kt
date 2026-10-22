package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * CameraX Fullscreen Capture Dialog for Homework Assignments.
 * Provides live viewfinder, flash controls, lens selector, document alignment frame,
 * high-resolution capture, and post-capture review before sending to AI.
 */
@Composable
fun CameraCaptureDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        CameraCaptureScreen(
            onClose = onDismiss,
            onPhotoConfirmed = { bitmap ->
                onPhotoCaptured(bitmap)
                onDismiss()
            }
        )
    }
}

@Composable
fun CameraCaptureScreen(
    onClose: () -> Unit,
    onPhotoConfirmed: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    val cameraExecutor: Executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(flashMode) {
        try {
            imageCapture.flashMode = flashMode
        } catch (_: Exception) { }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = DarkSurface,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Camera Access Required",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Study Buddy uses the camera to scan homework sheets, textbook questions, and math equations for instant AI solving.",
                    color = TextMutedGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("camera_grant_permission_button")
                ) {
                    Text(
                        text = "Enable Camera",
                        color = TrueBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMutedGray),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(44.dp)
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }
            }
        } else if (capturedBitmap != null) {
            // Review Captured Photo UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Review Assignment Photo",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { capturedBitmap = null },
                        modifier = Modifier.testTag("camera_review_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                    }
                }

                // Image display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Assignment",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Review Action Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { capturedBitmap = null },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("camera_retake_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retake", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }

                    Button(
                        onClick = {
                            capturedBitmap?.let { onPhotoConfirmed(it) }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("camera_use_photo_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solve with AI", color = TrueBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        } else {
            // Live CameraX Viewfinder
            var cameraControl by remember { mutableStateOf<Camera?>(null) }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            cameraControl = cam
                        } catch (e: Exception) {
                            Log.e("CameraCaptureView", "Use case binding failed", e)
                            errorMessage = e.localizedMessage
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    // Update when lensFacing changes
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(it.context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { p ->
                                p.setSurfaceProvider(it.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            cameraControl = cam
                        } catch (e: Exception) {
                            Log.e("CameraCaptureView", "Error updating camera", e)
                        }
                    }, ContextCompat.getMainExecutor(it.context))
                },
                modifier = Modifier.fillMaxSize()
            )

            // Homework Scanning Framing Box Overlay
            AssignmentFramingOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 90.dp)
            )

            // Top Camera Controls Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("camera_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Homework Vision",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Flash toggle button
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("camera_flash_toggle_button")
                ) {
                    val icon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Flash Toggle",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Shutter & Switch Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                        )
                    )
                    .navigationBarsPadding()
                    .padding(vertical = 24.dp, horizontal = 24.dp)
            ) {
                // Switch Camera Lens (Front / Back)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .testTag("camera_switch_lens_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Shutter Button
                ShutterButton(
                    isCapturing = isCapturing,
                    onClick = {
                        if (isCapturing) return@ShutterButton
                        isCapturing = true
                        errorMessage = null

                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            executor = cameraExecutor,
                            onPhotoCaptured = { bitmap ->
                                isCapturing = false
                                capturedBitmap = bitmap
                            },
                            onError = { exc ->
                                isCapturing = false
                                errorMessage = exc.localizedMessage ?: "Failed to capture photo"
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("camera_shutter_button")
                )

                // Guidance text / status
                Text(
                    text = if (isCapturing) "Processing..." else "Tap to snap",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }

            // Error snackbar if any
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Red.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 64.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = PureWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Camera Shutter Button with animated press feedback.
 */
@Composable
private fun ShutterButton(
    isCapturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f, label = "shutter_scale")

    Box(
        modifier = modifier
            .size(76.dp)
            .scale(scale)
            .border(4.dp, PureWhite, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(if (isCapturing) TextMutedGray else PureWhite)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCapturing,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCapturing) {
            CircularProgressIndicator(
                color = TrueBlack,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(PureWhite)
            )
        }
    }
}

/**
 * Modern document framing overlay with 4 corner guide brackets
 * to help users frame textbook questions or math equations neatly.
 */
@Composable
private fun AssignmentFramingOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Document Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp)
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Corner Bracket Visual Accents (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
            CornerAccent(modifier = Modifier.align(Alignment.TopStart), isTop = true, isLeft = true)
            CornerAccent(modifier = Modifier.align(Alignment.TopEnd), isTop = true, isLeft = false)
            CornerAccent(modifier = Modifier.align(Alignment.BottomStart), isTop = false, isLeft = true)
            CornerAccent(modifier = Modifier.align(Alignment.BottomEnd), isTop = false, isLeft = false)

            // Centered helper pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Frame homework problem or formula inside",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CornerAccent(
    modifier: Modifier,
    isTop: Boolean,
    isLeft: Boolean
) {
    val cornerLength = 22.dp
    val thickness = 3.dp

    Box(modifier = modifier) {
        // Horizontal bar
        Box(
            modifier = Modifier
                .align(if (isTop) Alignment.TopStart else Alignment.BottomStart)
                .width(cornerLength)
                .height(thickness)
                .background(PureWhite)
        )
        // Vertical bar
        Box(
            modifier = Modifier
                .align(if (isLeft) Alignment.TopStart else Alignment.TopEnd)
                .width(thickness)
                .height(cornerLength)
                .background(PureWhite)
        )
    }
}

/**
 * Capture photo via ImageCapture and convert properly rotated Bitmap
 */
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onPhotoCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val rawBitmap = imageProxy.toBitmap()
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                    val finalBitmap = if (rotationDegrees != 0) {
                        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                        Bitmap.createBitmap(
                            rawBitmap,
                            0,
                            0,
                            rawBitmap.width,
                            rawBitmap.height,
                            matrix,
                            true
                        )
                    } else {
                        rawBitmap
                    }

                    ContextCompat.getMainExecutor(context).execute {
                        onPhotoCaptured(finalBitmap)
                    }
                } catch (e: Exception) {
                    Log.e("CameraCaptureView", "Error converting image proxy", e)
                } finally {
                    imageProxy.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCaptureView", "Photo capture failed: ${exception.message}", exception)
                ContextCompat.getMainExecutor(context).execute {
                    onError(exception)
                }
            }
        }
    )
}
