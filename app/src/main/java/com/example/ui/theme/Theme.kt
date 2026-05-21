package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = ElectricCyan,
    tertiary = GoldenLegend,
    background = AnimeBackground,
    surface = AnimeSurface,
    onPrimary = CharcoalDark,
    onSecondary = CharcoalDark,
    onBackground = SolidWhite,
    onSurface = SolidWhite,
    surfaceVariant = DeepViolet,
    onSurfaceVariant = SoftViolet,
    error = AlertOrange,
    onError = CharcoalDark
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPink,
    secondary = ElectricCyan,
    tertiary = GoldenLegend,
    background = AnimeBackground,
    surface = AnimeSurface,
    onPrimary = CharcoalDark,
    onSecondary = CharcoalDark,
    onBackground = SolidWhite,
    onSurface = SolidWhite,
    surfaceVariant = DeepViolet,
    onSurfaceVariant = SoftViolet,
    error = AlertOrange,
    onError = CharcoalDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force dark theme as the default Google Premium Dark style for optimal contrast and battery efficiency
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
