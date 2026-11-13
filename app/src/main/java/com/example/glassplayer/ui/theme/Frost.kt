package com.example.glassplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max

/**
 * Paleta "Polaris". Dos estilos, ambos derivados del color de acento elegido:
 *  - Oscuro ("Neon"): negro con cuadricula tenue, tarjetas gris oscuro con contorno fino y el
 *    acento en neon para botones, titulares y detalles.
 *  - Claro ("Soft"): fondo gris suave, tarjetas blancas con sombra, tinta oscura del tono del
 *    acento y botones con el acento.
 */
@Immutable
class FrostColors(
    val onDark: Boolean,
    val content: Color,
    val secondary: Color,
    val tertiary: Color,
    val inverse: Color,
    /** Color medio del fondo (para calcular contrastes). */
    val bg: Color,
    /** Colores del degradado vertical de fondo, de arriba a abajo. */
    val bgColors: List<Color>,
    val surface: Color,
    val surfaceHigh: Color,
    val outline: Color,
    /** Superficie opaca para barras y paneles flotantes (barra de pestanas, menu lateral, avisos). */
    val floating: Color,
    /** Acento legible sobre este fondo (texto, iconos, lineas finas). */
    val accent: Color,
    /** Relleno de botones principales, insignias y el boton de play. */
    val accentFill: Color,
    /** Color de texto/icono que se lee sobre `accentFill`. */
    val onAccent: Color,
)

/** Acento por defecto (verde neon); la persona puede cambiarlo en Ajustes. */
val DefaultAccent = Color(0xFFE5383B)

/** Texto oscuro o claro, segun cual se lea mejor sobre `c`. */
fun onAccentFor(c: Color): Color = if (c.luminance() > 0.5f) Color(0xFF111113) else Color.White

/** Acerca el color a blanco/negro hasta que contraste lo suficiente con `bg`. */
private fun Color.readableOn(bg: Color, min: Float = 3f): Color {
    val target = if (bg.luminance() < 0.5f) Color.White else Color.Black
    var c = this
    var i = 0
    while (ColorUtils.calculateContrast(c.toArgb(), bg.toArgb()) < min && i < 12) {
        c = lerp(c, target, 0.12f)
        i++
    }
    return c
}

private fun hsvColor(h: Float, s: Float, v: Float): Color =
    Color.hsv(h.coerceIn(0f, 360f), s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))

/** Paleta clara u oscura derivada del acento elegido. */
fun frostFor(dark: Boolean, accent: Color): FrostColors {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(accent.toArgb(), hsv)
    val h = hsv[0]
    val gray = hsv[1] < 0.12f // un gris no tiene tono: el fondo tambien queda gris
    return if (dark) darkPulse(accent, h, gray, hsv[1]) else lightSoft(accent, h, gray, hsv[1])
}

private fun darkPulse(accent: Color, h: Float, gray: Boolean, s: Float): FrostColors {
    // Vidrio oscuro sobre fondo fotografico: superficies blancas muy translucidas, contorno fino,
    // botones principales blancos; el acento elegido marca indicadores, puntos y brillos.
    val black = Color(0xFF0B0B0D)
    return FrostColors(
        onDark = true,
        content = Color.White,
        secondary = Color.White.copy(alpha = 0.66f),
        tertiary = Color.White.copy(alpha = 0.42f),
        inverse = black,
        bg = black,
        bgColors = listOf(black, black),
        surface = Color.White.copy(alpha = 0.07f),
        surfaceHigh = Color.White.copy(alpha = 0.13f),
        outline = Color.White.copy(alpha = 0.14f),
        floating = Color(0xE6111114),
        accent = accent.readableOn(black, 4.5f),
        accentFill = Color.White,
        onAccent = Color(0xFF0B0B0D),
    )
}

private fun lightSoft(accent: Color, h: Float, gray: Boolean, s: Float): FrostColors {
    // Vidrio claro: cristal blanco esmerilado sobre un fondo suave, tinta oscura y botones negros.
    val ink = Color(0xFF16161A)
    val bg = Color(0xFFE9E9EE)
    return FrostColors(
        onDark = false,
        content = ink,
        secondary = ink.copy(alpha = 0.64f),
        tertiary = ink.copy(alpha = 0.44f),
        inverse = Color.White,
        bg = bg,
        bgColors = listOf(Color(0xFFF1F1F5), Color(0xFFE2E2E8)),
        surface = Color.White.copy(alpha = 0.55f),
        surfaceHigh = Color.White.copy(alpha = 0.80f),
        outline = Color.White.copy(alpha = 0.85f),
        floating = Color(0xEEFFFFFF),
        accent = accent.readableOn(bg),
        accentFill = ink,
        onAccent = Color.White,
    )
}

val DarkFrost = frostFor(true, DefaultAccent)
val LightFrost = frostFor(false, DefaultAccent)

val LocalFrost = staticCompositionLocalOf { DarkFrost }

object Frost {
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.content
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.secondary
    val TextTertiary: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.tertiary

    /** Color opuesto al texto (para el texto dentro de insignias rellenas con el color de contenido). */
    val Inverse: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.inverse

    /** Fondo de pantalla. */
    val Bg: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.bg

    /** Degradado de fondo de pantalla. */
    val BgBrush: Brush @Composable @ReadOnlyComposable get() = Brush.verticalGradient(LocalFrost.current.bgColors)

    /** Superficie opaca de barras y paneles flotantes. */
    val Floating: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.floating

    /** Tarjetas y paneles. */
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.surface

    /** Elementos elevados: botones redondos, campos, pildoras. */
    val SurfaceHigh: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.surfaceHigh

    /** Contorno fino de tarjetas y separadores. */
    val Outline: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.outline

    /** Acento (pestana activa, corazon, canciones sonando). */
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.accent

    /** El acento elegido, para rellenos (botones, insignias, play). */
    val AccentFill: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.accentFill

    /** Texto/icono sobre `AccentFill`. */
    val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.onAccent

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
