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
import com.example.glassplayer.ui.theme.Frost
import kotlin.math.abs

private val CardWidth = 236.dp

/**
 * Carrusel con profundidad 3D: la portada activa va al centro, nitida y a tamano completo;
 * las vecinas quedan parcialmente detras, mas pequenas, mas tenues y desenfocadas (Android 12+).
 * Marco de cristal claro; las portadas conservan sus colores.
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
    // Marco de cristal claro alrededor de la portada (como la portada de la referencia)
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .width(CardWidth)
            .shadow(22.dp, shape, ambientColor = Frost.Tint, spotColor = Frost.Tint)
            .glass(shape, strength = 1.9f, darkness = 0.04f)
            .padding(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(19.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFB69CFF), Color(0xFF6C7BFF)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.MusicNote, null,
                tint = Color.White.copy(alpha = 0.80f),
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
