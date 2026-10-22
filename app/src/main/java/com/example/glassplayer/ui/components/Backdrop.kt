package com.example.glassplayer.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desenfoque de fondo ("backdrop blur") para paneles de cristal.
 *
 *  - `backdropSource` graba en una capa todo lo que hay DETRAS de los paneles.
 *  - `backdropBlur` (la usa `glass(backdrop = ...)`) redibuja esa capa desenfocada
 *    dentro del panel, alineada con su posicion en pantalla.
 *
 * Solo en Android 12+ (API 31). En versiones anteriores no hace nada y el panel
 * se queda con su translucidez normal.
 */
class BackdropState internal constructor(internal val layer: GraphicsLayer) {
    internal var origin: Offset = Offset.Zero
}

@Composable
fun rememberBackdropState(): BackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { BackdropState(layer) }
}

fun Modifier.backdropSource(state: BackdropState): Modifier {
    if (Build.VERSION.SDK_INT < 31) return this
    return this
        .onGloballyPositioned { state.origin = it.positionInRoot() }
        .drawWithContent {
            state.layer.record { this@drawWithContent.drawContent() }
            drawLayer(state.layer)
        }
}

fun Modifier.backdropBlur(state: BackdropState, radius: Dp = 24.dp): Modifier = composed {
    if (Build.VERSION.SDK_INT < 31) return@composed Modifier
    val blurLayer = rememberGraphicsLayer()
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Modifier
        .onGloballyPositioned { offset = it.positionInRoot() - state.origin }
        .drawBehind {
            blurLayer.renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
            blurLayer.record {
                translate(-offset.x, -offset.y) { drawLayer(state.layer) }
            }
            drawLayer(blurLayer)
        }
}
