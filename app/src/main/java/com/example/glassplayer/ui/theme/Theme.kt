package com.example.glassplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Color de base (blanco suave) que se ve antes de que cargue el fondo. */
val GlassBase = Color(0xFFF7F7F8)

private val Colors = darkColorScheme(
    background = GlassBase,
    surface = GlassBase,
    primary = Color.White,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun GlassPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
}
