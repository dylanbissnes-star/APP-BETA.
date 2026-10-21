package com.example.glassplayer.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.PlayerViewModel
import com.example.glassplayer.ui.components.AuroraBackground
import com.example.glassplayer.ui.components.BlackGlassButton
import com.example.glassplayer.ui.components.CoverCarousel
import com.example.glassplayer.ui.components.GlassControlBar
import com.example.glassplayer.ui.components.blackGlass
import com.example.glassplayer.ui.components.rememberCoverPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop

private enum class Screen { Home, Library, Queue }

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val palette = rememberCoverPalette(state.current?.artUri)
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    // Selector de carpetas del sistema (Storage Access Framework)
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.addFolder(uri)
    }

    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(palette)

        AnimatedVisibility(
            visible = screen == Screen.Home,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            HomeContent(
                state = state,
                vm = vm,
                onOpenLibrary = { screen = Screen.Library },
                onOpenQueue = { screen = Screen.Queue },
            )
        }

        AnimatedVisibility(
            visible = screen == Screen.Library,
            enter = slideInHorizontally { -it / 3 } + fadeIn(),
            exit = slideOutHorizontally { -it / 3 } + fadeOut(),
        ) {
            LibraryScreen(
                state = state,
                onBack = { screen = Screen.Home },
                onAddFolder = { folderPicker.launch(null) },
                onRemoveFolder = vm::removeFolder,
                onSelectSource = vm::selectSource,
                onPlay = { list, index ->
                    vm.playFromList(list, index)
                    screen = Screen.Home
                },
                onQueue = vm::addToQueue,
            )
        }

        AnimatedVisibility(
            visible = screen == Screen.Queue,
            enter = slideInHorizontally { it / 3 } + fadeIn(),
            exit = slideOutHorizontally { it / 3 } + fadeOut(),
        ) {
            QueueScreen(
                state = state,
                onBack = { screen = Screen.Home },
                onJump = { index ->
                    vm.jumpTo(index)
                    screen = Screen.Home
                },
                onRemove = vm::removeFromQueue,
                onClearQueued = vm::clearQueued,
            )
        }

        // Aviso breve (p. ej. "añadida a la cola")
        state.message?.let { msg ->
            LaunchedEffect(msg) {
                delay(1800)
                vm.clearMessage()
            }
            val pill = RoundedCornerShape(50)
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
                    .shadow(12.dp, pill, ambientColor = Color.Black, spotColor = Color.Black)
                    .blackGlass(pill)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: PlayerUiState,
    vm: PlayerViewModel,
    onOpenLibrary: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        // Botones flotantes superiores
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BlackGlassButton(Icons.Rounded.LibraryMusic, "Biblioteca", onOpenLibrary)
            Text(
                "GlassPlayer",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.55f),
            )
            BlackGlassButton(
                Icons.AutoMirrored.Rounded.QueueMusic, "Cola de reproducción", onOpenQueue,
                badge = state.queuedCount,
            )
        }

        if (state.tracks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Column
        }

        val pagerState = rememberPagerState(initialPage = state.currentIndex) { state.tracks.size }

        // Carrusel -> reproductor (el usuario desliza y cambia la cancion).
        // drop(1): ignora el valor inicial para no pisar la cancion que esta sonando.
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.drop(1).collect { page ->
                vm.playIndex(page)
            }
        }
        // Reproductor -> carrusel (siguiente/anterior, fin de cancion, saltos desde la cola)
        LaunchedEffect(state.currentIndex) {
            if (pagerState.currentPage != state.currentIndex) {
                pagerState.animateScrollToPage(state.currentIndex)
            }
        }

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
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
