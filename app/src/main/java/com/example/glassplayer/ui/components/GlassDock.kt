package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DockItem(val icon: ImageVector, val label: String)

/**
 * Dock flotante de navegacion (adaptacion movil de la barra lateral de la referencia):
 * capsula de vidrio negro; el elemento activo se expande en una "pildora" con icono + texto,
 * borde degradado y un halo de luz. Los cambios se animan con resorte.
 */
@Composable
fun GlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<Int, Int> = emptyMap(),
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .blackGlass(shape)
            .padding(6.dp)
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            DockButton(
                item = item,
                selected = index == selectedIndex,
                badge = badges[index] ?: 0,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun DockButton(item: DockItem, selected: Boolean, badge: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val sel by animateFloatAsState(if (selected) 1f else 0f, tween(320), label = "dockSel")
    val tint by animateColorAsState(
        if (selected) Color.White else Color.White.copy(alpha = 0.55f), tween(250), label = "dockTint",
    )

    Box(
        Modifier
            .height(48.dp)
            .pressClick(onClick, pressedScale = 0.94f)
            .clip(shape)
            .drawBehind {
                if (sel > 0.01f) {
                    // Relleno suave
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f))),
                        cornerRadius = CornerRadius(size.height / 2f),
                        alpha = sel,
                    )
                    // Halo de luz inferior
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width / 2f, size.height),
                            radius = size.width * 0.7f,
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width / 2f, size.height),
                        alpha = sel,
                    )
                    // Borde con degradado
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.45f)),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        ),
                        cornerRadius = CornerRadius(size.height / 2f),
                        style = Stroke(1.2.dp.toPx()),
                        alpha = sel,
                    )
                }
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, item.label, tint = tint, modifier = Modifier.size(24.dp))
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(250)) + expandHorizontally(),
                exit = fadeOut(tween(120)) + shrinkHorizontally(),
            ) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 0.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badge > 9) "9+" else badge.toString(),
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
