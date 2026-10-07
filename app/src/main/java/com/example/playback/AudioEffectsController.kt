package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log

enum class AudioOutputType {
    SPEAKER, HEADPHONES, BLUETOOTH
}

class AudioEffectsController {

    private var audioSessionId: Int = 0

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    var isLoudnessEnhancerSupported: Boolean = false
        private set
    var isLimiterSupported: Boolean = false
        private set

    var currentBoostPercent: Int = 100
        private set
    var isLimiterActive: Boolean = false
        private set
    var isEffectsActive: Boolean = false
        private set

    fun attachToSession(sessionId: Int) {
        if (sessionId == 0) return
        if (audioSessionId == sessionId) return

        releaseEffects()
        audioSessionId = sessionId

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = true
            }
            isLoudnessEnhancerSupported = true
        } catch (e: Exception) {
            Log.w("AudioEffects", "LoudnessEnhancer not supported on this device/session", e)
            isLoudnessEnhancerSupported = false
        }

        // Initialize DynamicsProcessing Limiter on API 28+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val config = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, // 2 channels stereo
                    false, 0, // no pre-eq
                    false, 0, // no mbc
                    false, 0, // no post-eq
                    true      // limiter enabled
                ).build()

                dynamicsProcessing = DynamicsProcessing(0, sessionId, config).apply {
                    val limiter = DynamicsProcessing.Limiter(
                        true, // inUse
                        true, // enabled
                        0,    // linkGroup
                        1.0f, // attackTime (ms)
                        50.0f,// releaseTime (ms)
                        10.0f,// ratio (10:1 limiting)
                        -0.5f,// threshold in dBFS (prevent digital clipping)
                        0.0f  // postGain
                    )
                    setLimiterByChannelIndex(0, limiter)
                    setLimiterByChannelIndex(1, limiter)
                    enabled = true
                }
                isLimiterSupported = true
            } catch (e: Exception) {
                Log.w("AudioEffects", "DynamicsProcessing Limiter not supported", e)
                isLimiterSupported = false
            }
        }

        // Equalizer
        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Equalizer init error", e)
        }

        // Bass Boost
        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "BassBoost init error", e)
        }

        // Virtualizer
        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Virtualizer init error", e)
        }

        // Re-apply current boost
        setVolumeBoost(currentBoostPercent)
    }

    /**
     * Volume Boost: 100% = 0 mB (unity), 200% = +600 mB (+6 dB), 400% = +1200 mB (+12 dB)
     */
    fun setVolumeBoost(percent: Int) {
        currentBoostPercent = percent
        val gainMb = when {
            percent <= 100 -> 0
            else -> ((percent - 100) * 4).coerceIn(0, 1200) // 200% -> 400 mB, 400% -> 1200 mB
        }

        try {
            loudnessEnhancer?.let { enhancer ->
                enhancer.setTargetGain(gainMb)
                enhancer.enabled = gainMb > 0
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Error setting loudness enhancer gain", e)
        }

        // Limiter should engage whenever boost > 100% to protect against clipping
        val shouldLimit = gainMb > 0
        isLimiterActive = shouldLimit && isLimiterSupported
        try {
            dynamicsProcessing?.enabled = shouldLimit
        } catch (_: Exception) {}

        updateEffectsActive()
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
        } catch (_: Exception) {}
        updateEffectsActive()
    }

    fun setEqualizerPreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
        } catch (_: Exception) {}
    }

    fun setEqualizerBandLevel(band: Short, levelMilliBels: Short) {
        try {
            equalizer?.setBandLevel(band, levelMilliBels)
        } catch (_: Exception) {}
    }

    fun getNumberOfBands(): Short {
        return try {
            equalizer?.numberOfBands ?: 5
        } catch (_: Exception) { 5 }
    }

    fun getBandFreqRange(band: Short): IntArray {
        return try {
            equalizer?.getBandFreqRange(band) ?: intArrayOf(60000, 14000000)
        } catch (_: Exception) { intArrayOf(60000, 14000000) }
    }

    fun getCenterFreq(band: Short): Int {
        return try {
            equalizer?.getCenterFreq(band) ?: 1000000
        } catch (_: Exception) { 1000000 }
    }

    fun getBandLevelRange(): ShortArray {
        return try {
            equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
        } catch (_: Exception) { shortArrayOf(-1500, 1500) }
    }

    fun setBassBoost(strength: Short) {
        try {
            bassBoost?.let {
                it.enabled = strength > 0
                if (strength > 0) it.setStrength(strength)
            }
        } catch (_: Exception) {}
        updateEffectsActive()
    }

    fun setVirtualizer(strength: Short) {
        try {
            virtualizer?.let {
                it.enabled = strength > 0
                if (strength > 0) it.setStrength(strength)
            }
        } catch (_: Exception) {}
        updateEffectsActive()
    }

    private fun updateEffectsActive() {
        val boostOn = currentBoostPercent > 100
        val eqOn = equalizer?.enabled == true
        val bassOn = bassBoost?.enabled == true
        val virtOn = virtualizer?.enabled == true
        isEffectsActive = boostOn || eqOn || bassOn || virtOn
    }

    fun releaseEffects() {
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        try { dynamicsProcessing?.release() } catch (_: Exception) {}
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}

        loudnessEnhancer = null
        dynamicsProcessing = null
        equalizer = null
        bassBoost = null
        virtualizer = null
        audioSessionId = 0
        isEffectsActive = false
    }
}
