package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.db.entity.FavoriteSongEntity
import com.example.data.db.entity.LoudnessGainCacheEntity
import com.example.data.db.entity.PlaybackHistoryEntity
import com.example.data.db.entity.PlaylistEntity
import com.example.data.db.entity.PlaylistItemEntity
import com.example.data.db.entity.SongMetadataCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // Favorites
    @Query("SELECT songId FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    // Playlist Items
    @Query("SELECT songId FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    // Playback History & Most Played
    @Query("SELECT songId FROM playback_history ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayedSongIds(limit: Int = 50): Flow<List<Long>>

    @Query("SELECT songId FROM playback_history ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedSongIds(limit: Int = 50): Flow<List<Long>>

    @Query("SELECT playCount FROM playback_history WHERE songId = :songId")
    suspend fun getPlayCount(songId: Long): Int?

    @Transaction
    suspend fun recordSongPlayed(songId: Long) {
        val current = getPlayCount(songId)
        if (current == null) {
            insertPlaybackHistory(PlaybackHistoryEntity(songId = songId, lastPlayedAt = System.currentTimeMillis(), playCount = 1))
        } else {
            updatePlaybackHistory(songId = songId, lastPlayedAt = System.currentTimeMillis(), playCount = current + 1)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybackHistory(history: PlaybackHistoryEntity)

    @Query("UPDATE playback_history SET lastPlayedAt = :lastPlayedAt, playCount = :playCount WHERE songId = :songId")
    suspend fun updatePlaybackHistory(songId: Long, lastPlayedAt: Long, playCount: Int)

    // Song Metadata Cache (for fast offline loading & low CPU usage)
    @Query("SELECT * FROM song_metadata_cache WHERE songId = :songId")
    suspend fun getMetadata(songId: Long): SongMetadataCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: SongMetadataCacheEntity)

    // Loudness Gain Cache
    @Query("SELECT * FROM loudness_gain_cache WHERE songId = :songId")
    suspend fun getLoudnessGain(songId: Long): LoudnessGainCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoudnessGain(gain: LoudnessGainCacheEntity)
}
