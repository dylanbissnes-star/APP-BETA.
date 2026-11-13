package com.example.glassplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.ui.theme.Frost

/**
 * Boton redondo de cristal con borde fino iluminado en una esquina.
 * `active` lo tine con el acento; `badge` muestra un contador.
 */
@Composable
fun RoundButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = size * 0.5f,
    tint: Color = Frost.TextPrimary,
    badge: Int = 0,
    active: Boolean = false,
) {
    val accent = Frost.Accent
    val fill by animateColorAsState(if (active) accent.copy(alpha = 0.20f) else Frost.SurfaceHigh, tween(250), label = "rbFill")
    val edge by animateColorAsState(if (active) accent.copy(alpha = 0.60f) else Frost.Outline, tween(250), label = "rbEdge")
    val iconTint by animateColorAsState(if (active) accent else tint, tween(250), label = "rbTint")

    Box(modifier.size(size)) {
        Box(
            Modifier
                .matchParentSize()
                .pressClick(onClick)
                .clip(CircleShape)
                .background(fill)
                .border(1.dp, if (active) SolidColor(edge) else Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = icon, animationSpec = tween(220), label = "rbIcon") { current ->
                Icon(current, description, tint = iconTint, modifier = Modifier.size(iconSize))
            }
        }
        if (badge > 0) {
            CountBadge(badge, Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
        }
    }
}

/** Icono sin fondo: solo el simbolo, con rebote al tocar. */
@Composable
fun PlainIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    tint: Color = Frost.TextPrimary,
) {
    val iconTint by animateColorAsState(tint, tween(250), label = "plainTint")
    Box(
        modifier
            .size(size)
            .pressClick(onClick, pressedScale = 0.86f)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = icon, animationSpec = tween(200), label = "plainIcon") { current ->
            Icon(current, description, tint = iconTint, modifier = Modifier.size(iconSize))
        }
    }
}

/** Circulito con un numero (cola, avisos). */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Frost.AccentFill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            color = Frost.OnAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Boton alargado. `accent` = relleno con el color de acento elegido (como "Shuffle" de la
 * referencia amarilla); `primary` = relleno con el color del texto; si no, relleno elevado con contorno.
 * `iconTint` tine el icono (p. ej. el corazon cuando ya es favorito).
 */
@Composable
fun ActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = false,
    accent: Boolean = false,
    iconTint: Color? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val filled = primary || accent
    val fg = if (filled) Frost.OnAccent else Frost.TextPrimary
    val fill = if (filled) Frost.AccentFill else Frost.SurfaceHigh
    Row(
        modifier
            .pressClick(onClick, pressedScale = 0.95f)
            .clip(shape)
            .background(fill)
            .then(if (filled) Modifier else Modifier.border(1.dp, Brush.linearGradient(listOf(Frost.EdgeLight, Frost.EdgeDark)), shape))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = iconTint ?: fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Insignia con borde fino. */
@Composable
fun OutlineBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .card(RoundedCornerShape(50), raised = true)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 11.sp, color = Frost.TextSecondary)
    }
}
