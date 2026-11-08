package com.example.glassplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max

/**
 * Paleta "Studio": superficies solidas (fondo, tarjetas, tarjetas elevadas y contorno fino),
 * texto y un acento. Dos variantes: oscura (por defecto, como las referencias) y clara.
 */
@Immutable
class FrostColors(
    val onDark: Boolean,
    val content: Color,
    val secondary: Color,
    val tertiary: Color,
    val inverse: Color,
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val outline: Color,
    val accent: Color,
)

val LightFrost = FrostColors(
    onDark = false,
    content = Color(0xFF111113),
    secondary = Color(0xFF111113).copy(alpha = 0.62f),
    tertiary = Color(0xFF111113).copy(alpha = 0.42f),
    inverse = Color.White,
    bg = Color(0xFFF4F4F6),
    surface = Color.White,
    surfaceHigh = Color(0xFFECECF0),
    outline = Color(0xFFE1E1E6),
    accent = Color(0xFF6D4AE0),
)

val DarkFrost = FrostColors(
    onDark = true,
    content = Color.White,
    secondary = Color.White.copy(alpha = 0.62f),
    tertiary = Color.White.copy(alpha = 0.42f),
    inverse = Color(0xFF111113),
    bg = Color(0xFF0E0E10),
    surface = Color(0xFF18181B),
    surfaceHigh = Color(0xFF232327),
    outline = Color(0xFF2C2C32),
    accent = Color(0xFFB39DFF),
)

val LocalFrost = staticCompositionLocalOf { DarkFrost }

object Frost {
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.content
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.secondary
    val TextTertiary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.tertiary

    /** Color opuesto al texto (para el texto dentro de insignias rellenas con el color de contenido). */
    val Inverse: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.inverse

    /** Fondo de pantalla. */
    val Bg: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.bg

    /** Tarjetas y paneles. */
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.surface

    /** Elementos elevados: botones redondos, campos, pildoras. */
    val SurfaceHigh: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.surfaceHigh

    /** Contorno fino de tarjetas y separadores. */
    val Outline: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.outline

    /** Acento (pestana activa, corazon, canciones sonando). */
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.accent

    /** `true` si ahora se dibuja sobre una superficie oscura. */
    val OnDark: Boolean @Composable @ReadOnlyComposable get() = LocalFrost.current.onDark

    /** Color de contenido con una opacidad dada (iconos y rellenos tenues). */
    @Composable
    @ReadOnlyComposable
    fun content(alpha: Float): Color = LocalFrost.current.content.copy(alpha = alpha)

    /** Color para sombras. */
    val Tint = Color(0xFF000000)
}

/**
 * Hace un color mas vivo (saturacion/brillo minimos). Si el color es casi gris,
 * devuelve `fallback` porque un gris no tiene tono que realzar.
 */
fun Color.vividOr(fallback: Color, minSat: Float = 0.5f, minValue: Float = 0.80f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    if (hsv[1] < 0.12f) return fallback
    hsv[1] = max(hsv[1], minSat)
    hsv[2] = max(hsv[2], minValue)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
