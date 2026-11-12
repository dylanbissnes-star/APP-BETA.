package com.example.glassplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.ui.theme.Frost

/**
 * Titulo de seccion como en la referencia: una etiqueta pequena arriba ("Top"), el titulo grande
 * debajo ("Tendencias") y un enlace "Ver todo" a la derecha (opcional).
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
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(small, color = Frost.TextSecondary, fontSize = 13.sp)
            Text(big, color = Frost.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
        }
        if (seeAll != null) {
            Text(
                "Ver todo",
                color = Frost.TextPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .pressClick(seeAll, pressedScale = 0.92f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            )
        }
    }
}
