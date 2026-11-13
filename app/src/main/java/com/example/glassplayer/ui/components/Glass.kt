package com.example.glassplayer.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.glassplayer.ui.theme.Frost

/**
 * Cristal esmerilado de verdad.
 *
 * `glassSource` graba todo lo que hay debajo (fondo + pantallas) en una capa. Cada panel
 * flotante (`glassPanel`) vuelve a dibujar esa capa a su espalda, desenfocada y con un tinte
 * ahumado, asi se ve lo que pasa por detras como en un cristal. El desenfoque necesita
 * Android 12+; antes el panel usa un relleno casi opaco.
 *
 * Los paneles NO deben estar dentro del contenido marcado como fuente (se verian a si mismos).
 */
class GlassState internal constructor(internal val layer: GraphicsLayer)

val LocalGlass = compositionLocalOf<GlassState?> { null }

@Composable
fun rememberGlassState(): GlassState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassState(layer) }
}

/** Marca este contenido como lo que se ve a traves de los paneles de cristal. */
fun Modifier.glassSource(state: GlassState): Modifier = drawWithContent {
    val w = size.width.toInt().coerceAtLeast(1)
    val h = size.height.toInt().coerceAtLeast(1)
    state.layer.record(this, layoutDirection, IntSize(w, h)) {
        this@drawWithContent.drawContent()
    }
    drawLayer(state.layer)
}

/** Borde de cristal: mas claro en la esquina superior izquierda. */
@Composable
private fun glassBorderBrush(): Brush =
    Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark, Frost.EdgeDark.copy(alpha = Frost.EdgeDark.alpha * 0.6f)))

/**
 * Panel flotante de cristal con el fondo desenfocado detras. Se usa en la barra de pestanas, el
 * mini reproductor, el menu lateral y los avisos.
 */
fun Modifier.glassPanel(shape: Shape, blur: Dp = 26.dp): Modifier = composed {
    val glass = LocalGlass.current
    val panelLayer = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }
    val real = glass != null && Build.VERSION.SDK_INT >= 31
    val tint = Frost.GlassTint
    val fallback = Frost.Floating
    val sheen = Frost.Sheen
    val border = glassBorderBrush()

    this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            if (real && glass != null) {
                val w = size.width.toInt().coerceAtLeast(1)
                val h = size.height.toInt().coerceAtLeast(1)
                val r = blur.toPx()
                panelLayer.renderEffect = BlurEffect(r, r, TileMode.Clamp)
                panelLayer.record(this, layoutDirection, IntSize(w, h)) {
                    translate(left = -origin.x, top = -origin.y) { drawLayer(glass.layer) }
                }
                val path = Path().apply { addOutline(outline) }
                clipPath(path) { drawLayer(panelLayer) }
                drawOutline(outline, tint)
            } else {
                drawOutline(outline, fallback)
            }
            drawOutline(outline, Brush.verticalGradient(listOf(sheen, sheen.copy(alpha = 0f))))
        }
        .border(1.dp, border, shape)
}

/**
 * Tarjeta de cristal para el contenido (filas, columnas, campos): relleno ahumado translucido,
 * brillo suave arriba y borde fino. `raised` la aclara un poco (elementos activos).
 */
fun Modifier.card(shape: Shape, raised: Boolean = false, outlined: Boolean = true): Modifier = composed {
    val fill = Frost.Surface
    val high = Frost.SurfaceHigh
    val sheen = Frost.Sheen
    val border = glassBorderBrush()
    this
        .clip(shape)
        .background(fill)
        .then(if (raised) Modifier.background(high) else Modifier)
        .background(Brush.verticalGradient(listOf(sheen, sheen.copy(alpha = 0f))))
        .then(if (outlined) Modifier.border(1.dp, border, shape) else Modifier)
}

/** Linea fina que separa grupos (como las del menu de la referencia). */
@Composable
fun hairlineBrush(): Brush {
    val c = Frost.Outline
    return Brush.horizontalGradient(listOf(c.copy(alpha = 0f), c, c.copy(alpha = 0f)))
}
