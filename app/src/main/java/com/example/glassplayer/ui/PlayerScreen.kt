package com.example.glassplayer.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.PlayerViewModel
import com.example.glassplayer.R
import com.example.glassplayer.ui.components.AuroraBackground
import com.example.glassplayer.ui.components.CoverCarousel
import com.example.glassplayer.ui.components.DockItem
import com.example.glassplayer.ui.components.GlassControlBar
import com.example.glassplayer.ui.components.GlassDock
import com.example.glassplayer.ui.components.ScreenHeader
import com.example.glassplayer.ui.components.blackGlass
import com.example.glassplayer.ui.components.rememberCoverPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop

private enum class Screen { Home, Library, Queue }

private val dockItems = listOf(
    DockItem(Icons.Rounded.Album, "Reproductor"),
    DockItem(Icons.Rounded.LibraryMusic, "Biblioteca"),
    DockItem(Icons.AutoMirrored.Rounded.QueueMusic, "Cola"),
)

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val art = state.current?.artUri
    val palette = rememberCoverPalette(art)
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    // Selector de carpetas del sistema (Storage Access Framework)
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.addFolder(uri)
    }

    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    Box(Modifier.fillMaxSize()) {
        // Fondo negro con luz ambiental que cambia con la portada
        AuroraBackground(colors = palette, art = art)

        // Marca de agua: el logo, grande y muy tenue, detras de todo
        Image(
            painter = painterResource(R.drawable.logo_mark),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .alpha(0.07f),
        )

        // Las tres pantallas comparten fondo, cabecera y dock; cambian con un deslizamiento suave
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { (it / 5) * dir } +
                    fadeIn(tween(300))) togetherWith
                    (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -(it / 5) * dir } +
                        fadeOut(tween(200)))
            },
            label = "screens",
        ) { target ->
            when (target) {
                Screen.Home -> HomeContent(state = state, vm = vm)
                Screen.Library -> LibraryScreen(
                    state = state,
                    onAddFolder = { folderPicker.launch(null) },
                    onRemoveFolder = vm::removeFolder,
                    onSelectSource = vm::selectSource,
                    onPlay = { list, index ->
                        vm.playFromList(list, index)
                        screen = Screen.Home
                    },
                    onQueue = vm::addToQueue,
                )
                Screen.Queue -> QueueScreen(
                    state = state,
                    onJump = { index ->
                        vm.jumpTo(index)
                        screen = Screen.Home
                    },
                    onRemove = vm::removeFromQueue,
                    onClearQueued = vm::clearQueued,
                )
            }
        }

        // Dock flotante de navegacion
        GlassDock(
            items = dockItems,
            selectedIndex = screen.ordinal,
            onSelect = { screen = Screen.entries[it] },
            badges = mapOf(Screen.Queue.ordinal to state.queuedCount),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )

        // Aviso breve (p. ej. "añadida a la cola"), por encima del dock
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
                    .padding(start = 24.dp, end = 24.dp, bottom = 92.dp)
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
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 92.dp), // espacio para el dock
    ) {
        ScreenHeader(
            title = "Reproduciendo",
            subtitle = "${state.tracks.size} canciones en la lista",
        )

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

        // Centrado; si la pantalla es baja, el contenido se puede desplazar en vez de aplastarse
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
            ) {
                CoverCarousel(state.tracks, pagerState)
                Spacer(Modifier.height(20.dp))
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
}
