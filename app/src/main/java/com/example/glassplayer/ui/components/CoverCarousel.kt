package com.example.glassplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.glassplayer.data.Track
import kotlin.math.abs

private val CardWidth = 230.dp

/**
 * Carrusel 3D: la portada central se ve de frente y las vecinas
 * giran hacia atras y se achican, como en la referencia.
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
                    rotationY = (offset * -38f).coerceIn(-55f, 55f)
                    val s = 1f - 0.16f * dist
                    scaleX = s
                    scaleY = s
                    // Pega las vecinas a la central para que se solapen
                    translationX = offset * cardWidthPx * 0.30f
                    alpha = 1f - 0.28f * dist
                }
        )
    }
}

@Composable
private fun CoverCard(track: Track, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = modifier
            .width(CardWidth)
            .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .glass(shape)
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF3A3A4A), Color(0xFF15151C)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.MusicNote, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.height(56.dp))
            if (track.artUri != null) {
                AsyncImage(
                    model = track.artUri,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
