package com.example.glassplayer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glassplayer.PlayerViewModel
import com.example.glassplayer.ui.components.AuroraBackground
import com.example.glassplayer.ui.components.CoverCarousel
import com.example.glassplayer.ui.components.GlassControlBar
import com.example.glassplayer.ui.components.rememberCoverPalette

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val palette = rememberCoverPalette(state.current?.artUri)

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(palette)

        if (state.loading || state.tracks.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
            return@Box
        }

        val pagerState = rememberPagerState(initialPage = state.currentIndex) { state.tracks.size }

        // Carrusel -> reproductor (el usuario desliza y cambia la cancion)
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                if (page != state.currentIndex) vm.playIndex(page)
            }
        }
        // Reproductor -> carrusel (siguiente/anterior o fin de la cancion)
        LaunchedEffect(state.currentIndex) {
            if (pagerState.currentPage != state.currentIndex) {
                pagerState.animateScrollToPage(state.currentIndex)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "GlassPlayer",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 28.dp),
            )

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                CoverCarousel(state.tracks, pagerState)
                Spacer(Modifier.height(28.dp))
                GlassControlBar(
                    state = state,
                    onPrevious = vm::previous,
                    onTogglePlay = vm::togglePlay,
                    onNext = vm::next,
                    onSeek = vm::seekToFraction,
                    onShuffle = vm::toggleShuffle,
                    onRepeat = vm::cycleRepeat,
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                )
            }
        }
    }
}
