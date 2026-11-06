package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.WebSearchResultImage
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

/**
 * Beautiful, responsive online web image gallery displayed directly inside chat messages.
 * Shows high-resolution pictures and diagrams from Wikimedia Commons / Wikipedia
 * with creator attributions, source links, and copy/download actions.
 */
@Composable
fun WebImageSearchResultsGallery(
    images: List<WebSearchResultImage>,
    onDownload: (WebSearchResultImage) -> Unit,
    onCopyLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val context = LocalContext.current
    var selectedFullscreenImage by remember { mutableStateOf<WebSearchResultImage?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("web_image_gallery_container")
    ) {
        // Section Header: Globe icon + Results counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2838))
                    .border(1.dp, ChatGptPlusStarBlue.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = ChatGptPlusStarBlue,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Web Visuals & Diagrams (${images.size})",
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ChatGptPillDark,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    text = images.firstOrNull()?.sourceWebsite ?: "Wikimedia",
                    color = TextMutedGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        // Gallery Items (LazyRow for smooth horizontal scrolling on mobile phones)
        if (images.size == 1) {
            val single = images.first()
            SingleWebImageCard(
                image = single,
                onOpenFullscreen = { selectedFullscreenImage = single },
                onDownload = { onDownload(single) },
                onCopyLink = { onCopyLink(single.originalUrl) },
                onOpenSource = {
                    val url = single.sourcePageUrl.ifBlank { single.originalUrl }
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(images, key = { it.id }) { img ->
                    WebImageCard(
                        image = img,
                        onOpenFullscreen = { selectedFullscreenImage = img },
                        onDownload = { onDownload(img) },
                        onCopyLink = { onCopyLink(img.originalUrl) },
                        onOpenSource = {
                            val url = img.sourcePageUrl.ifBlank { img.originalUrl }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    )
                }
            }
        }
    }

    // Fullscreen Image Dialog with zoom & detailed attribution
    selectedFullscreenImage?.let { img ->
        FullscreenImageViewerDialog(
            image = img,
            onDismiss = { selectedFullscreenImage = null },
            onDownload = { onDownload(img) },
            onCopyLink = { onCopyLink(img.originalUrl) },
            onOpenSource = {
                val url = img.sourcePageUrl.ifBlank { img.originalUrl }
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )
    }
}

/**
 * Single full-width card when 1 relevant diagram/photo is returned.
 */
@Composable
fun SingleWebImageCard(
    image: WebSearchResultImage,
    onOpenFullscreen: (WebSearchResultImage) -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenFullscreen(image) }
    ) {
        Column {
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF0F1115)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = image.thumbnailUrl.ifBlank { image.originalUrl },
                    contentDescription = image.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextMutedGray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Image Preview Unavailable", color = TextMutedGray, fontSize = 11.sp)
                        }
                    }
                )

                // Zoom hint overlay button in bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TrueBlack.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Expand",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Metadata & Actions Bar
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = image.title,
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Author & License
                val authorText = image.author?.ifBlank { "Wikimedia Contributor" } ?: "Wikimedia Contributor"
                val licenseText = image.license ?: "Creative Commons"
                Text(
                    text = "By $authorText • $licenseText",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Source: ${image.sourceWebsite}",
                        color = ChatGptPlusStarBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Copy Link
                        IconButton(
                            onClick = onCopyLink,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                                .testTag("copy_image_link_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = PureWhite, modifier = Modifier.size(15.dp))
                        }

                        // Download
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                                .testTag("download_image_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download Image", tint = PureWhite, modifier = Modifier.size(16.dp))
                        }

                        // Open in Source
                        IconButton(
                            onClick = onOpenSource,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                                .testTag("open_image_source_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open Source Website", tint = PureWhite, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Carousel card for multiple image search results.
 */
@Composable
fun WebImageCard(
    image: WebSearchResultImage,
    onOpenFullscreen: () -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenFullscreen() }
    ) {
        Column {
            // Image Preview (150.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF0F1115)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = image.thumbnailUrl.ifBlank { image.originalUrl },
                    contentDescription = image.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextMutedGray, modifier = Modifier.size(24.dp))
                            Text("Image Error", color = TextMutedGray, fontSize = 10.sp)
                        }
                    }
                )

                // Magnifying glass tap indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(TrueBlack.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Expand",
                        tint = PureWhite,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Description info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = image.title,
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val author = image.author?.ifBlank { "Contributor" } ?: "Contributor"
                Text(
                    text = "By $author • ${image.license ?: "Free"}",
                    color = TextMutedGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = image.sourceWebsite,
                        color = ChatGptPlusStarBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCopyLink,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = PureWhite, modifier = Modifier.size(13.dp))
                        }

                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = PureWhite, modifier = Modifier.size(14.dp))
                        }

                        IconButton(
                            onClick = onOpenSource,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ChatGptPillDark)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open Source", tint = PureWhite, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Image Viewer Dialog.
 * Allows student to view high-res educational diagram, zoom in, inspect details,
 * review licensing and author attribution, and save image to device.
 */
@Composable
fun FullscreenImageViewerDialog(
    image: WebSearchResultImage,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenSource: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = TrueBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar: Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = image.title,
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Verified from ${image.sourceWebsite}",
                            color = ChatGptPlusStarBlue,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChatGptPillDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Image View Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A0C0E))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = image.originalUrl.ifBlank { image.thumbnailUrl },
                        contentDescription = image.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            SubcomposeAsyncImage(
                                model = image.thumbnailUrl,
                                contentDescription = image.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Attribution & Description Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Author / Creator:",
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = image.author?.ifBlank { "Wikimedia Contributor" } ?: "Wikimedia Contributor",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "License:",
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = image.license ?: "CC BY-SA",
                                color = Color(0xFF4ADE80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (!image.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = image.description,
                                color = TextMutedGray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: Download, Copy Link, Open in Browser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download Image Button
                    Surface(
                        onClick = onDownload,
                        shape = RoundedCornerShape(10.dp),
                        color = PureWhite,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("fullscreen_download_image_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download", color = TrueBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Copy Link Button
                    Surface(
                        onClick = onCopyLink,
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("fullscreen_copy_link_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Link", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Open Source in Browser
                    Surface(
                        onClick = onOpenSource,
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("fullscreen_open_source_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open Source Website", tint = PureWhite, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Loading Indicator displayed while internet image retrieval is active.
 */
@Composable
fun SearchingImagesLoadingBar(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14171D),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = ChatGptPlusStarBlue,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Searching Wikimedia & Web for verified images...",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Retrieving educational diagrams and visual references",
                    color = TextMutedGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
