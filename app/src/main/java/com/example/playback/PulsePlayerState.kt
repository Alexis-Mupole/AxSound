package com.example.playback

import com.example.data.model.Song

data class PulsePlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val shuffleMode: Boolean = false,
    val repeatMode: Int = 0, // 0 = OFF, 1 = ALL, 2 = ONE
    val playbackSpeed: Float = 1.0f,
    val volumeBoostPercent: Int = 100,
    val isLimiterActive: Boolean = false,
    val isAudioOffloadActive: Boolean = true,
    val efficiencyModeEnabled: Boolean = true,
    val audioOutputType: AudioOutputType = AudioOutputType.SPEAKER,
    val sleepTimerRemainingSeconds: Long? = null,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
