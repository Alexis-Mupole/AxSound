package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.PulseApplication
import com.example.R
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class PulseMediaPlaybackService : MediaSessionService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    val effectsController = AudioEffectsController()

    private val _playerState = MutableStateFlow(PulsePlayerState())
    val playerState: StateFlow<PulsePlayerState> = _playerState.asStateFlow()

    private var playlistSongs: List<Song> = emptyList()

    private var sleepTimerJob: Job? = null
    private var positionTickerJob: Job? = null
    private var idleReleaseHandler: Handler? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var audioManager: AudioManager
    private var audioDeviceCallback: AudioDeviceCallback? = null

    companion object {
        var instance: PulseMediaPlaybackService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setupNotificationProvider()
        initializePlayer()
        setupMediaSession()
        setupAudioDeviceListener()
        startPositionTicker()

        idleReleaseHandler = Handler(Looper.getMainLooper())
    }

    private fun setupNotificationProvider() {
        val channelId = "axsound_playback_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AxSound Playback Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls and lock screen media notification"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(channelId)
            .setChannelName(R.string.playback_notification_channel_name)
            .setNotificationId(1001)
            .build()

        setMediaNotificationProvider(notificationProvider)
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // true = handle audio focus automatically
            .setHandleAudioBecomingNoisy(true) // pause when headphones disconnect
            .setWakeMode(C.WAKE_MODE_LOCAL) // wake lock only held while playing
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    acquireWakeLock()
                    cancelIdleReleaseTimeout()
                    // Record play history in database
                    _playerState.value.currentSong?.let { song ->
                        serviceScope.launch {
                            try {
                                val repo = (application as? PulseApplication)?.container?.musicRepository
                                repo?.recordSongPlayed(song.id)
                            } catch (_: Exception) {}
                        }
                    }
                } else {
                    releaseWakeLock()
                    scheduleIdleReleaseTimeout()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val buffering = playbackState == Player.STATE_BUFFERING
                updateState {
                    it.copy(
                        isBuffering = buffering,
                        durationMs = player.duration.coerceAtLeast(0L),
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                        bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                    )
                }

                if (playbackState == Player.STATE_ENDED) {
                    if (player.repeatMode == Player.REPEAT_MODE_ONE) {
                        player.seekTo(0)
                        player.play()
                    } else if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                        player.play()
                    } else if (playlistSongs.isNotEmpty()) {
                        // Loop to first song so playback never stops unexpectedly
                        player.seekTo(0, 0L)
                        player.prepare()
                        player.play()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                val currentSong = playlistSongs.getOrNull(index)
                updateState {
                    it.copy(
                        currentSong = currentSong,
                        queueIndex = index,
                        durationMs = player.duration.coerceAtLeast(0L),
                        currentPositionMs = 0L,
                        errorMessage = null
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PulsePlayback", "Player error encountered: ${error.message}", error)
                val failedTitle = _playerState.value.currentSong?.title ?: "Track"
                updateState {
                    it.copy(errorMessage = "Format unsupported or file unreadable: $failedTitle")
                }
                // Graceful fallback: skip to next song without crashing
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare()
                    player.play()
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                effectsController.attachToSession(audioSessionId)
                evaluateAudioOffload()
            }
        })

        // Initial audio session attachment
        effectsController.attachToSession(player.audioSessionId)
        evaluateAudioOffload()
    }

    private fun setupMediaSession() {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        if (songs.isEmpty()) return
        playlistSongs = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.artworkUri)
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, startPositionMs)
        player.prepare()
        player.play()

        updateState {
            it.copy(
                queue = songs,
                queueIndex = startIndex,
                currentSong = songs.getOrNull(startIndex)
            )
        }
    }

    fun play() = player.play()
    fun pause() = player.pause()

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        updateState { it.copy(currentPositionMs = positionMs) }
    }

    fun skipToNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (playlistSongs.isNotEmpty()) {
            player.seekTo(0, 0L)
        }
    }

    fun skipToPrevious() {
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun setShuffleMode(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
        updateState { it.copy(shuffleMode = enabled) }
    }

    fun setRepeatMode(mode: Int) {
        val playerMode = when (mode) {
            1 -> Player.REPEAT_MODE_ALL
            2 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = playerMode
        updateState { it.copy(repeatMode = mode) }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        updateState { it.copy(playbackSpeed = speed) }
        evaluateAudioOffload()
    }

    fun setVolumeBoost(percent: Int) {
        effectsController.setVolumeBoost(percent)
        updateState {
            it.copy(
                volumeBoostPercent = percent,
                isLimiterActive = effectsController.isLimiterActive
            )
        }
        evaluateAudioOffload()
    }

    fun setEfficiencyMode(enabled: Boolean) {
        updateState { it.copy(efficiencyModeEnabled = enabled) }
        evaluateAudioOffload()
    }

    /**
     * Audio Offload evaluation:
     * When Efficiency Mode is ON, audio offload is active if NO effects (EQ, Boost, Pitch, etc.) are active.
     */
    @OptIn(UnstableApi::class)
    private fun evaluateAudioOffload() {
        val efficiencyOn = _playerState.value.efficiencyModeEnabled
        val speedNormal = _playerState.value.playbackSpeed == 1.0f
        val effectsIdle = !effectsController.isEffectsActive

        val canOffload = efficiencyOn && speedNormal && effectsIdle

        val trackSelectionParams = player.trackSelectionParameters.buildUpon()
            .setAudioOffloadPreferences(
                TrackSelectionParameters.AudioOffloadPreferences.Builder()
                    .setAudioOffloadMode(
                        if (canOffload)
                            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
                        else
                            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
                    )
                    .setIsGaplessSupportRequired(false)
                    .build()
            )
            .build()

        player.trackSelectionParameters = trackSelectionParams
        updateState { it.copy(isAudioOffloadActive = canOffload) }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            updateState { it.copy(sleepTimerRemainingSeconds = null) }
            return
        }

        var remainingSeconds = minutes * 60L
        updateState { it.copy(sleepTimerRemainingSeconds = remainingSeconds) }

        sleepTimerJob = serviceScope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1000)
                remainingSeconds -= 1
                updateState { it.copy(sleepTimerRemainingSeconds = remainingSeconds) }
            }
            // Timer expired: stop playback gracefully
            pause()
            updateState { it.copy(sleepTimerRemainingSeconds = null) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        updateState { it.copy(sleepTimerRemainingSeconds = null) }
    }

    private fun startPositionTicker() {
        positionTickerJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    updateState {
                        it.copy(
                            currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                            durationMs = player.duration.coerceAtLeast(0L),
                            bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                        )
                    }
                }
                delay(1000) // 1 Hz for power efficiency
            }
        }
    }

    // 10 minutes paused timeout to release wake locks and player resources
    private fun scheduleIdleReleaseTimeout() {
        idleReleaseHandler?.removeCallbacksAndMessages(null)
        idleReleaseHandler?.postDelayed({
            if (!player.isPlaying) {
                Log.d("PulsePlayback", "Releasing idle resources after 10 minutes paused")
                effectsController.releaseEffects()
                stopForeground(STOP_FOREGROUND_DETACH)
            }
        }, 10 * 60 * 1000L)
    }

    private fun cancelIdleReleaseTimeout() {
        idleReleaseHandler?.removeCallbacksAndMessages(null)
    }

    private fun setupAudioDeviceListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioDeviceCallback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentOutputType()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentOutputType()
                }
            }
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        }
        updateCurrentOutputType()
    }

    private fun updateCurrentOutputType() {
        val outputType = when {
            audioManager.isBluetoothA2dpOn -> AudioOutputType.BLUETOOTH
            audioManager.isWiredHeadsetOn -> AudioOutputType.HEADPHONES
            else -> AudioOutputType.SPEAKER
        }
        updateState { it.copy(audioOutputType = outputType) }
    }

    private inline fun updateState(crossinline block: (PulsePlayerState) -> PulsePlayerState) {
        _playerState.value = block(_playerState.value)
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AxSound:PlaybackWakeLock").apply {
                setReferenceCounted(false)
            }
        }
        try {
            wakeLock?.acquire(4 * 3600 * 1000L) // 4 hours continuous safety timeout
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        releaseWakeLock()
        instance = null
        serviceScope.cancel()
        positionTickerJob?.cancel()
        sleepTimerJob?.cancel()
        idleReleaseHandler?.removeCallbacksAndMessages(null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        }

        effectsController.releaseEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
