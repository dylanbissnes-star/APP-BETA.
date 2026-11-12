package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.theme.Frost

/**
 * Mini reproductor: pildora oscura con degradado hacia el color del tema, portada, titulo, linea
 * de progreso, corazon y pausa (como el de la referencia). Al tocarlo se abre el reproductor.
 */
@Composable
fun MiniPlayer(
    track: Track,
    playing: Boolean,
    progress: Float,
    favorite: Boolean,
    onToggle: () -> Unit,
    onFavorite: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(30.dp)
    // Siempre oscuro, aunque la app este en modo claro
    val start = Frost.Inverse.let { if (Frost.OnDark) it else Color(0xFF1E1524) }
    val end = Frost.AccentFill.let { if (Frost.OnDark) Frost.Floating else it }
    Row(
        modifier
            .fillMaxWidth()
            .pressClick(onOpen, pressedScale = 0.985f)
            .shadow(14.dp, shape, ambientColor = Frost.Tint.copy(alpha = 0.3f), spotColor = Frost.Tint.copy(alpha = 0.3f))
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(start, start, end.copy(alpha = 0.92f))))
            .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverThumb(track.artUri, 46.dp, corner = 23.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.66f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Linea de progreso bajo el texto
            Box(
                Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }
        }
        PlainIconButton(
            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            if (favorite) "Quitar de favoritos" else "Añadir a favoritos",
            onFavorite, size = 42.dp, iconSize = 22.dp,
            tint = Color.White,
        )
        PlainIconButton(
            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            if (playing) "Pausar" else "Reproducir",
            onToggle, size = 44.dp, iconSize = 30.dp,
            tint = Color.White,
        )
    }
}
