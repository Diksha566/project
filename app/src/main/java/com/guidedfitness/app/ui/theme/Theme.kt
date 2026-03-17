package com.guidedfitness.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Bg = Color(0xFF121212)
private val Card1 = Color(0xFF1E1E22)
private val Card2 = Color(0xFF2A2A2E)
private val Primary = Color(0xFF7B61FF)
private val Accent = Color(0xFF4CAF50)
private val OnDark = Color(0xFFEDEDED)
private val OnDarkMuted = Color(0xFFB8B8B8)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Accent,
    tertiary = Accent,
    background = Bg,
    surface = Card1,
    surfaceVariant = Card2,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = OnDark,
    onSurface = OnDark,
    onSurfaceVariant = OnDarkMuted
)

@Composable
fun GuidedFitnessTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
