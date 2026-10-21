package com.example.glassplayer

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.glassplayer.data.Folder
import com.example.glassplayer.data.FolderScanner
import com.example.glassplayer.data.FolderStore
import com.example.glassplayer.data.SOURCE_ALL
import com.example.glassplayer.data.SOURCE_DEMO
import com.example.glassplayer.data.Track
import com.example.glassplayer.data.TrackRepository
import com.example.glassplayer.data.toTrack
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    /** Lista de reproduccion actual (es la del reproductor: carrusel y cola salen de aqui). */
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    /** Cuantas canciones agregadas "a la cola" hay justo despues de la actual. */
    val queuedCount: Int = 0,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,

    // Biblioteca
    val library: List<Track> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedSource: String = SOURCE_ALL,
    val loading: Boolean = true,
    val scanning: Boolean = false,
    val message: String? = null,
) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    private val queuedStart: Int get() = (currentIndex + 1).coerceAtMost(tracks.size)
    private val queuedEnd: Int get() = (currentIndex + 1 + queuedCount).coerceIn(queuedStart, tracks.size)

    /** Canciones que el usuario agrego a la cola (van justo despues de la actual). */
    val queued: List<Track> get() = tracks.subList(queuedStart, queuedEnd)

    /** El resto de la lista que sigue despues de la cola. */
    val upcoming: List<Track> get() = tracks.subList(queuedEnd, tracks.size)
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var libraryReady = false
    private var queueInitialized = false
    private var lastIndex = 0

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val p = controller ?: return
            val newIndex = p.currentMediaItemIndex
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                // Al avanzar, las canciones de la cola se van "gastando".
                val delta = newIndex - lastIndex
                _state.update { s ->
                    s.copy(
                        queuedCount = when {
                            delta > 0 -> (s.queuedCount - delta).coerceAtLeast(0)
                            delta < 0 -> 0
                            else -> s.queuedCount
                        }
                    )
                }
            }
            lastIndex = newIndex
        }

        override fun onEvents(player: Player, events: Player.Events) {
            syncFromPlayer(player, events.contains(Player.EVENT_TIMELINE_CHANGED))
        }
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            val c = runCatching { future.get() }.getOrNull() ?: return@addListener
            controller = c
            c.addListener(listener)
            lastIndex = c.currentMediaItemIndex
            syncFromPlayer(c, rebuildTracks = true)
            tryInitQueue()
        }, MoreExecutors.directExecutor())

        // Actualiza la posicion mientras suena.
        viewModelScope.launch {
            while (true) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        _state.update {
                            it.copy(positionMs = c.currentPosition, durationMs = c.duration.coerceAtLeast(0L))
                        }
                    }
                }
                delay(250)
            }
        }
    }

    // ------------------------------------------------------------------
    // Biblioteca y carpetas
    // ------------------------------------------------------------------

    /** Llamar al iniciar y cada vez que se concede el permiso de audio. */
    fun loadLibrary(hasAudioPermission: Boolean) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            val folders = FolderStore.load(ctx)
            _state.update { it.copy(folders = folders, scanning = true) }

            val device = if (hasAudioPermission) TrackRepository.loadLocal(ctx) else emptyList()
            _state.update { it.copy(library = device, loading = false) }

            for (folder in folders) {
                val tracks = FolderScanner.scan(ctx, folder.uri.toUri())
                _state.update { s -> s.copy(library = (s.library + tracks).distinctBy { it.id }) }
            }

            if (_state.value.library.isEmpty()) {
                _state.update { it.copy(library = TrackRepository.demo()) }
            }
            _state.update { it.copy(scanning = false) }
            libraryReady = true
            tryInitQueue()
        }
    }

    /** El usuario eligio una carpeta en el selector del sistema. */
    fun addFolder(uri: Uri) {
        val ctx = getApplication<Application>()
        FolderStore.add(ctx, uri)
        val folder = Folder(uri.toString(), FolderStore.nameFor(uri))
        _state.update { s ->
            s.copy(
                folders = (s.folders.filterNot { it.uri == folder.uri } + folder).sortedBy { it.name.lowercase() },
                scanning = true,
                selectedSource = folder.uri,
            )
        }
        viewModelScope.launch {
            val tracks = FolderScanner.scan(ctx, uri)
            _state.update { s ->
                val base = s.library
                    .filterNot { it.sourceId == folder.uri }
                    .let { list -> if (tracks.isNotEmpty()) list.filterNot { it.sourceId == SOURCE_DEMO } else list }
                s.copy(
                    library = (base + tracks).distinctBy { it.id },
                    scanning = false,
                    message = if (tracks.isEmpty()) "No hay música en «${folder.name}»"
                    else "${tracks.size} canciones de «${folder.name}»",
                )
            }
            libraryReady = true
            tryInitQueue()
        }
    }

    fun removeFolder(folderUri: String) {
        FolderStore.remove(getApplication(), folderUri)
        _state.update { s ->
            s.copy(
                folders = s.folders.filterNot { it.uri == folderUri },
                library = s.library.filterNot { it.sourceId == folderUri },
                selectedSource = if (s.selectedSource == folderUri) SOURCE_ALL else s.selectedSource,
                message = "Carpeta quitada",
            )
        }
    }

    fun selectSource(id: String) = _state.update { it.copy(selectedSource = id) }

    fun clearMessage() = _state.update { it.copy(message = null) }

    /** Carga la biblioteca en el reproductor la primera vez (si no hay ya algo sonando). */
    private fun tryInitQueue() {
        val c = controller ?: return
        if (!libraryReady || queueInitialized) return
        queueInitialized = true
        if (c.mediaItemCount == 0) {
            c.setMediaItems(_state.value.library.map(Track::toMediaItem))
            c.prepare()
        }
    }

    private fun syncFromPlayer(p: Player, rebuildTracks: Boolean) {
        val items = if (rebuildTracks) List(p.mediaItemCount) { p.getMediaItemAt(it).toTrack() } else null
        _state.update {
            it.copy(
                tracks = items ?: it.tracks,
                currentIndex = if (p.mediaItemCount > 0) p.currentMediaItemIndex else 0,
                isPlaying = p.isPlaying,
                positionMs = p.currentPosition,
                durationMs = p.duration.coerceAtLeast(0L),
                shuffle = p.shuffleModeEnabled,
                repeatMode = p.repeatMode,
            )
        }
    }

    // ------------------------------------------------------------------
    // Reproduccion
    // ------------------------------------------------------------------

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNextMediaItem() }

    fun previous() {
        val c = controller ?: return
        // Si ya avanzo mas de 3 s, reinicia la cancion; si no, va a la anterior.
        if (c.currentPosition > 3000) c.seekTo(0) else c.seekToPreviousMediaItem()
    }

    /** Desde el carrusel: saltar a una cancion de la lista actual. */
    fun playIndex(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount && index != c.currentMediaItemIndex) {
            c.seekToDefaultPosition(index)
            c.play()
        }
    }

    /** Desde la biblioteca: reproducir `list` empezando en `index` (se conserva la cola). */
    fun playFromList(list: List<Track>, index: Int) {
        val c = controller ?: return
        if (index !in list.indices) return
        queueInitialized = true
        val keep = _state.value.queued
        c.setMediaItems(list.map(Track::toMediaItem), index, 0L)
        if (keep.isNotEmpty()) c.addMediaItems(index + 1, keep.map(Track::toMediaItem))
        c.prepare()
        c.play()
        lastIndex = index
        _state.update { it.copy(queuedCount = keep.size) }
    }

    fun seekToFraction(fraction: Float) {
        val c = controller ?: return
        val d = c.duration
        if (d > 0) c.seekTo((d * fraction.coerceIn(0f, 1f)).toLong())
    }

    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    // ------------------------------------------------------------------
    // Cola de reproduccion
    // ------------------------------------------------------------------

    /** Agrega la cancion justo despues de la actual (y despues de lo que ya estaba en cola). */
    fun addToQueue(track: Track) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            queueInitialized = true
            c.setMediaItem(track.toMediaItem())
            c.prepare()
            c.play()
            lastIndex = 0
            _state.update { it.copy(message = "Reproduciendo «${track.title}»") }
            return
        }
        val at = (c.currentMediaItemIndex + 1 + _state.value.queuedCount).coerceAtMost(c.mediaItemCount)
        c.addMediaItem(at, track.toMediaItem())
        _state.update { it.copy(queuedCount = it.queuedCount + 1, message = "«${track.title}» añadida a la cola") }
    }

    /** Ir a una cancion de la cola / lista (por su posicion real en el reproductor). */
    fun jumpTo(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) {
            c.seekToDefaultPosition(index)
            c.play()
        }
    }

    fun removeFromQueue(index: Int) {
        val c = controller ?: return
        val cur = c.currentMediaItemIndex
        if (index == cur || index !in 0 until c.mediaItemCount) return
        c.removeMediaItem(index)
        _state.update { s ->
            val wasQueued = index in (cur + 1)..(cur + s.queuedCount)
            s.copy(queuedCount = if (wasQueued) s.queuedCount - 1 else s.queuedCount)
        }
    }

    /** Quita solo lo que el usuario agrego a la cola (no el resto de la lista). */
    fun clearQueued() {
        val c = controller ?: return
        val n = _state.value.queuedCount
        if (n <= 0) return
        val from = c.currentMediaItemIndex + 1
        c.removeMediaItems(from, (from + n).coerceAtMost(c.mediaItemCount))
        _state.update { it.copy(queuedCount = 0, message = "Cola vaciada") }
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        super.onCleared()
    }
}
