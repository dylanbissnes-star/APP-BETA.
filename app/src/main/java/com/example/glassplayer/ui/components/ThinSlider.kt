package com.example.glassplayer.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.example.glassplayer.ui.theme.Frost

/**
 * Deslizador fino y monocromatico (blanco sobre gris).
 * Toca para saltar o arrastra. Con `live = true` avisa mientras arrastras (volumen);
 * con `live = false` avisa solo al soltar (progreso de la cancion).
 */
@Composable
fun ThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    live: Boolean = false,
    onDragging: (Boolean) -> Unit = {},
    fill: Color = Frost.content(0.92f),
    track: Color = Frost.content(0.30f),
) {
    val latestChange by rememberUpdatedState(onValueChange)
    val latestDragging by rememberUpdatedState(onDragging)
    var drag by remember { mutableStateOf<Float?>(null) }
    val shown = (drag ?: value).coerceIn(0f, 1f)
    val thumb by animateDpAsState(if (drag != null) 16.dp else 12.dp, tween(160), label = "thumb")

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset -> latestChange((offset.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        latestDragging(true)
                        val f = (offset.x / size.width).coerceIn(0f, 1f)
                        drag = f
                        if (live) latestChange(f)
                    },
                    onDragEnd = {
                        drag?.let { if (!live) latestChange(it) }
                        drag = null
                        latestDragging(false)
                    },
                    onDragCancel = {
                        drag = null
                        latestDragging(false)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val f = (change.position.x / size.width).coerceIn(0f, 1f)
                        drag = f
                        if (live) latestChange(f)
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(track)
        )
        Box(
            Modifier
                .fillMaxWidth(shown)
                .height(3.dp)
                .clip(CircleShape)
                .background(fill)
        )
        Box(
            Modifier
                .offset(x = maxWidth * shown - thumb / 2)
                .size(thumb)
                .clip(CircleShape)
                .background(Frost.TextPrimary)
        )
    }
}

/** Volumen del sistema (flujo de musica) con icono y deslizador fino; se sincroniza con los botones fisicos. */
@Composable
fun VolumeControl(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val max = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var fraction by remember { mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(dragging) {
        while (!dragging) {
            fraction = audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
            delay(400)
        }
    }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (fraction <= 0f) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = "Volumen",
            tint = Frost.content(0.7f),
            modifier = Modifier.size(20.dp),
        )
        ThinSlider(
            value = fraction,
            onValueChange = { f ->
                fraction = f
                runCatching {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, (f * max).roundToInt(), 0)
                }
            },
            live = true,
            onDragging = { dragging = it },
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        )
    }
}
