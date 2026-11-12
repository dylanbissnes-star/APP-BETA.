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
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
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
import com.example.glassplayer.data.LyricsState
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SettingsStore
import com.example.glassplayer.data.Track
import com.example.glassplayer.ui.components.AppDrawerContent
import com.example.glassplayer.ui.components.BottomTabBar
import com.example.glassplayer.ui.components.CoverCarousel
import com.example.glassplayer.ui.components.DarkModeToggle
import com.example.glassplayer.ui.components.DrawerEntry
import com.example.glassplayer.ui.components.LyricsPanel
import com.example.glassplayer.ui.components.MiniPlayer
import com.example.glassplayer.ui.components.NowPlayingInfo
import com.example.glassplayer.ui.components.PageBackground
import com.example.glassplayer.ui.components.PlayerControls
import com.example.glassplayer.ui.components.PlainIconButton
import com.example.glassplayer.ui.components.RoundButton
import com.example.glassplayer.ui.components.TabBarHeight
import com.example.glassplayer.ui.components.TabItem
import com.example.glassplayer.ui.components.card
import com.example.glassplayer.ui.theme.Frost
import com.example.glassplayer.ui.theme.LocalFrost
import com.example.glassplayer.ui.theme.frostFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/** Las 5 primeras son las pestanas de la barra inferior (en ese orden). Cola y Reproductor no tienen pestana. */
private enum class Screen { Home, Explore, Favorites, Library, Search, Queue, Player, Settings }

/** Pagina de detalle que se abre encima de la pantalla actual. */
private sealed interface Detail {
    data class Artist(val name: String) : Detail
    data class Album(val name: String) : Detail
}

private data class View(val screen: Screen, val detail: Detail?)

