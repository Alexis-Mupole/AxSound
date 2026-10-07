package com.example.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.db.entity.PlaylistEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Song
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.SortOption
import com.example.data.preferences.UserPreferences
import com.example.data.repository.MusicRepository
import com.example.playback.MusicPlayerClient
import com.example.playback.PulsePlayerState
import com.example.ui.components.EmptyLibraryState
import com.example.ui.components.SongItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    favoriteSongIds: List<Long>,
    playlists: List<PlaylistEntity>,
    recentlyPlayedIds: List<Long>,
    mostPlayedIds: List<Long>,
    playerState: PulsePlayerState,
    playerClient: MusicPlayerClient,
    musicRepository: MusicRepository,
    userPreferences: UserPreferences,
    preferencesRepository: PreferencesRepository,
    onNavigateToSearch: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAboutDeveloper: () -> Unit = {},
    onToggleFavorite: (Long, Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Artists", "Folders", "Playlists", "Favorites", "Recent", "Top")

    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Playlist song picker
    var selectedPlaylistForView by remember { mutableStateOf<PlaylistEntity?>(null) }
    var selectedFolderForView by remember { mutableStateOf<Folder?>(null) }
    var selectedAlbumForView by remember { mutableStateOf<Album?>(null) }
    var selectedArtistForView by remember { mutableStateOf<Artist?>(null) }

    val albums = remember(songs) { musicRepository.getAlbums(songs) }
    val artists = remember(songs) { musicRepository.getArtists(songs) }
    val folders = remember(songs) { musicRepository.getFolders(songs) }

    val favoriteSongs = remember(songs, favoriteSongIds) {
        val favSet = favoriteSongIds.toSet()
        songs.filter { favSet.contains(it.id) }
    }

    val recentSongs = remember(songs, recentlyPlayedIds) {
        val idMap = songs.associateBy { it.id }
        recentlyPlayedIds.mapNotNull { idMap[it] }
    }

    val topSongs = remember(songs, mostPlayedIds) {
        val idMap = songs.associateBy { it.id }
        mostPlayedIds.mapNotNull { idMap[it] }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AxSound",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = onNavigateToEqualizer) {
                    Icon(
                        Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = if (playerState.volumeBoostPercent > 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onNavigateToAppearance) {
                    Icon(Icons.Default.ColorLens, contentDescription = "Themes & Appearance")
                }
                IconButton(onClick = onNavigateToAboutDeveloper) {
                    Icon(Icons.Default.Person, contentDescription = "About Developer: Alexis Mupole")
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )

        // Navigation Tabs (Songs, Albums, Artists, Folders, Playlists, Favorites, Recent, Top)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        selectedPlaylistForView = null
                        selectedFolderForView = null
                        selectedAlbumForView = null
                        selectedArtistForView = null
                    },
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> {
                    // Songs Tab
                    if (songs.isEmpty()) {
                        EmptyLibraryState(onRescan = { musicRepository.refreshSongs() })
                    } else {
                        Column {
                            // Sort and total info bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${songs.size} songs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box {
                                    TextButton(onClick = { sortMenuExpanded = true }) {
                                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sort: ${userPreferences.sortOption.name}")
                                    }

                                    DropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false }
                                    ) {
                                        SortOption.values().forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option.name) },
                                                onClick = {
                                                    scope.launch {
                                                        preferencesRepository.setSort(
                                                            option,
                                                            if (userPreferences.sortOption == option) !userPreferences.sortAscending else true
                                                        )
                                                    }
                                                    sortMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(items = songs, key = { it.id }) { song ->
                                    val isCurrent = song.id == playerState.currentSong?.id
                                    SongItem(
                                        song = song,
                                        isPlaying = playerState.isPlaying && isCurrent,
                                        isCurrentTrack = isCurrent,
                                        onClick = {
                                            playerClient.playSongList(songs, songs.indexOf(song))
                                        },
                                        onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                        onPlayNext = { /* Play Next */ },
                                        onAddToQueue = { /* Add to Queue */ },
                                        onAddToPlaylist = { /* Add to Playlist */ }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Albums Tab
                    if (selectedAlbumForView != null) {
                        val album = selectedAlbumForView!!
                        val albumSongs = remember(songs, album) { songs.filter { it.albumId == album.id } }
                        SubListHeader(title = album.title, subtitle = "${album.artist} • ${albumSongs.size} tracks", onBack = { selectedAlbumForView = null }) {
                            LazyColumn {
                                items(items = albumSongs, key = { it.id }) { song ->
                                    val isCurrent = song.id == playerState.currentSong?.id
                                    SongItem(
                                        song = song,
                                        isPlaying = playerState.isPlaying && isCurrent,
                                        isCurrentTrack = isCurrent,
                                        onClick = { playerClient.playSongList(albumSongs, albumSongs.indexOf(song)) },
                                        onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                        onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                    )
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(albums) { album ->
                                AlbumCard(album = album, onClick = { selectedAlbumForView = album })
                            }
                        }
                    }
                }
                2 -> {
                    // Artists Tab
                    if (selectedArtistForView != null) {
                        val artist = selectedArtistForView!!
                        val artistSongs = remember(songs, artist) { songs.filter { it.artist.equals(artist.name, ignoreCase = true) } }
                        SubListHeader(title = artist.name, subtitle = "${artistSongs.size} tracks", onBack = { selectedArtistForView = null }) {
                            LazyColumn {
                                items(items = artistSongs, key = { it.id }) { song ->
                                    val isCurrent = song.id == playerState.currentSong?.id
                                    SongItem(
                                        song = song,
                                        isPlaying = playerState.isPlaying && isCurrent,
                                        isCurrentTrack = isCurrent,
                                        onClick = { playerClient.playSongList(artistSongs, artistSongs.indexOf(song)) },
                                        onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                        onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(artists) { artist ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedArtistForView = artist }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(artist.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Text("${artist.songCount} songs • ${artist.albumCount} albums", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Folders Tab
                    if (selectedFolderForView != null) {
                        val folder = selectedFolderForView!!
                        val folderSongs = remember(songs, folder) { songs.filter { it.dataPath.startsWith(folder.path) } }
                        SubListHeader(title = folder.name, subtitle = "${folderSongs.size} tracks", onBack = { selectedFolderForView = null }) {
                            LazyColumn {
                                items(items = folderSongs, key = { it.id }) { song ->
                                    val isCurrent = song.id == playerState.currentSong?.id
                                    SongItem(
                                        song = song,
                                        isPlaying = playerState.isPlaying && isCurrent,
                                        isCurrentTrack = isCurrent,
                                        onClick = { playerClient.playSongList(folderSongs, folderSongs.indexOf(song)) },
                                        onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                        onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(folders) { folder ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedFolderForView = folder }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(folder.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Text("${folder.songCount} songs • ${folder.path}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Playlists Tab
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (playlists.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Playlists Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Create your first custom playlist to group favorite tunes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(playlists) { playlist ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(playlist.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("Custom Playlist", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            IconButton(onClick = { scope.launch { musicRepository.deletePlaylist(playlist.id) } }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                                .testTag("create_playlist_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Playlist")
                        }
                    }
                }
                5 -> {
                    // Favorites Tab
                    if (favoriteSongs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No Favorite Songs Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Tap the heart icon on any song to add it here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = favoriteSongs, key = { it.id }) { song ->
                                val isCurrent = song.id == playerState.currentSong?.id
                                SongItem(
                                    song = song,
                                    isPlaying = playerState.isPlaying && isCurrent,
                                    isCurrentTrack = isCurrent,
                                    onClick = { playerClient.playSongList(favoriteSongs, favoriteSongs.indexOf(song)) },
                                    onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                    onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                )
                            }
                        }
                    }
                }
                6 -> {
                    // Recently Played
                    if (recentSongs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No Playback History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = recentSongs, key = { it.id }) { song ->
                                val isCurrent = song.id == playerState.currentSong?.id
                                SongItem(
                                    song = song,
                                    isPlaying = playerState.isPlaying && isCurrent,
                                    isCurrentTrack = isCurrent,
                                    onClick = { playerClient.playSongList(recentSongs, recentSongs.indexOf(song)) },
                                    onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                    onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                )
                            }
                        }
                    }
                }
                7 -> {
                    // Top Played
                    if (topSongs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No Most Played Tracks Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = topSongs, key = { it.id }) { song ->
                                val isCurrent = song.id == playerState.currentSong?.id
                                SongItem(
                                    song = song,
                                    isPlaying = playerState.isPlaying && isCurrent,
                                    isCurrentTrack = isCurrent,
                                    onClick = { playerClient.playSongList(topSongs, topSongs.indexOf(song)) },
                                    onToggleFavorite = { onToggleFavorite(song.id, song.isFavorite) },
                                    onPlayNext = {}, onAddToQueue = {}, onAddToPlaylist = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            scope.launch { musicRepository.createPlaylist(newPlaylistName.trim()) }
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SubListHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
private fun AlbumCard(album: Album, onClick: () -> Unit) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(album.artworkUri ?: R.drawable.default_album_art)
                    .error(R.drawable.default_album_art)
                    .size(240, 240)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.artist} • ${album.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
