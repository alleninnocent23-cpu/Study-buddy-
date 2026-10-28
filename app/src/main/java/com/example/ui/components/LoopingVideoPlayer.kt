package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack

/**
 * Looping, muted video player composable built using TextureView and MediaPlayer.
 * Plays the provided raw resource in a continuous loop with zero volume.
 */
@Composable
fun LoopingVideoPlayer(
    modifier: Modifier = Modifier,
    @RawRes videoRawResId: Int = R.raw.scanner_robot,
    cornerRadius: Dp = 0.dp,
    hasBorder: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    showMuteBadge: Boolean = false,
    badgeText: String = ""
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isVideoReady by remember { mutableStateOf(false) }

    // Lifecycle observer to pause/resume playback
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        if (activeMediaPlayer?.isPlaying == true) {
                            activeMediaPlayer?.pause()
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        if (isVideoReady && activeMediaPlayer?.isPlaying == false) {
                            activeMediaPlayer?.start()
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        activeMediaPlayer?.stop()
                        activeMediaPlayer?.release()
                        activeMediaPlayer = null
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                activeMediaPlayer?.stop()
                activeMediaPlayer?.release()
                activeMediaPlayer = null
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    var boxModifier = modifier
    if (cornerRadius > 0.dp) {
        boxModifier = boxModifier.clip(RoundedCornerShape(cornerRadius))
    }
    if (backgroundColor != Color.Transparent) {
        boxModifier = boxModifier.background(backgroundColor)
    }
    if (hasBorder) {
        boxModifier = boxModifier.border(1.dp, BorderSubtle, RoundedCornerShape(cornerRadius))
    }
    boxModifier = boxModifier.testTag("looping_video_box")

    Box(
        modifier = boxModifier
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        private var surface: Surface? = null

                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val surf = Surface(surfaceTexture)
                            surface = surf
                            try {
                                val player = MediaPlayer().apply {
                                    setSurface(surf)
                                    // Try loading via Raw Resource Descriptor first
                                    try {
                                        val afd = ctx.resources.openRawResourceFd(videoRawResId)
                                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                        afd.close()
                                    } catch (e: Exception) {
                                        // Fallback to Uri
                                        val uri = Uri.parse("android.resource://${ctx.packageName}/$videoRawResId")
                                        setDataSource(ctx, uri)
                                    }

                                    isLooping = true
                                    // Mute the audio (volume 0f on left and right channels)
                                    setVolume(0f, 0f)

                                    setOnPreparedListener { mp ->
                                        isVideoReady = true
                                        mp.start()
                                    }

                                    prepareAsync()
                                }
                                activeMediaPlayer = player
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {}

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            try {
                                activeMediaPlayer?.stop()
                                activeMediaPlayer?.release()
                                activeMediaPlayer = null
                            } catch (e: Exception) {
                                // ignore
                            }
                            surface?.release()
                            surface = null
                            isVideoReady = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                    }
                }
            }
        )

        // Overlay badge indicating muted status and looping scanner mascot
        if (showMuteBadge) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0xCC111111), RoundedCornerShape(20.dp))
                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Muted",
                    tint = TextMutedGray,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badgeText,
                    color = PureWhite,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
