package com.example.glassplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Boton circular flotante: vidrio negro con borde degradado.
 *  - se encoge al presionar y rebota (resorte)
 *  - el icono cambia con fundido suave
 *  - `active` enciende un halo de luz; `pulse` lo hace "respirar" (p. ej. mientras suena)
 */
@Composable
fun BlackGlassButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = size * 0.5f,
    tint: Color = Color.White,
    badge: Int = 0,
    active: Boolean = false,
    pulse: Boolean = false,
) {
    val glow by animateFloatAsState(if (active) 1f else 0f, tween(350), label = "glow")
    val iconTint by animateColorAsState(tint, tween(250), label = "tint")
    val breathing = if (pulse) {
        val t = rememberInfiniteTransition(label = "pulse")
        val v by t.animateFloat(
            0.08f, 0.30f,
            infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulseAlpha",
        )
        v
    } else 0f

    Box(
        modifier
            .size(size)
            .drawBehind {
                val a = maxOf(glow * 0.22f, breathing)
                if (a > 0.005f) {
                    val r = this.size.minDimension * 0.95f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = a), Color.Transparent),
                            center = center,
                            radius = r,
                        ),
                        radius = r,
                        center = center,
                    )
                }
            }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .pressClick(onClick)
                .shadow(12.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
                .blackGlass(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = icon, animationSpec = tween(220), label = "icon") { current ->
                Icon(current, description, tint = iconTint, modifier = Modifier.size(iconSize))
            }
        }
        if (badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badge > 99) "99+" else badge.toString(),
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Boton alargado (texto + icono opcional) con el mismo estilo. */
@Composable
fun BlackGlassPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .pressClick(onClick, pressedScale = 0.94f)
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .blackGlass(shape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Box(Modifier.size(8.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

/** Insignia con borde fino (como el "3" de la referencia). */
@Composable
fun OutlineBadge(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .glass(shape, strength = 0.7f)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}
