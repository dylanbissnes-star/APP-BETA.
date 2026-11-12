package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost
import java.util.Locale

/**
 * Mini reproductor (como el de la referencia neon): tarjeta oscura con una flecha para abrir el
 * reproductor, portada, titulo en mayusculas, "por artista" y los controles: repetir, anterior,
 * play/pausa (circulo neon), siguiente y aleatorio. Una linea fina de progreso abajo.
 */
@Composable
fun MiniPlayer(
    track: Track,
    playing: Boolean,
    progress: Float,
    shuffle: Boolean,
    repeatMode: Int,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val accent = Frost.Accent
    Column(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Frost.Tint.copy(alpha = 0.35f), spotColor = Frost.Tint.copy(alpha = 0.35f))
            .clip(shape)
            .background(Frost.Floating)
            .border(1.dp, Frost.Outline, shape),
    ) {
        // Flecha para abrir el reproductor
        Box(
            Modifier
                .fillMaxWidth()
                .pressClick(onOpen, pressedScale = 0.99f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.KeyboardArrowUp, "Abrir reproductor", tint = Frost.TextSecondary, modifier = Modifier.size(20.dp))
        }
        Row(
            Modifier.padding(start = 10.dp, end = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .pressClick(onOpen, pressedScale = 0.99f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverThumb(track.artUri, 40.dp, corner = 8.dp)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(
                        track.title.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Frost.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "por ${track.artist}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 11.sp,
                        color = Frost.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            PlainIconButton(
                if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                "Repetir", onRepeat, size = 30.dp, iconSize = 17.dp,
                tint = if (repeatMode != Player.REPEAT_MODE_OFF) accent else Frost.TextSecondary,
            )
            PlainIconButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious, size = 32.dp, iconSize = 22.dp, tint = Frost.TextPrimary)
            Box(
                Modifier
                    .size(38.dp)
                    .pressClick(onToggle, pressedScale = 0.9f)
                    .clip(CircleShape)
                    .background(Frost.AccentFill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (playing) "Pausar" else "Reproducir",
                    tint = Frost.OnAccent,
                    modifier = Modifier.size(24.dp),
                )
            }
            PlainIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 32.dp, iconSize = 22.dp, tint = Frost.TextPrimary)
            PlainIconButton(
                Icons.Rounded.Shuffle, "Aleatorio", onShuffle, size = 30.dp, iconSize = 17.dp,
                tint = if (shuffle) accent else Frost.TextSecondary,
            )
        }
        // Linea de progreso
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Frost.Outline)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Frost.AccentFill)
            )
        }
    }
}
