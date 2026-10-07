package com.example.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val contentUri: Uri,
    val artworkUri: Uri?,
    val dataPath: String,
    val mimeType: String,
    val format: String, // MP3, FLAC, WAV, AAC, etc.
    val bitrateKbps: Int = 0,
    val sampleRateHz: Int = 0,
    val bitDepth: Int = 16,
    val year: Int = 0,
    val trackNumber: Int = 0,
    val genre: String = "Unknown",
    val dateAdded: Long = 0,
    val sizeBytes: Long = 0,
    val lyrics: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val audioQualityBadge: String
        get() {
            val fmt = format.uppercase()
            return when {
                bitDepth >= 24 || sampleRateHz >= 96000 -> "HI-RES • $fmt • ${bitDepth}-bit • ${sampleRateHz / 1000} kHz"
                sampleRateHz >= 44100 -> "$fmt • ${bitDepth}-bit • ${(sampleRateHz / 1000.0).let { if (it % 1.0 == 0.0) it.toInt() else it }} kHz"
                bitrateKbps > 0 -> "$fmt • ${bitrateKbps} kbps"
                else -> fmt
            }
        }
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: Uri?,
    val year: Int
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Genre(
    val name: String,
    val songCount: Int
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int
)
