package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Pure OLED Black & ChatGPT Dark Palette
val TrueBlack = Color(0xFF000000)
val DarkBackground = Color(0xFF0D0D0D)
val DarkSurface = Color(0xFF141414)
val DarkSurfaceElevated = Color(0xFF1C1C1C)
val BorderSubtle = Color(0xFF262626)
val BorderLight = Color(0xFF383838)

// Exact ChatGPT Colors from screenshots
val ChatGptPillDark = Color(0xFF212121)
val ChatGptPillHover = Color(0xFF2A2A2A)
val ChatGptPlusPillBg = Color(0xFF263544)
val ChatGptPlusStarBlue = Color(0xFF60A5FA)
val ChatGptPlusTextBlue = Color(0xFF93C5FD)
val ChatGptBlueWave = Color(0xFF2F69FF)
val ChatGptUserBubble = Color(0xFF132B6E)
val ChatGptIconGray = Color(0xFF9E9EA7)
val ChatGptInputPlaceholder = Color(0xFF8E8E93)
val ChatGptDrawerBg = Color(0xFF171717)

val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFECECF1)
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

