package com.example.glassplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Color de base (indigo profundo) que se ve antes de que cargue el fondo de esferas. */
val GlassBase = Color(0xFF2A1B5E)

private val Colors = darkColorScheme(
    background = GlassBase,
    surface = GlassBase,
    primary = Color.White,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp),
)

@Composable
fun GlassPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
}
