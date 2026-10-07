package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val songId: Long,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1
)

@Entity(tableName = "song_metadata_cache")
data class SongMetadataCacheEntity(
    @PrimaryKey
    val songId: Long,
    val format: String,
    val bitrateKbps: Int,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val lyrics: String?,
    val lastModified: Long
)

@Entity(tableName = "loudness_gain_cache")
data class LoudnessGainCacheEntity(
    @PrimaryKey
    val songId: Long,
    val gainDb: Float,
    val peakDb: Float,
    val calculatedAt: Long = System.currentTimeMillis()
)
