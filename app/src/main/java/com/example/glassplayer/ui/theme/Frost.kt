package com.example.glassplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max

/**
 * Colores de contenido segun la superficie sobre la que se dibuja:
 *  - superficie CLARA (fondo blanco): texto grafito, cristal blanco con borde gris
 *  - superficie OSCURA (modo oscuro, o portada desenfocada con velo): texto blanco, cristal translucido
 */
@Immutable
class FrostColors(
    val onDark: Boolean,
    val content: Color,
    val secondary: Color,
    val tertiary: Color,
    val inverse: Color,
)

val LightFrost = FrostColors(
    onDark = false,
    content = Color(0xFF1C1C1E),
    secondary = Color(0xFF1C1C1E).copy(alpha = 0.62f),
    tertiary = Color(0xFF1C1C1E).copy(alpha = 0.42f),
    inverse = Color.White,
)

val DarkFrost = FrostColors(
    onDark = true,
    content = Color.White,
    secondary = Color.White.copy(alpha = 0.76f),
    tertiary = Color.White.copy(alpha = 0.52f),
    inverse = Color(0xFF1B1B20),
)

val LocalFrost = staticCompositionLocalOf { LightFrost }

object Frost {
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.content
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.secondary
    val TextTertiary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.tertiary

    /** Color opuesto al texto (para el texto dentro de insignias rellenas con el color de contenido). */
    val Inverse: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.inverse

    /** `true` si ahora se dibuja sobre una superficie oscura. */
    val OnDark: Boolean @Composable @ReadOnlyComposable get() = LocalFrost.current.onDark

    /** Color de contenido con una opacidad dada (iconos, bordes y rellenos tenues). */
    @Composable
    @ReadOnlyComposable
    fun content(alpha: Float): Color = LocalFrost.current.content.copy(alpha = alpha)

    /** Tinte grafito para sombras y para dar contraste bajo el cristal. */
    val Tint = Color(0xFF1B1B20)

    /** Acento de la barra de progreso (rojo-rosa). */
    val Accent = Color(0xFFE5365F)
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
