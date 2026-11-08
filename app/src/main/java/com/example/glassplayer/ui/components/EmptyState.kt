package com.example.glassplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.ui.theme.Frost

/** Mensaje centrado para listas vacias, con una accion opcional debajo. */
@Composable
fun EmptyHint(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            color = Frost.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text,
            color = Frost.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (action != null) {
            Box(Modifier.padding(top = 18.dp)) { action() }
        }
    }
}
