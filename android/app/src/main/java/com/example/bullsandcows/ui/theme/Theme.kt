package com.example.bullsandcows.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme colours
private val BullColorLight   = Color(0xFF2E7D32)
private val CowColorLight    = Color(0xFFE65100)
private val WinRowBgLight    = Color(0xFFC8E6C9)
private val RevealRowBgLight = Color(0xFFFFF9C4)

// Dark theme colours
private val BullColorDark    = Color(0xFF66BB6A)
private val CowColorDark     = Color(0xFFFF8A65)
private val WinRowBgDark     = Color(0xFF1B3A1F)
private val RevealRowBgDark  = Color(0xFF3D3519)

@Composable fun bullColor():    Color = if (isSystemInDarkTheme()) BullColorDark    else BullColorLight
@Composable fun cowColor():     Color = if (isSystemInDarkTheme()) CowColorDark     else CowColorLight
@Composable fun winRowBg():     Color = if (isSystemInDarkTheme()) WinRowBgDark     else WinRowBgLight
@Composable fun revealRowBg():  Color = if (isSystemInDarkTheme()) RevealRowBgDark  else RevealRowBgLight

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