private val tabs = listOf(
    TabItem(Icons.Rounded.Home, "Inicio"),
    TabItem(Icons.Rounded.Explore, "Explorar"),
    TabItem(Icons.Rounded.Favorite, "Favoritos"),
    TabItem(Icons.Rounded.LibraryMusic, "Biblioteca"),
    TabItem(Icons.Rounded.Search, "Buscar"),
)

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val art = state.current?.artUri

    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var detail by remember { mutableStateOf<Detail?>(null) }
    var exploreTab by rememberSaveable { mutableStateOf(0) } // 0 = artistas, 1 = albumes

    // Modo oscuro (por defecto) o claro; se recuerda entre sesiones
    var dark by rememberSaveable { mutableStateOf(SettingsStore.dark(context)) }
    // Color de acento elegido por la persona (Ajustes) y si el reproductor se pinta con el
    var accentArgb by rememberSaveable { mutableStateOf(SettingsStore.accent(context)) }
    var playerAccentBg by rememberSaveable { mutableStateOf(SettingsStore.playerAccentBg(context)) }
    val accentColor = Color(accentArgb)
    val frost = frostFor(dark, accentColor)
    // Con «estilo del tema» el reproductor usa el mismo fondo que la app; si no, queda oscuro sobre la portada desenfocada
    val playerFrost = if (playerAccentBg) frost else frostFor(true, accentColor)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Iconos de la barra de estado/navegacion legibles segun el fondo
    val onDark = if (screen == Screen.Player) playerFrost.onDark else frost.onDark
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !onDark
        controller.isAppearanceLightNavigationBars = !onDark
    }

    // Selector de carpetas del sistema (Storage Access Framework)
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.addFolder(uri)
    }

    fun go(target: Screen) {
        detail = null
        screen = target
    }
    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }
    // Reproducir una lista y abrir el reproductor
    val lyricsState by vm.lyrics.collectAsStateWithLifecycle()
    val playAndOpen: (List<Track>, Int) -> Unit = { list, index ->
        vm.playFromList(list, index)
        go(Screen.Player)
    }

    BackHandler(enabled = detail != null || screen != Screen.Home) {
        if (detail != null) detail = null else screen = Screen.Home
    }

    CompositionLocalProvider(LocalFrost provides frost) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = screen != Screen.Player,
            drawerContent = {
                AppDrawerContent(
                    menu = listOf(
                        DrawerEntry(Icons.Rounded.Home, "Inicio", screen == Screen.Home) { go(Screen.Home); closeDrawer() },
                        DrawerEntry(Icons.Rounded.Search, "Buscar", screen == Screen.Search) { go(Screen.Search); closeDrawer() },
                        DrawerEntry(Icons.Rounded.Favorite, "Favoritos", screen == Screen.Favorites) { go(Screen.Favorites); closeDrawer() },
                        DrawerEntry(Icons.AutoMirrored.Rounded.QueueMusic, "Cola", screen == Screen.Queue) { go(Screen.Queue); closeDrawer() },
                        DrawerEntry(Icons.Rounded.Settings, "Ajustes", screen == Screen.Settings) { go(Screen.Settings); closeDrawer() },
                    ),
                    library = listOf(
                        DrawerEntry(Icons.Rounded.Album, "Álbumes", screen == Screen.Explore && exploreTab == 1) {
                            exploreTab = 1; go(Screen.Explore); closeDrawer()
                        },
                        DrawerEntry(Icons.Rounded.MusicNote, "Canciones", screen == Screen.Library) {
                            vm.selectSource(SOURCE_ALL); go(Screen.Library); closeDrawer()
                        },
                        DrawerEntry(Icons.Rounded.Person, "Artistas", screen == Screen.Explore && exploreTab == 0) {
                            exploreTab = 0; go(Screen.Explore); closeDrawer()
                        },
                    ),
                    folders = state.folders,
                    onOpenFolder = { folder ->
                        vm.selectSource(folder.uri)
                        go(Screen.Library)
                        closeDrawer()
                    },
                    onRemoveFolder = { folder -> vm.removeFolder(folder.uri) },
                    onAddFolder = {
                        folderPicker.launch(null)
                        closeDrawer()
                    },
                )
            },
        ) {
            Box(Modifier.fillMaxSize().background(Frost.BgBrush)) {
                // En el reproductor, la portada de la cancion se ve de fondo, muy desenfocada
                AnimatedVisibility(
                    visible = screen == Screen.Player && detail == null && !playerAccentBg,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(300)),
                ) {
                    PageBackground(dark = true, art = art, showArt = art != null)
                }

                val showTopBar = screen != Screen.Player && detail == null

                Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                    if (showTopBar) {
                        TopBar(
                            dark = dark,
                            onMenu = { scope.launch { drawerState.open() } },
                            onSettings = { go(Screen.Settings) },
                            onToggleDark = {
                                dark = !dark
                                SettingsStore.saveDark(context, dark)
                            },
                        )
                    }

                    AnimatedContent(
                        targetState = View(screen, detail),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        transitionSpec = {
                            val dir = when {
                                targetState.detail != null && initialState.detail == null -> 1
                                targetState.detail == null && initialState.detail != null -> -1
                                targetState.screen.ordinal >= initialState.screen.ordinal -> 1
                                else -> -1
                            }
                            (slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { (it / 6) * dir } +
                                fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -(it / 6) * dir } +
                                    fadeOut(tween(200)))
                        },
                        label = "screens",
                    ) { v ->
                        val d = v.detail
                        if (d != null) {
                            val lib = state.library
                            when (d) {
                                is Detail.Artist -> {
                                    val tracks = lib.filter { it.artist == d.name }
                                    DetailScreen(
                                        kindLabel = "Artista",
                                        title = d.name,
                                        subtitle = songsLabel(tracks.size),
                                        cover = tracks.firstNotNullOfOrNull { it.artUri },
                                        tracks = tracks,
                                        state = state,
                                        onBack = { detail = null },
                                        onPlay = playAndOpen,
                                        onToggleFavorite = vm::toggleFavorite,
                                        onSetFavorites = vm::setFavorites,
                                    )
                                }
                                is Detail.Album -> {
                                    val tracks = lib.filter { it.album == d.name }
                                    val artist = tracks.map { it.artist }.filter { it.isNotBlank() }.distinct()
                                        .let { if (it.size == 1) it[0] else if (it.isEmpty()) "" else "Varios artistas" }
                                    DetailScreen(
                                        kindLabel = "Álbum",
                                        title = d.name,
                                        subtitle = listOf(artist, songsLabel(tracks.size)).filter { it.isNotBlank() }.joinToString(" · "),
                                        cover = tracks.firstNotNullOfOrNull { it.artUri },
                                        tracks = tracks,
                                        state = state,
                                        onBack = { detail = null },
                                        onPlay = playAndOpen,
                                        onToggleFavorite = vm::toggleFavorite,
                                        onSetFavorites = vm::setFavorites,
                                    )
                                }
                            }
                        } else when (v.screen) {
                            Screen.Home -> HomeScreen(
                                state = state,
                                onPlayAll = { playAndOpen(state.library.shuffled(), 0) },
                                onOpenLibrary = {
                                    vm.selectSource(SOURCE_ALL)
                                    go(Screen.Library)
                                },
                                onOpenFolder = { entry ->
                                    vm.selectSource(entry.key)
                                    go(Screen.Library)
                                },
                                onPlayFolder = { entry -> playAndOpen(entry.tracks, 0) },
                                onAddFolder = { folderPicker.launch(null) },
                                onPlay = playAndOpen,
                                onToggleFavorite = vm::toggleFavorite,
                                onOpenArtist = { name -> detail = Detail.Artist(name) },
                                onOpenArtists = {
                                    exploreTab = 0
                                    go(Screen.Explore)
                                },
                            )
                            Screen.Explore -> ExploreScreen(
                                state = state,
                                tab = exploreTab,
                                onTab = { exploreTab = it },
                                onOpenArtist = { name -> detail = Detail.Artist(name) },
                                onOpenAlbum = { name -> detail = Detail.Album(name) },
                            )
                            Screen.Favorites -> FavoritesScreen(
                                state = state,
                                onPlay = playAndOpen,
                                onToggleFavorite = vm::toggleFavorite,
                            )
                            Screen.Library -> LibraryScreen(
                                state = state,
                                onAddFolder = { folderPicker.launch(null) },
                                onRemoveFolder = vm::removeFolder,
                                onSelectSource = vm::selectSource,
                                onPlay = playAndOpen,
                                onQueue = vm::addToQueue,
                                onToggleFavorite = vm::toggleFavorite,
                            )
                            Screen.Search -> SearchScreen(
                                state = state,
                                onPlay = playAndOpen,
                                onToggleFavorite = vm::toggleFavorite,
                            )
                            Screen.Queue -> QueueScreen(
                                state = state,
                                onJump = { index ->
                                    vm.jumpTo(index)
                                    go(Screen.Player)
                                },
                                onRemove = vm::removeFromQueue,
                                onClearQueued = vm::clearQueued,
                            )
                            // El reproductor siempre se dibuja oscuro, sobre la portada desenfocada
                            Screen.Player -> CompositionLocalProvider(LocalFrost provides playerFrost) {
                                PlayerContent(
                                    state = state,
                                    lyrics = lyricsState,
                                    vm = vm,
                                    onBack = { go(Screen.Home) },
                                    onOpenQueue = { go(Screen.Queue) },
                                )
                            }
                            Screen.Settings -> SettingsScreen(
                                accent = accentColor,
                                onAccent = {
                                    accentArgb = it.toArgb()
                                    SettingsStore.saveAccent(context, accentArgb)
                                },
                                dark = dark,
                                onDark = {
                                    dark = it
                                    SettingsStore.saveDark(context, it)
                                },
                                playerAccentBg = playerAccentBg,
                                onPlayerAccentBg = {
                                    playerAccentBg = it
                                    SettingsStore.savePlayerAccentBg(context, it)
                                },
                                onClearLyrics = vm::clearLyricsCache,
                            )
                        }
                    }
                }

                // Mini reproductor sobre la barra de pestanas
                Column(Modifier.align(Alignment.BottomCenter)) {
                    AnimatedVisibility(
                        visible = screen != Screen.Player && state.current != null,
                        enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 2 },
                        exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 2 },
                    ) {
                        state.current?.let { track ->
                            MiniPlayer(
                                track = track,
                                playing = state.isPlaying,
                                progress = state.progress,
                                favorite = track.id in state.favorites,
                                onToggle = vm::togglePlay,
                                onFavorite = { vm.toggleFavorite(track.id) },
                                onOpen = { go(Screen.Player) },
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                            )
                        }
                    }
                    BottomTabBar(
                        items = tabs,
                        selectedIndex = if (screen.ordinal > Screen.Search.ordinal) -1 else screen.ordinal,
                        onSelect = { go(Screen.entries[it]) },
                    )
                }

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
                            .padding(start = 24.dp, end = 24.dp, bottom = TabBarHeight + 80.dp)
                            .shadow(12.dp, pill, ambientColor = Frost.Tint, spotColor = Frost.Tint)
                            .clip(pill)
                            .background(Frost.Floating)
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
}

