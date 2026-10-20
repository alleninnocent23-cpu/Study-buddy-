package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Pure Monochrome Black & White Palette
val TrueBlack = Color(0xFF000000)
val DarkBackground = Color(0xFF0D0D0D)
val DarkSurface = Color(0xFF141414)
val DarkSurfaceElevated = Color(0xFF1C1C1C)
val BorderSubtle = Color(0xFF2E2E2E)
val BorderLight = Color(0xFF444444)

val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF2F2F2)
val TextMutedGray = Color(0xFF888888)
val TextSubtle = Color(0xFF555555)

// Neutral indicators
val SuccessWhite = Color(0xFFE2E8F0)
val ActiveGray = Color(0xFF262626)

// Gradients (Monochrome)
val BlackWhiteGradient = Brush.verticalGradient(
    colors = listOf(TrueBlack, DarkBackground)
)

val MinimalCardBorder = Brush.linearGradient(
    colors = listOf(Color(0xFF333333), Color(0xFF1A1A1A))
)
