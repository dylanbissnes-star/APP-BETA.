package com.example.glassplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassplayer.data.LyricLine
import com.example.glassplayer.data.Lyrics
import com.example.glassplayer.data.LyricsState
import com.example.glassplayer.ui.theme.Frost

/**
 * Letra de la cancion, en una tarjeta del tamano de la portada.
 *  - Sincronizada: la linea actual se resalta y la lista se desplaza sola; toca una linea para saltar a ella.
 *  - Sin tiempos: texto plano que se desplaza con el dedo.
 */
@Composable
fun LyricsPanel(
    state: LyricsState,
    positionMs: Long,
    onSeekTo: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier
            .card(shape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is LyricsState.Idle, is LyricsState.Loading -> Message("Buscando la letra…") {
                CircularProgressIndicator(color = Frost.Accent, modifier = Modifier.padding(bottom = 14.dp))
            }
            is LyricsState.Missing -> Message("No encontré la letra de esta canción.") {
                ActionPill("Reintentar", onRetry)
            }
            is LyricsState.Failed -> Message("No pude descargar la letra. Revisa tu conexión.") {
                ActionPill("Reintentar", onRetry)
            }
            is LyricsState.Ready -> LyricsBody(state.lyrics, positionMs, onSeekTo)
        }
    }
}

@Composable
private fun Message(text: String, extra: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        extra()
        Text(
            text,
            color = Frost.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 14.dp),
        )
    }
}

@Composable
private fun LyricsBody(lyrics: Lyrics, positionMs: Long, onSeekTo: (Long) -> Unit) {
    when {
        lyrics.instrumental -> Message("Instrumental ♪") {}
        lyrics.hasSynced -> SyncedLyrics(lyrics.synced, positionMs, onSeekTo)
        else -> Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 22.dp),
        ) {
            Text(
                lyrics.plain,
                color = Frost.TextPrimary,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SyncedLyrics(lines: List<LyricLine>, positionMs: Long, onSeekTo: (Long) -> Unit) {
    val listState = rememberLazyListState()
    // Un poco de adelanto para que la linea se resalte justo cuando se canta
    val current by remember(lines) {
        derivedStateOf { lines.indexOfLast { it.timeMs <= positionMs + 200 } }
    }

    LaunchedEffect(current) {
        if (current >= 0) {
            val viewport = listState.layoutInfo.viewportSize.height
            listState.animateScrollToItem(current, scrollOffset = -(viewport * 0.36f).toInt())
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 90.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(lines) { i, line ->
            val isCurrent = i == current
            val color by animateColorAsState(
                when {
                    isCurrent -> Frost.TextPrimary
                    i < current -> Frost.TextTertiary
                    else -> Frost.TextSecondary
                },
                tween(250), label = "lyricColor",
            )
            Text(
                line.text.ifBlank { "♪" },
                color = color,
                fontSize = if (isCurrent) 21.sp else 18.sp,
                lineHeight = 27.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressClick({ onSeekTo(line.timeMs) }, pressedScale = 0.98f)
                    .padding(vertical = 4.dp),
            )
        }
    }
}
