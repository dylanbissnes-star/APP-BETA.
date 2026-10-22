package com.example.glassplayer.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.glassplayer.data.Track
import kotlin.math.abs

private val CardWidth = 226.dp

/**
 * Carrusel con profundidad 3D: la portada activa va al centro, nitida y a tamano completo;
 * las vecinas quedan parcialmente detras, mas pequenas, mas tenues y desenfocadas (Android 12+).
 * Las portadas conservan sus colores; todo lo demas es monocromatico.
 */
@Composable
fun CoverCarousel(
    tracks: List<Track>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidePadding = ((screenWidth - CardWidth) / 2).coerceAtLeast(0.dp)
    val cardWidthPx = with(LocalDensity.current) { CardWidth.toPx() }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        pageSize = PageSize.Fixed(CardWidth),
        contentPadding = PaddingValues(horizontal = sidePadding),
        verticalAlignment = Alignment.CenterVertically,
        beyondViewportPageCount = 2,
    ) { page ->
        val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val dist = abs(offset).coerceAtMost(2f)

        CoverCard(
            track = tracks[page],
            modifier = Modifier
                .zIndex(-dist)
                .graphicsLayer {
                    cameraDistance = 14f * density
                    rotationY = (offset * -34f).coerceIn(-52f, 52f)
                    val s = 1f - 0.18f * dist
                    scaleX = s
                    scaleY = s
                    // Las vecinas se solapan con la central
                    translationX = offset * cardWidthPx * 0.30f
                    alpha = 1f - 0.34f * dist
                    // Desenfoque progresivo de las portadas laterales (Android 12+)
                    if (Build.VERSION.SDK_INT >= 31) {
                        val r = dist * 5f * density
                        renderEffect = if (r > 0.5f) BlurEffect(r, r) else null
                    }
                }
        )
    }
}

/** Tarjeta de cristal que enmarca solo la portada; el texto va debajo del carrusel. */
@Composable
private fun CoverCard(track: Track, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = modifier
            .width(CardWidth)
            .shadow(26.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .glass(shape, darkness = 0.35f)
            .padding(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(25.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF0C0C0C)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.MusicNote, null,
                tint = Color.White.copy(alpha = 0.40f),
                modifier = Modifier.size(56.dp),
            )
            if (track.artUri != null) {
                AsyncImage(
                    model = track.artUri,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
