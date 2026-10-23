package com.example.glassplayer.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glassplayer.PlayerUiState
import com.example.glassplayer.PlayerViewModel
import com.example.glassplayer.R
import com.example.glassplayer.ui.components.CoverCarousel
import com.example.glassplayer.ui.components.DarkModeToggle
import com.example.glassplayer.ui.components.DockItem
import com.example.glassplayer.ui.components.GlassDock
import com.example.glassplayer.ui.components.MiniPlayer
import com.example.glassplayer.ui.components.NowPlayingInfo
import com.example.glassplayer.ui.components.PageBackground
import com.example.glassplayer.ui.components.PlainIconButton
import com.example.glassplayer.ui.components.PlayerControls
import com.example.glassplayer.ui.components.backdropSource
import com.example.glassplayer.ui.components.glass
import com.example.glassplayer.ui.components.rememberBackdropState
import com.example.glassplayer.ui.components.rememberCoverPalette
import com.example.glassplayer.ui.theme.DarkFrost
import com.example.glassplayer.ui.theme.Frost
import com.example.glassplayer.ui.theme.LightFrost
import com.example.glassplayer.ui.theme.LocalFrost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop

/** El orden de las 4 primeras es el del dock. `Player` no esta en el dock: se abre desde el mini reproductor. */
private enum class Screen { Home, Search, Library, Queue, Player }

private val dockItems = listOf(
    DockItem(Icons.Rounded.Home, "Inicio"),
    DockItem(Icons.Rounded.Search, "Buscar"),
    DockItem(Icons.Rounded.LibraryMusic, "Biblioteca"),
    DockItem(Icons.AutoMirrored.Rounded.QueueMusic, "Cola"),
)

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val art = state.current?.artUri
    val palette = rememberCoverPalette(art)
    val accent = remember(palette) { lerp(palette[2], Color(0xFFD81B4A), 0.35f) }
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    // Modo claro / oscuro (se recuerda entre sesiones)
    val prefs = remember { context.getSharedPreferences("glass_prefs", Context.MODE_PRIVATE) }
    var dark by rememberSaveable { mutableStateOf(prefs.getBoolean("dark", false)) }

    // La portada pasa a ser el fondo cuando ya se eligio una cancion (empezo a sonar)
    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.isPlaying) { if (state.isPlaying) started = true }
    val showArt = started && art != null

    // Sobre la portada (con velo oscuro) o en modo oscuro, el contenido es claro
    val onDark = dark || showArt

    // Iconos de la barra de estado/navegacion legibles segun el fondo
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !onDark
        controller.isAppearanceLightNavigationBars = !onDark
    }

    // El dock y el mini reproductor desenfocan todo lo que hay detras
    val backdrop = rememberBackdropState()

    // Selector de carpetas del sistema (Storage Access Framework)
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.addFolder(uri)
    }

    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    CompositionLocalProvider(LocalFrost provides if (onDark) DarkFrost else LightFrost) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().backdropSource(backdrop)) {
                PageBackground(dark = dark, art = art, showArt = showArt)

                Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                    TopBar(
                        dark = dark,
                        onToggleDark = {
                            dark = !dark
                            prefs.edit().putBoolean("dark", dark).apply()
                        },
                    )

                    AnimatedContent(
                        targetState = screen,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            Screen.Home -> HomeMenu(
                                state = state,
                                onOpenFolder = { entry ->
                                    vm.selectSource(entry.key)
                                    screen = Screen.Library
                                },
                                onPlayFolder = { entry ->
                                    vm.playFromList(entry.tracks, 0)
                                    screen = Screen.Player
                                },
                                onAddFolder = { folderPicker.launch(null) },
                            )
                            Screen.Search -> SearchScreen(
                                state = state,
                                onPlay = { list, index ->
                                    vm.playFromList(list, index)
                                    screen = Screen.Player
                                },
                            )
                            Screen.Library -> LibraryScreen(
                                state = state,
                                onAddFolder = { folderPicker.launch(null) },
                                onRemoveFolder = vm::removeFolder,
                                onSelectSource = vm::selectSource,
                                onPlay = { list, index ->
                                    vm.playFromList(list, index)
                                    screen = Screen.Player
                                },
                                onQueue = vm::addToQueue,
                            )
                            Screen.Queue -> QueueScreen(
                                state = state,
                                onJump = { index ->
                                    vm.jumpTo(index)
                                    screen = Screen.Player
                                },
                                onRemove = vm::removeFromQueue,
                                onClearQueued = vm::clearQueued,
                            )
                            Screen.Player -> PlayerContent(
                                state = state,
                                vm = vm,
                                accent = accent,
                                onBack = { screen = Screen.Home },
                                onOpenSearch = { screen = Screen.Search },
                                onOpenQueue = { screen = Screen.Queue },
                            )
                        }
                    }
                }
            }

            // Mini reproductor: en todas las pantallas menos en el propio reproductor
            AnimatedVisibility(
                visible = screen != Screen.Player && state.current != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 26.dp, end = 26.dp, bottom = 92.dp),
                enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 2 },
            ) {
                state.current?.let { track ->
                    MiniPlayer(
                        track = track,
                        playing = state.isPlaying,
                        onToggle = vm::togglePlay,
                        onNext = vm::next,
                        onOpen = { screen = Screen.Player },
                        backdrop = backdrop,
                    )
                }
            }

            // Dock flotante de navegacion
            GlassDock(
                items = dockItems,
                selectedIndex = if (screen == Screen.Player) -1 else screen.ordinal,
                onSelect = { screen = Screen.entries[it] },
                badges = mapOf(Screen.Queue.ordinal to state.queuedCount),
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
            )

            // Aviso breve (p. ej. "añadida a la cola"), por encima del mini reproductor
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
                        .padding(start = 24.dp, end = 24.dp, bottom = 164.dp)
                        .shadow(12.dp, pill, ambientColor = Frost.Tint, spotColor = Frost.Tint)
                        .glass(pill, strength = 1.6f, darkness = 0.40f)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Frost.TextPrimary,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Barra superior fija: logo y nombre a la izquierda, interruptor claro/oscuro SIEMPRE visible a la derecha. */
