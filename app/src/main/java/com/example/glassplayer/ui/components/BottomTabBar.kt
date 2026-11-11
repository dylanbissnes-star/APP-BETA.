package com.example.glassplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.glassplayer.ui.theme.Frost

data class TabItem(val icon: ImageVector, val label: String)

/** Alto de la barra sin contar la zona de gestos del sistema. */
val TabBarHeight = 64.dp

/**
 * Barra de pestanas inferior (como la de la referencia): superficie oscura con esquinas superiores
 * redondeadas, icono + etiqueta por pestana; la activa se tine con el acento.
 */
@Composable
fun BottomTabBar(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Frost.Surface)
            .border(1.dp, Frost.Outline, shape)
            .navigationBarsPadding()
            .height(TabBarHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            TabButton(item, selected = index == selectedIndex, onClick = { onSelect(index) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabButton(item: TabItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val tint by animateColorAsState(
        if (selected) Frost.Accent else Frost.TextTertiary, tween(220), label = "tabTint",
    )
    val chip by animateColorAsState(
        if (selected) Frost.SurfaceHigh else Frost.Surface, tween(220), label = "tabChip",
    )
    Column(
        modifier
            .pressClick(onClick, pressedScale = 0.92f)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(chip)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, item.label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Text(
            item.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
