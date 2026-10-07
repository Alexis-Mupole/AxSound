package com.example.data.repository

import com.example.data.db.dao.MusicDao
import com.example.data.db.entity.FavoriteSongEntity
import com.example.data.db.entity.PlaylistEntity
import com.example.data.db.entity.PlaylistItemEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Genre
import com.example.data.model.Song
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.SortOption
import com.example.data.scanner.MediaStoreScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(
    private val scanner: MediaStoreScanner,
    private val musicDao: MusicDao,
    private val preferencesRepository: PreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    val rawSongs: StateFlow<List<Song>> = _rawSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteIds: Flow<List<Long>> = musicDao.getAllFavoriteSongIds()
    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    val recentlyPlayedIds: Flow<List<Long>> = musicDao.getRecentlyPlayedSongIds()
    val mostPlayedIds: Flow<List<Long>> = musicDao.getMostPlayedSongIds()

    // Combined songs with favorite state
    val songs: StateFlow<List<Song>> = combine(
        _rawSongs,
        favoriteIds,
        preferencesRepository.userPreferencesFlow
    ) { raw, favIds, prefs ->
        val favSet = favIds.toSet()
        val mapped = raw.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }

        sortSongs(mapped, prefs.sortOption, prefs.sortAscending)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        // Initial scan
        refreshSongs()

        // Auto rescan when MediaStore emits change
        scope.launch {
            scanner.observeMediaStoreChanges().collect {
                refreshSongs()
            }
        }
    }

    fun refreshSongs() {
        scope.launch {
            _isLoading.value = true
            try {
                val scanned = scanner.scanSongs()
                _rawSongs.value = scanned
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun sortSongs(list: List<Song>, sortOption: SortOption, ascending: Boolean): List<Song> {
        val comparator = when (sortOption) {
            SortOption.TITLE -> compareBy<Song> { it.title.lowercase() }
            SortOption.ARTIST -> compareBy<Song> { it.artist.lowercase() }
            SortOption.ALBUM -> compareBy<Song> { it.album.lowercase() }
            SortOption.DATE_ADDED -> compareBy<Song> { it.dateAdded }
            SortOption.DURATION -> compareBy<Song> { it.durationMs }
        }
        return if (ascending) list.sortedWith(comparator) else list.sortedWith(comparator.reversed())
    }

    fun getAlbums(songsList: List<Song>): List<Album> {
        return songsList.groupBy { it.albumId }.map { (_, albumSongs) ->
            val first = albumSongs.first()
            Album(
                id = first.albumId,
                title = first.album,
                artist = first.artist,
                songCount = albumSongs.size,
                artworkUri = first.artworkUri,
                year = first.year
            )
        }.sortedBy { it.title.lowercase() }
    }

    fun getArtists(songsList: List<Song>): List<Artist> {
        return songsList.groupBy { it.artist.lowercase() }.map { (_, artistSongs) ->
            val first = artistSongs.first()
            val albumsCount = artistSongs.map { it.albumId }.distinct().size
            Artist(
                name = first.artist,
                songCount = artistSongs.size,
                albumCount = albumsCount
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun getGenres(songsList: List<Song>): List<Genre> {
        return songsList.groupBy { it.genre.lowercase() }.map { (_, genreSongs) ->
            Genre(
                name = genreSongs.first().genre,
                songCount = genreSongs.size
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun getFolders(songsList: List<Song>): List<Folder> {
        return songsList.groupBy {
            try {
                File(it.dataPath).parent ?: "Root"
            } catch (_: Exception) {
                "Root"
            }
        }.map { (dirPath, folderSongs) ->
            val name = try {
                File(dirPath).name.ifBlank { dirPath }
            } catch (_: Exception) {
                dirPath
            }
            Folder(
                path = dirPath,
                name = name,
                songCount = folderSongs.size
            )
        }.sortedBy { it.name.lowercase() }
    }

    suspend fun toggleFavorite(songId: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (currentIsFavorite) {
            musicDao.deleteFavorite(songId)
        } else {
            musicDao.insertFavorite(FavoriteSongEntity(songId = songId))
        }
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        musicDao.recordSongPlayed(songId)
    }

    // Playlists
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        musicDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        musicDao.renamePlaylist(playlistId, newName.trim())
    }

    fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>> {
        return musicDao.getPlaylistSongIds(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        musicDao.insertPlaylistItem(PlaylistItemEntity(playlistId = playlistId, songId = songId, orderIndex = 0))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }
}
