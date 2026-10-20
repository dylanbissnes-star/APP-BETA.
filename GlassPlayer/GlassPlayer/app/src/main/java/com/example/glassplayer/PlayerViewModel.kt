package com.example.glassplayer

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.glassplayer.data.Track
import com.example.glassplayer.data.TrackRepository
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val loading: Boolean = true,
) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var pendingTracks: List<Track>? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = syncFromPlayer(player)
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            pendingTracks?.let { applyTracks(it) }
            syncFromPlayer(controller!!)
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

    /** Llamar al iniciar y cada vez que se concede el permiso de audio. */
    fun loadTracks(hasAudioPermission: Boolean) {
        viewModelScope.launch {
            var list = if (hasAudioPermission) TrackRepository.loadLocal(getApplication()) else emptyList()
            if (list.isEmpty()) list = TrackRepository.demo()
            _state.update { it.copy(tracks = list, loading = false, currentIndex = 0) }
            if (controller != null) applyTracks(list) else pendingTracks = list
        }
    }

    private fun applyTracks(list: List<Track>) {
        val c = controller ?: return
        c.setMediaItems(list.map(Track::toMediaItem))
        c.prepare()
        pendingTracks = null
    }

    private fun syncFromPlayer(p: Player) {
        _state.update {
            it.copy(
                currentIndex = if (p.mediaItemCount > 0) p.currentMediaItemIndex else it.currentIndex,
                isPlaying = p.isPlaying,
                positionMs = p.currentPosition,
                durationMs = p.duration.coerceAtLeast(0L),
                shuffle = p.shuffleModeEnabled,
                repeatMode = p.repeatMode,
            )
        }
    }

    // ---- Acciones de la UI ----

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

    fun playIndex(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount && index != c.currentMediaItemIndex) {
            c.seekToDefaultPosition(index)
            c.play()
        }
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

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        super.onCleared()
    }
}
