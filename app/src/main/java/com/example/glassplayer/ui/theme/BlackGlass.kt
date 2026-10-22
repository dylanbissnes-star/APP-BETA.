package com.example.glassplayer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta "Black Glass": SOLO negros, grises, transparencias y blancos.
 * Sin colores de acento. La unica fuente de color son las portadas de los discos.
 */
object BlackGlass {
    val Black = Color(0xFF000000)            // negro absoluto
    val Deep = Color(0xFF080808)             // negro profundo
    val Surface = Color(0xFF101010)          // superficies oscuras
    val GlassFill = Color.White.copy(alpha = 0.045f)   // paneles de cristal
    val GlassBorder = Color.White.copy(alpha = 0.12f)  // bordes translucidos
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFF909090)
    val Shine = Color.White.copy(alpha = 0.18f)        // brillos
}

/** Convierte cualquier color a su gris equivalente (para que el ambiente sea monocromatico). */
fun Color.toGray(): Color {
    val l = 0.299f * red + 0.587f * green + 0.114f * blue
    return Color(l, l, l, alpha)
}
