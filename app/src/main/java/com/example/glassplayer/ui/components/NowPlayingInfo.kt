package com.example.glassplayer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/**
 * Titulo grande y artista centrados (como la referencia). Cambia con un fundido y subida suave
 * al cambiar de cancion.
 */
@Composable
fun NowPlayingInfo(
    track: Track?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = track,
        contentKey = { it?.id },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        transitionSpec = {
            (fadeIn(tween(340)) + slideInVertically(tween(340)) { it / 5 }) togetherWith fadeOut(tween(160))
        },
        label = "nowPlayingInfo",
    ) { t ->
        Column(
            Modifier.fillMaxWidth().heightIn(min = 56.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                t?.title ?: "—",
                color = Frost.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(t?.artist.orEmpty(), t?.album.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
                color = Frost.TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
