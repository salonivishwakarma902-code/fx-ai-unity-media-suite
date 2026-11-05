package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FxDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF001E2E),
    primaryContainer = CyanContainer,
    onPrimaryContainer = OnCyanContainer,
    secondary = SunsetAmber,
    onSecondary = Color(0xFF281500),
    secondaryContainer = SunsetContainer,
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = NeuralViolet,
    onTertiary = Color.White,
    tertiaryContainer = NeuralVioletContainer,
    onTertiaryContainer = Color(0xFFF3E8FF),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianGlassBorder,
    error = ErrorCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
