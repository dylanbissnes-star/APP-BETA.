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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.glassplayer.ui.theme.Frost

data class TabItem(val icon: ImageVector, val label: String)

/** Alto de la barra sin contar la zona de gestos del sistema. */
val TabBarHeight = 76.dp

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
    val shape = RoundedCornerShape(34.dp)
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
            .shadow(16.dp, shape, ambientColor = Frost.Tint.copy(alpha = 0.25f), spotColor = Frost.Tint.copy(alpha = 0.25f))
            .clip(shape)
            .background(Frost.Floating)
            .border(1.dp, Frost.Outline, shape)
            .height(TabBarHeight - 10.dp)
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
    // La pestana activa va en una pildora rellena (blanca en oscuro, con el acento en claro)
    val tint by animateColorAsState(
        if (selected) Frost.OnAccent else Frost.TextSecondary, tween(220), label = "tabTint",
    )
    val chip by animateColorAsState(
        if (selected) Frost.AccentFill else Color.Transparent, tween(220), label = "tabChip",
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
            color = if (selected) Frost.TextPrimary else Frost.TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
