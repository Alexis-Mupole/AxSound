package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@UnstableApi
class MusicPlayerClient(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _playerState = MutableStateFlow(PulsePlayerState())
    val playerState: StateFlow<PulsePlayerState> = _playerState.asStateFlow()

    init {
        // Start or bind to service
        val intent = Intent(context, PulseMediaPlaybackService::class.java)
        try {
            context.startService(intent)
        } catch (_: Exception) {}

        scope.launch {
            // Periodically attach to the service instance
            while (true) {
                PulseMediaPlaybackService.instance?.let { service ->
                    service.playerState.collect { state ->
                        _playerState.value = state
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun getService(): PulseMediaPlaybackService? {
        val s = PulseMediaPlaybackService.instance
        if (s == null) {
            val intent = Intent(context, PulseMediaPlaybackService::class.java)
            try { context.startService(intent) } catch (_: Exception) {}
        }
        return s
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        getService()?.playSongList(songs, startIndex, startPositionMs)
    }

    fun play() = getService()?.play()
    fun pause() = getService()?.pause()
    fun togglePlayPause() = getService()?.togglePlayPause()
    fun seekTo(positionMs: Long) = getService()?.seekTo(positionMs)
    fun skipToNext() = getService()?.skipToNext()
    fun skipToPrevious() = getService()?.skipToPrevious()
    fun setShuffleMode(enabled: Boolean) = getService()?.setShuffleMode(enabled)
    fun setRepeatMode(mode: Int) = getService()?.setRepeatMode(mode)
    fun setPlaybackSpeed(speed: Float) = getService()?.setPlaybackSpeed(speed)
    fun setVolumeBoost(percent: Int) = getService()?.setVolumeBoost(percent)
    fun setEfficiencyMode(enabled: Boolean) = getService()?.setEfficiencyMode(enabled)
    fun startSleepTimer(minutes: Int) = getService()?.startSleepTimer(minutes)
    fun cancelSleepTimer() = getService()?.cancelSleepTimer()

    fun getEffectsController(): AudioEffectsController? = getService()?.effectsController
}
