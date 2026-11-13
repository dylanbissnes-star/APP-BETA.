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
 * Paleta "Glasslim": cristal ahumado sobre un fondo (tela, bruma o la portada). Todo es
 * monocromo (blanco/negro) y el acento elegido se reserva para indicadores y detalles.
 *  - Oscuro: cristal ahumado casi negro, texto blanco, boton principal blanco.
 *  - Claro: cristal esmerilado blanco, texto casi negro, boton principal oscuro.
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
    val bgColors: List<Color>,
    /** Relleno de tarjetas de cristal (translucido). */
    val surface: Color,
    /** Elementos elevados: botones redondos, campos, pestana activa. */
    val surfaceHigh: Color,
    val outline: Color,
    /** Superficie casi opaca de reserva para paneles flotantes (cuando no hay desenfoque real). */
    val floating: Color,
    /** Tinte de los paneles flotantes sobre el fondo desenfocado. */
    val glassTint: Color,
    /** Brillo suave en la parte alta de cada cristal. */
    val sheen: Color,
    /** Borde del cristal: esquina iluminada y esquina en sombra. */
    val edgeLight: Color,
    val edgeDark: Color,
    /** Velo sobre el fondo para que el texto se lea. */
    val scrim: Color,
    /** Acento legible sobre este fondo (indicadores, iconos activos, corazon). */
    val accent: Color,
    /** Relleno del boton principal (blanco en oscuro, oscuro en claro). */
    val accentFill: Color,
    /** Color de texto/icono que se lee sobre `accentFill`. */
    val onAccent: Color,
)

/** Acento por defecto (rojo de los indicadores de la referencia); se cambia en Ajustes. */
val DefaultAccent = Color(0xFFFF3B30)

/** Texto oscuro o claro, segun cual se lea mejor sobre `c`. */
fun onAccentFor(c: Color): Color = if (c.luminance() > 0.5f) Color(0xFF111113) else Color.White

/** Acerca el color a blanco/negro hasta que contraste lo suficiente con `bg`. */
private fun Color.readableOn(bg: Color, min: Float = 3f): Color {
    val target = if (bg.luminance() < 0.5f) Color.White else Color.Black
    var c = this
    var i = 0
    while (ColorUtils.calculateContrast(c.toArgb(), bg.copy(alpha = 1f).toArgb()) < min && i < 12) {
        c = lerp(c, target, 0.12f)
        i++
    }
    return c
}

/** Paleta clara u oscura; el acento solo tine indicadores. */
fun frostFor(dark: Boolean, accent: Color): FrostColors = if (dark) darkGlass(accent) else lightGlass(accent)

private fun darkGlass(accent: Color): FrostColors {
    val bg = Color(0xFF0D0D0E)
    return FrostColors(
        onDark = true,
        content = Color.White,
        secondary = Color.White.copy(alpha = 0.70f),
        tertiary = Color.White.copy(alpha = 0.45f),
        inverse = Color(0xFF0F0F10),
        bg = bg,
        bgColors = listOf(bg, bg),
        surface = Color(0xFF181513).copy(alpha = 0.52f),
        surfaceHigh = Color.White.copy(alpha = 0.10f),
        outline = Color.White.copy(alpha = 0.12f),
        floating = Color(0xF21B1816),
        glassTint = Color(0xFF1C1917).copy(alpha = 0.50f),
        sheen = Color.White.copy(alpha = 0.08f),
        edgeLight = Color.White.copy(alpha = 0.26f),
        edgeDark = Color.White.copy(alpha = 0.06f),
        scrim = Color.Black.copy(alpha = 0.30f),
        accent = accent.readableOn(bg, 3f),
        accentFill = Color.White,
        onAccent = Color(0xFF0F0F10),
    )
}

private fun lightGlass(accent: Color): FrostColors {
    val bg = Color(0xFFE9E7E5)
    val ink = Color(0xFF17171A)
    return FrostColors(
        onDark = false,
        content = ink,
        secondary = ink.copy(alpha = 0.66f),
        tertiary = ink.copy(alpha = 0.45f),
        inverse = Color.White,
        bg = bg,
        bgColors = listOf(bg, bg),
        surface = Color.White.copy(alpha = 0.55f),
        surfaceHigh = Color.Black.copy(alpha = 0.06f),
        outline = Color.Black.copy(alpha = 0.09f),
        floating = Color(0xF2F3F1F0),
        glassTint = Color.White.copy(alpha = 0.55f),
        sheen = Color.White.copy(alpha = 0.55f),
        edgeLight = Color.White.copy(alpha = 0.95f),
        edgeDark = Color.Black.copy(alpha = 0.06f),
        scrim = Color.White.copy(alpha = 0.42f),
        accent = accent.readableOn(bg, 3f),
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

    /** Tinte de los paneles flotantes de cristal. */
    val GlassTint: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.glassTint

    /** Brillo suave en la parte alta del cristal. */
    val Sheen: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.sheen

    /** Borde del cristal (esquina iluminada / en sombra). */
    val EdgeLight: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.edgeLight
    val EdgeDark: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.edgeDark

    /** Velo sobre el fondo. */
    val Scrim: Color @Composable @ReadOnlyComposable get() = LocalFrost.current.scrim

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
