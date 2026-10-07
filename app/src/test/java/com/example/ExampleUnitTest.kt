package com.example

import android.net.Uri
import com.example.data.model.Song
import com.example.ui.theme.GradientThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun songDurationFormatting_isCorrect() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            durationMs = 215000L, // 3 mins 35 seconds
            contentUri = Uri.parse("content://media/1"),
            artworkUri = null,
            dataPath = "/music/song.mp3",
            mimeType = "audio/mpeg",
            format = "MP3"
        )
        assertEquals("3:35", song.durationFormatted)
    }

    @Test
    fun hiResBadge_detectsCorrectQuality() {
        val flacSong = Song(
            id = 2L,
            title = "Lossless Track",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            durationMs = 180000L,
            contentUri = Uri.parse("content://media/2"),
            artworkUri = null,
            dataPath = "/music/song.flac",
            mimeType = "audio/flac",
            format = "FLAC",
            bitDepth = 24,
            sampleRateHz = 96000
        )
        assertTrue(flacSong.audioQualityBadge.contains("HI-RES"))
        assertTrue(flacSong.audioQualityBadge.contains("24-bit"))
        assertTrue(flacSong.audioQualityBadge.contains("96 kHz"))
    }

    @Test
    fun gradientThemeLookup_fallsBackGracefully() {
        val midnight = GradientThemes.getByName("Midnight")
        assertEquals("Midnight", midnight.name)

        val fallback = GradientThemes.getByName("NonExistentTheme")
        assertEquals("Midnight", fallback.name)
    }
}
