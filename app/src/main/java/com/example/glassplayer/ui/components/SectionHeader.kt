package com.example.glassplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.ui.theme.Frost

/**
 * Titulo de seccion de una linea ("Descubre tus artistas favoritos") con una flecha a la derecha
 * para ver todo (opcional). `small` se conserva por compatibilidad y ya no se muestra.
 */
@Composable
fun SectionHeader(
    small: String,
    big: String,
    modifier: Modifier = Modifier,
    seeAll: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(big, color = Frost.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (seeAll != null) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward, "Ver todo",
                tint = Frost.TextPrimary,
                modifier = Modifier
                    .size(34.dp)
                    .pressClick(seeAll, pressedScale = 0.9f)
                    .padding(7.dp),
            )
        }
    }
}
