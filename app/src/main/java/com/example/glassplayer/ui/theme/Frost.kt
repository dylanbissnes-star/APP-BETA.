package com.example.glassplayer.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max

/**
 * Paleta "Frosted Glass": cristal esmerilado claro sobre un fondo de color.
 * Texto siempre blanco (mas o menos opaco); el color lo ponen el fondo de esferas y las portadas.
 */
object Frost {
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.76f)
    val TextTertiary = Color.White.copy(alpha = 0.52f)

    /** Tinte indigo oscuro que se mezcla bajo el cristal para dar contraste al texto blanco. */
    val Tint = Color(0xFF241447)

    /** Acento de la barra de progreso (rojo-rosa, como en la referencia). */
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
