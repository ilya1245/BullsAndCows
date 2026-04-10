package com.example.bullsandcows.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BullColor  = Color(0xFF2E7D32)
val CowColor   = Color(0xFFE65100)
val WinRowBg   = Color(0xFFC8E6C9)
val RevealRowBg = Color(0xFFFFF9C4)

private val LightColors = lightColorScheme(
    primary   = Color(0xFFFF6B35),
    secondary = Color(0xFF8B4513)
)

private val DarkColors = darkColorScheme(
    primary   = Color(0xFFFFAB76),
    secondary = Color(0xFFD4956A)
)

@Composable
fun BullsAndCowsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}
