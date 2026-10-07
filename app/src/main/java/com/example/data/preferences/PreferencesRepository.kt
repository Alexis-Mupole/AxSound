package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulsewave_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class SortOption {
    TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColorHex: String = "#8B5CF6", // Electric Violet
    val gradientThemeName: String = "Midnight",
    val useDynamicColor: Boolean = false,
    val useAdaptiveAlbumArtColor: Boolean = true,
    val cornerRoundnessDp: Int = 20,
    val fontScale: Float = 1.0f,
    val iconStyle: String = "rounded",

    val efficiencyMode: Boolean = true,
    val volumeBoostPercent: Int = 100, // 100% = normal (0 dB), 200% = +6 dB, 400% = +12 dB
    val boostWarningDismissed: Boolean = false,
    val loudnessNormalization: Boolean = false,
    val crossfadeSeconds: Int = 0,
    val playbackSpeed: Float = 1.0f,

    val equalizerEnabled: Boolean = false,
    val equalizerPreset: Int = 0,
    val equalizerBandLevels: String = "0,0,0,0,0",
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,

    val speakerBoostPercent: Int = 100,
    val headphoneBoostPercent: Int = 100,
    val bluetoothBoostPercent: Int = 100,

    val minDurationSeconds: Int = 30,
    val excludedFolders: Set<String> = emptySet(),
    val sortOption: SortOption = SortOption.TITLE,
    val sortAscending: Boolean = true,

    val lastPlayedSongId: Long = -1L,
    val lastPlaybackPositionMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val repeatMode: Int = 0 // 0 = OFF, 1 = ALL, 2 = ONE
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val GRADIENT_THEME = stringPreferencesKey("gradient_theme")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val USE_ADAPTIVE_ALBUM_COLOR = booleanPreferencesKey("use_adaptive_album_color")
        val CORNER_ROUNDNESS = intPreferencesKey("corner_roundness")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val ICON_STYLE = stringPreferencesKey("icon_style")

        val EFFICIENCY_MODE = booleanPreferencesKey("efficiency_mode")
        val VOLUME_BOOST_PERCENT = intPreferencesKey("volume_boost_percent")
        val BOOST_WARNING_DISMISSED = booleanPreferencesKey("boost_warning_dismissed")
        val LOUDNESS_NORMALIZATION = booleanPreferencesKey("loudness_normalization")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")

        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val EQUALIZER_BAND_LEVELS = stringPreferencesKey("equalizer_band_levels")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")

        val SPEAKER_BOOST_PERCENT = intPreferencesKey("speaker_boost_percent")
        val HEADPHONE_BOOST_PERCENT = intPreferencesKey("headphone_boost_percent")
        val BLUETOOTH_BOOST_PERCENT = intPreferencesKey("bluetooth_boost_percent")

        val MIN_DURATION_SECONDS = intPreferencesKey("min_duration_seconds")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")

        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYBACK_POSITION_MS = longPreferencesKey("last_playback_position_ms")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name),
            accentColorHex = prefs[Keys.ACCENT_COLOR_HEX] ?: "#8B5CF6",
            gradientThemeName = prefs[Keys.GRADIENT_THEME] ?: "Midnight",
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: false,
            useAdaptiveAlbumArtColor = prefs[Keys.USE_ADAPTIVE_ALBUM_COLOR] ?: true,
            cornerRoundnessDp = prefs[Keys.CORNER_ROUNDNESS] ?: 20,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            iconStyle = prefs[Keys.ICON_STYLE] ?: "rounded",

            efficiencyMode = prefs[Keys.EFFICIENCY_MODE] ?: true,
            volumeBoostPercent = prefs[Keys.VOLUME_BOOST_PERCENT] ?: 100,
            boostWarningDismissed = prefs[Keys.BOOST_WARNING_DISMISSED] ?: false,
            loudnessNormalization = prefs[Keys.LOUDNESS_NORMALIZATION] ?: false,
            crossfadeSeconds = prefs[Keys.CROSSFADE_SECONDS] ?: 0,
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,

            equalizerEnabled = prefs[Keys.EQUALIZER_ENABLED] ?: false,
            equalizerPreset = prefs[Keys.EQUALIZER_PRESET] ?: 0,
            equalizerBandLevels = prefs[Keys.EQUALIZER_BAND_LEVELS] ?: "0,0,0,0,0",
            bassBoostStrength = prefs[Keys.BASS_BOOST_STRENGTH] ?: 0,
            virtualizerStrength = prefs[Keys.VIRTUALIZER_STRENGTH] ?: 0,

            speakerBoostPercent = prefs[Keys.SPEAKER_BOOST_PERCENT] ?: 100,
            headphoneBoostPercent = prefs[Keys.HEADPHONE_BOOST_PERCENT] ?: 100,
            bluetoothBoostPercent = prefs[Keys.BLUETOOTH_BOOST_PERCENT] ?: 100,

            minDurationSeconds = prefs[Keys.MIN_DURATION_SECONDS] ?: 30,
            excludedFolders = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet(),
            sortOption = SortOption.valueOf(prefs[Keys.SORT_OPTION] ?: SortOption.TITLE.name),
            sortAscending = prefs[Keys.SORT_ASCENDING] ?: true,

            lastPlayedSongId = prefs[Keys.LAST_PLAYED_SONG_ID] ?: -1L,
            lastPlaybackPositionMs = prefs[Keys.LAST_PLAYBACK_POSITION_MS] ?: 0L,
            shuffleMode = prefs[Keys.SHUFFLE_MODE] ?: false,
            repeatMode = prefs[Keys.REPEAT_MODE] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(hex: String) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR_HEX] = hex }
    }

    suspend fun setGradientTheme(name: String) {
        context.dataStore.edit { it[Keys.GRADIENT_THEME] = name }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setUseAdaptiveAlbumArtColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_ADAPTIVE_ALBUM_COLOR] = enabled }
    }

    suspend fun setCornerRoundness(dp: Int) {
        context.dataStore.edit { it[Keys.CORNER_ROUNDNESS] = dp }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[Keys.FONT_SCALE] = scale }
    }

    suspend fun setEfficiencyMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EFFICIENCY_MODE] = enabled }
    }

    suspend fun setVolumeBoostPercent(percent: Int) {
        context.dataStore.edit { it[Keys.VOLUME_BOOST_PERCENT] = percent }
    }

    suspend fun setBoostWarningDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.BOOST_WARNING_DISMISSED] = dismissed }
    }

    suspend fun setCrossfadeSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_SECONDS] = seconds }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EQUALIZER_ENABLED] = enabled }
    }

    suspend fun setEqualizerPreset(preset: Int) {
        context.dataStore.edit { it[Keys.EQUALIZER_PRESET] = preset }
    }

    suspend fun setEqualizerBandLevels(levels: String) {
        context.dataStore.edit { it[Keys.EQUALIZER_BAND_LEVELS] = levels }
    }

    suspend fun setBassBoost(strength: Int) {
        context.dataStore.edit { it[Keys.BASS_BOOST_STRENGTH] = strength }
    }

    suspend fun setVirtualizer(strength: Int) {
        context.dataStore.edit { it[Keys.VIRTUALIZER_STRENGTH] = strength }
    }

    suspend fun setSpeakerBoostPercent(percent: Int) {
        context.dataStore.edit { it[Keys.SPEAKER_BOOST_PERCENT] = percent }
    }

    suspend fun setHeadphoneBoostPercent(percent: Int) {
        context.dataStore.edit { it[Keys.HEADPHONE_BOOST_PERCENT] = percent }
    }

    suspend fun setBluetoothBoostPercent(percent: Int) {
        context.dataStore.edit { it[Keys.BLUETOOTH_BOOST_PERCENT] = percent }
    }

    suspend fun setMinDurationSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.MIN_DURATION_SECONDS] = seconds }
    }

    suspend fun setSort(option: SortOption, ascending: Boolean) {
        context.dataStore.edit {
            it[Keys.SORT_OPTION] = option.name
            it[Keys.SORT_ASCENDING] = ascending
        }
    }

    suspend fun savePlaybackState(songId: Long, positionMs: Long, shuffle: Boolean, repeat: Int) {
        context.dataStore.edit {
            it[Keys.LAST_PLAYED_SONG_ID] = songId
            it[Keys.LAST_PLAYBACK_POSITION_MS] = positionMs
            it[Keys.SHUFFLE_MODE] = shuffle
            it[Keys.REPEAT_MODE] = repeat
        }
    }
}
