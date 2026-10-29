package com.example.mastermartini.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light only for EP2; a dark scheme is a stretch goal (see docs/PLAN.md).
// Dynamic color is off so Android 12+ wallpapers don't replace the brand colors.
private val LightColorScheme = lightColorScheme(
    primary = MartiniGreen,
    onPrimary = Color.White,
    primaryContainer = MartiniGreenContainer,
    onPrimaryContainer = OnMartiniGreenContainer,
    secondary = MartiniGoldDark,
    onSecondary = Color.White,
    secondaryContainer = MartiniGoldContainer,
    onSecondaryContainer = OnMartiniGoldContainer,
    tertiary = MartiniGold,
    onTertiary = MartiniInk,
    background = Color.White,
    onBackground = MartiniInk,
    surface = Color.White,
    onSurface = MartiniInk,
    surfaceVariant = MartiniMist,
    onSurfaceVariant = MartiniInkVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FA),
    surfaceContainer = MartiniMist,
    surfaceContainerHigh = Color(0xFFECEDEF),
    surfaceContainerHighest = Color(0xFFE6E7E9),
    outline = MartiniOutline,
    outlineVariant = MartiniOutlineVariant,
)

@Composable
fun MasterMartiniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
