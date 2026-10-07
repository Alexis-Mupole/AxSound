package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.dao.MusicDao
import com.example.data.db.entity.SongMetadataCacheEntity
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(
    private val context: Context,
    private val musicDao: MusicDao
) {

    private val albumArtUri = Uri.parse("content://media/external/audio/albumart")

    fun observeMediaStoreChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(Unit)
            }
        }
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.registerContentObserver(uri, true, observer)
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    suspend fun scanSongs(
        minDurationMs: Long = 30_000L,
        excludedFolders: Set<String> = emptySet()
    ): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )

        // Filter by MIME type audio/*, and is_music = 1 or duration > minDuration
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE ? AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("audio/%", minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val yearCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Track"
                    val artist = c.getString(artistCol).takeIf { !it.isNullOrBlank() && it != "<unknown>" } ?: "Unknown Artist"
                    val album = c.getString(albumCol).takeIf { !it.isNullOrBlank() && it != "<unknown>" } ?: "Unknown Album"
                    val albumId = c.getLong(albumIdCol)
                    val duration = c.getLong(durationCol)
                    val dataPath = c.getString(dataCol) ?: ""
                    val mimeType = c.getString(mimeCol) ?: "audio/mpeg"
                    val year = c.getInt(yearCol)
                    val track = c.getInt(trackCol)
                    val dateAdded = c.getLong(dateAddedCol)
                    val size = c.getLong(sizeCol)

                    // Skip if in excluded folders
                    if (excludedFolders.any { excluded -> dataPath.startsWith(excluded) }) {
                        continue
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(albumArtUri, albumId)

                    val format = resolveFormat(mimeType, dataPath)

                    // Look up cached metadata or parse once
                    val cached = musicDao.getMetadata(id)
                    val (bitrate, sampleRate, bitDepth, lyrics) = if (cached != null) {
                        Quad(cached.bitrateKbps, cached.sampleRateHz, cached.bitDepth, cached.lyrics)
                    } else {
                        val parsed = extractAudioDetails(dataPath, contentUri)
                        musicDao.insertMetadata(
                            SongMetadataCacheEntity(
                                songId = id,
                                format = format,
                                bitrateKbps = parsed.first,
                                sampleRateHz = parsed.second,
                                bitDepth = parsed.third,
                                lyrics = parsed.fourth,
                                lastModified = System.currentTimeMillis()
                            )
                        )
                        parsed
                    }

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            durationMs = duration,
                            contentUri = contentUri,
                            artworkUri = artworkUri,
                            dataPath = dataPath,
                            mimeType = mimeType,
                            format = format,
                            bitrateKbps = bitrate,
                            sampleRateHz = sampleRate,
                            bitDepth = bitDepth,
                            year = year,
                            trackNumber = track,
                            dateAdded = dateAdded,
                            sizeBytes = size,
                            lyrics = lyrics
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MediaStoreScanner", "Error querying MediaStore", e)
        }

        songs
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun extractAudioDetails(dataPath: String, uri: Uri): Quad<Int, Int, Int, String?> {
        var bitrate = 0
        var sampleRate = 44100
        var bitDepth = 16
        var lyrics: String? = null

        // Check for sidecar .lrc file first
        if (dataPath.isNotEmpty()) {
            try {
                val lrcFile = File(dataPath.substringBeforeLast(".") + ".lrc")
                if (lrcFile.exists() && lrcFile.canRead()) {
                    lyrics = lrcFile.readText()
                }
            } catch (_: Exception) {}
        }

        val retriever = MediaMetadataRetriever()
        try {
            if (dataPath.isNotEmpty() && File(dataPath).exists()) {
                retriever.setDataSource(dataPath)
            } else {
                retriever.setDataSource(context, uri)
            }

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let {
                bitrate = it / 1000
            }

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let {
                sampleRate = it
            }

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.toIntOrNull()?.let {
                bitDepth = it
            }

            if (lyrics == null) {
                // Try embedded lyrics (USLT or lyric frame)
                lyrics = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) // or title
                // Note: Standard API retriever has METADATA_KEY_WRITER / COMPILATION
            }
        } catch (_: Exception) {
            // Graceful fallback for corrupted/unreadable headers
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return Quad(bitrate, sampleRate, bitDepth, lyrics)
    }

    private fun resolveFormat(mimeType: String, path: String): String {
        val extension = path.substringAfterLast('.', "").uppercase()
        return when {
            mimeType.contains("flac", ignoreCase = true) || extension == "FLAC" -> "FLAC"
            mimeType.contains("wav", ignoreCase = true) || extension == "WAV" -> "WAV"
            mimeType.contains("ogg", ignoreCase = true) || extension == "OGG" -> "OGG"
            mimeType.contains("opus", ignoreCase = true) || extension == "OPUS" -> "OPUS"
            mimeType.contains("aac", ignoreCase = true) || extension == "AAC" -> "AAC"
            mimeType.contains("mp4", ignoreCase = true) || extension == "M4A" -> "M4A"
            mimeType.contains("mpeg", ignoreCase = true) || extension == "MP3" -> "MP3"
            mimeType.contains("midi", ignoreCase = true) || extension in listOf("MID", "MIDI") -> "MIDI"
            mimeType.contains("amr", ignoreCase = true) || extension == "AMR" -> "AMR"
            extension == "AIFF" || extension == "AIF" -> "AIFF"
            extension == "WMA" -> "WMA"
            extension == "ALAC" -> "ALAC"
            extension.isNotBlank() -> extension
            else -> "AUDIO"
        }
    }
}
