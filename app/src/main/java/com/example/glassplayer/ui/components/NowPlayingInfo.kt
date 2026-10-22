package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/** Titulo, artista y album debajo de la portada central; cambia con un fundido y subida suave. */
@Composable
fun NowPlayingInfo(track: Track?, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = track,
        contentKey = { it?.id },
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            (fadeIn(tween(340)) + slideInVertically(tween(340)) { it / 5 }) togetherWith fadeOut(tween(160))
        },
        label = "nowPlayingInfo",
    ) { t ->
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                t?.title ?: "—",
                color = Frost.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                t?.artist ?: "",
                color = Frost.TextSecondary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (!t?.album.isNullOrBlank()) {
                Text(
                    t?.album.orEmpty(),
                    color = Frost.TextTertiary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