/** Barra superior: una pildora de vidrio con menu lateral, logo y nombre, ajustes e interruptor claro/oscuro. */
@Composable
private fun TopBar(dark: Boolean, onMenu: () -> Unit, onSettings: () -> Unit, onToggleDark: () -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        Modifier
            .statusBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 6.dp)
            .fillMaxWidth()
            .card(shape, raised = false)
            .padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(Icons.Rounded.Menu, "Menú", onMenu, size = 40.dp)
        Spacer(Modifier.width(10.dp))
        Image(
            painter = painterResource(R.drawable.logo_mark),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            colorFilter = ColorFilter.tint(Frost.TextPrimary),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.app_name),
            modifier = Modifier.weight(1f),
            color = Frost.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        PlainIconButton(Icons.Rounded.Settings, "Ajustes", onSettings, size = 38.dp, iconSize = 22.dp, tint = Frost.TextSecondary)
        Spacer(Modifier.width(2.dp))
        DarkModeToggle(dark = dark, onToggle = onToggleDark)
    }
}

/** Nombre de la lista que suena: el album o artista si todas son del mismo, o «Mi música». */
private fun listLabel(state: PlayerUiState): String {
    val t = state.tracks
    if (t.isEmpty()) return "Mi música"
    if (t.all { it.id in state.favorites }) return "Mis favoritas"
    t.map { it.album }.distinct().singleOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
    t.map { it.artist }.distinct().singleOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
    return "Mi música"
}

