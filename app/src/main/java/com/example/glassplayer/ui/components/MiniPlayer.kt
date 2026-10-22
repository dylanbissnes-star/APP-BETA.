package com.example.glassplayer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/**
 * Mini reproductor flotante (pastilla de cristal con portada, titulo, pausa y siguiente).
 * Aparece en las pantallas que no son el reproductor; al tocarlo se abre el reproductor.
 */
@Composable
fun MiniPlayer(
    track: Track,
    playing: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
) {
    val shape = RoundedCornerShape(30.dp)
    Row(
        modifier
            .fillMaxWidth()
            .pressClick(onOpen, pressedScale = 0.985f)
            .shadow(18.dp, shape, ambientColor = Frost.Tint, spotColor = Frost.Tint)
            .glass(shape, strength = 1.5f, darkness = 0.16f, backdrop = backdrop)
            .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(track.artUri, 44.dp, corner = 14.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Frost.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PlainIconButton(
            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            if (playing) "Pausar" else "Reproducir",
            onToggle, size = 44.dp, iconSize = 28.dp,
        )
        PlainIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 44.dp, iconSize = 26.dp)
    }
}
