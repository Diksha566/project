package com.guidedfitness.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF4CAF50)
private val GreenLight = Color(0xFF81C784)
private val GreenDark = Color(0xFF388E3C)
private val TealSecondary = Color(0xFF009688)

private val DarkColorScheme = darkColorScheme(
    primary = GreenLight,
    secondary = TealSecondary,
    tertiary = GreenDark
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = TealSecondary,
    tertiary = GreenDark
)

@Composable
fun GuidedFitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