/**
 * Reproductor (como la referencia amarilla): cabecera con la lista que suena, portada grande
 * redondeada (o la letra en su lugar), titulo centrado, progreso, transporte y acciones.
 */
@Composable
private fun PlayerContent(
    state: PlayerUiState,
    lyrics: LyricsState,
    vm: PlayerViewModel,
    onBack: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    val context = LocalContext.current
    var showLyrics by rememberSaveable { mutableStateOf(false) }
    val current = state.current

    // La letra se busca solo cuando se abre el panel (y al cambiar de cancion con el panel abierto)
    LaunchedEffect(showLyrics, current?.id) {
        if (showLyrics && current != null) vm.loadLyrics(current)
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = TabBarHeight), // espacio para la barra de pestanas
    ) {
        // Barra: volver, lista que suena y cola
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton(Icons.Rounded.KeyboardArrowDown, "Volver", onBack, iconSize = 26.dp)
            Text(
                "Lista: “${listLabel(state)}”",
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                color = Frost.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            RoundButton(
                Icons.AutoMirrored.Rounded.QueueMusic, "Cola de reproducción", onOpenQueue,
                badge = state.queuedCount,
            )
        }

        if (state.tracks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Frost.Accent)
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
                // Portada o letra, en el mismo espacio
                Crossfade(targetState = showLyrics, animationSpec = tween(300), label = "coverOrLyrics") { lyricsOn ->
                    if (lyricsOn) {
                        LyricsPanel(
                            state = lyrics,
                            positionMs = state.positionMs,
                            onSeekTo = { ms ->
                                if (state.durationMs > 0) vm.seekToFraction(ms / state.durationMs.toFloat())
                            },
                            onRetry = { current?.let { vm.loadLyrics(it, force = true) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(272.dp),
                        )
                    } else {
                        CoverCarousel(state.tracks, pagerState)
                    }
                }
                Spacer(Modifier.height(18.dp))
                NowPlayingInfo(track = current)
                Spacer(Modifier.height(10.dp))
                PlayerControls(
                    state = state,
                    accent = Frost.Accent,
                    onPrevious = vm::previous,
                    onTogglePlay = vm::togglePlay,
                    onNext = vm::next,
                    onSeek = vm::seekToFraction,
                    onShuffle = vm::toggleShuffle,
                    onRepeat = vm::cycleRepeat,
                    favorite = current != null && current.id in state.favorites,
                    onFavorite = { current?.let { vm.toggleFavorite(it.id) } },
                    lyricsOn = showLyrics,
                    onLyrics = { showLyrics = !showLyrics },
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
