package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonochromeColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = TrueBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = PureWhite,
    secondary = OffWhite,
    onSecondary = TrueBlack,
    secondaryContainer = Color(0xFF222222),
    onSecondaryContainer = PureWhite,
    tertiary = TextMutedGray,
    onTertiary = PureWhite,
    background = TrueBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMutedGray,
    outline = BorderSubtle,
    outlineVariant = BorderLight,
    error = PureWhite,
    onError = TrueBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        typography = Typography,
        content = content
    )
}