@Composable
private fun TopBar(dark: Boolean, onToggleDark: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_mark),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.app_name),
            modifier = Modifier.weight(1f),
            color = Frost.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        DarkModeToggle(dark = dark, onToggle = onToggleDark)
    }
}

/** Pantalla del reproductor: carrusel de portadas, titulo, controles y pastilla de acciones. */
@Composable
private fun PlayerContent(
    state: PlayerUiState,
    vm: PlayerViewModel,
    accent: Color,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(bottom = 88.dp), // espacio para el dock
    ) {
        // Barra: volver al menu, titulo y buscar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlainIconButton(Icons.Rounded.KeyboardArrowDown, "Volver al menú", onBack, size = 48.dp, iconSize = 32.dp)
            Text(
                "Reproduciendo ahora",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Frost.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            PlainIconButton(Icons.Rounded.Search, "Buscar", onOpenSearch, size = 48.dp, iconSize = 26.dp)
        }

        if (state.tracks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Frost.TextPrimary)
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CoverCarousel(state.tracks, pagerState)
                Spacer(Modifier.height(16.dp))
                NowPlayingInfo(state.current)
                Spacer(Modifier.height(4.dp))
                PlayerControls(
                    state = state,
                    accent = accent,
                    onPrevious = vm::previous,
                    onTogglePlay = vm::togglePlay,
                    onNext = vm::next,
                    onSeek = vm::seekToFraction,
                    onShuffle = vm::toggleShuffle,
                    onRepeat = vm::cycleRepeat,
                    onOpenQueue = onOpenQueue,
                    onOpenDevices = { openAudioOutputs(context) },
                )
            }
        }
    }
}

/** Abre el selector de salida de audio del sistema (altavoz, Bluetooth, volumen). */
private fun openAudioOutputs(context: Context) {
    val panel = Intent(Settings.Panel.ACTION_VOLUME)
    val fallback = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    runCatching { context.startActivity(panel) }
        .recoverCatching { context.startActivity(fallback) }
}
