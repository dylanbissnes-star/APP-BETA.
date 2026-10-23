package com.example.glassplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Interruptor sol/luna para cambiar entre modo claro y oscuro (la bolita se desliza con resorte). */
@Composable
fun DarkModeToggle(
    dark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val knobX by animateDpAsState(
        if (dark) 36.dp else 4.dp,
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "knobX",
    )
    val track by animateColorAsState(
        if (dark) Color(0xFF34343A) else Color(0xFFE4E4E7),
        tween(300), label = "toggleTrack",
    )
    val hint = Color(0xFF9A9AA0)

    Box(
        modifier
            .size(width = 68.dp, height = 36.dp)
            .pressClick(onToggle, pressedScale = 0.95f)
            .clip(RoundedCornerShape(50))
            .background(track),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(Icons.Rounded.LightMode, null, tint = hint, modifier = Modifier.padding(start = 9.dp).size(18.dp))
        Icon(
            Icons.Rounded.DarkMode, null, tint = hint,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 9.dp).size(18.dp),
        )
        Box(
            Modifier
                .offset(x = knobX)
                .size(28.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(if (dark) Color(0xFF111114) else Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (dark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = if (dark) "Cambiar a modo claro" else "Cambiar a modo oscuro",
                tint = if (dark) Color.White else Color(0xFFF5A623),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
