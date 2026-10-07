package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import com.example.data.model.Song
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PermissionRationale
import com.example.ui.screens.about.AlexisMupoleScreen
import com.example.ui.screens.appearance.AppearanceScreen
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.nowplaying.NowPlayingScreen
import com.example.ui.screens.queue.QueueBottomSheet
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.GradientThemes
import com.example.ui.theme.PulseWaveTheme
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val app = application as PulseApplication
        val container = app.container

        setContent {
            val userPrefs by container.preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val playerState by container.playerClient.playerState
                .collectAsStateWithLifecycle()

            val songs by container.musicRepository.songs
                .collectAsStateWithLifecycle()

            val favoriteIds by container.musicRepository.favoriteIds
                .collectAsStateWithLifecycle(initialValue = emptyList())

            val playlists by container.musicRepository.playlists
                .collectAsStateWithLifecycle(initialValue = emptyList())

            val recentlyPlayedIds by container.musicRepository.recentlyPlayedIds
                .collectAsStateWithLifecycle(initialValue = emptyList())

            val mostPlayedIds by container.musicRepository.mostPlayedIds
                .collectAsStateWithLifecycle(initialValue = emptyList())

            var hasAudioPermission by remember {
                mutableStateOf(checkAudioPermission())
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val audioGranted = results.entries.any {
                    it.key in listOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) && it.value
                }
                hasAudioPermission = audioGranted
                if (audioGranted) {
                    container.musicRepository.refreshSongs()
                }
            }

            LaunchedEffect(Unit) {
                if (!hasAudioPermission) {
                    val required = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        required.add(Manifest.permission.READ_MEDIA_AUDIO)
                        required.add(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        required.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    permissionLauncher.launch(required.toTypedArray())
                }

                // Handle intent if launched with audio file URI
                intent?.data?.let { uri ->
                    val path = uri.path ?: uri.toString()
                    val intentSong = Song(
                        id = System.currentTimeMillis(),
                        title = uri.lastPathSegment ?: "External Audio",
                        artist = "Unknown Artist",
                        album = "External",
                        albumId = 0L,
                        durationMs = 0L,
                        contentUri = uri,
                        artworkUri = null,
                        dataPath = path,
                        mimeType = intent.type ?: "audio/*",
                        format = "AUDIO"
                    )
                    container.playerClient.playSongList(listOf(intentSong), 0)
                }
            }

            val isDark = when (userPrefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.AMOLED -> true
            }
            val isAmoled = userPrefs.themeMode == ThemeMode.AMOLED

            val gradientPreset = remember(userPrefs.gradientThemeName) {
                GradientThemes.getByName(userPrefs.gradientThemeName)
            }

            val gradientBrush = remember(gradientPreset, isDark, isAmoled) {
                gradientPreset.getBrush(isDark = isDark, isAmoled = isAmoled)
            }

            PulseWaveTheme(
                themeMode = userPrefs.themeMode,
                accentHex = userPrefs.accentColorHex,
                useDynamicColor = userPrefs.useDynamicColor,
                cornerRoundnessDp = userPrefs.cornerRoundnessDp
            ) {
                if (!hasAudioPermission) {
                    PermissionRationale(
                        onRequestPermission = {
                            val required = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                required.add(Manifest.permission.READ_MEDIA_AUDIO)
                                required.add(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                required.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            permissionLauncher.launch(required.toTypedArray())
                        }
                    )
                } else {
                    PulseAppContent(
                        container = container,
                        userPrefs = userPrefs,
                        playerState = playerState,
                        songs = songs,
                        favoriteIds = favoriteIds,
                        playlists = playlists,
                        recentlyPlayedIds = recentlyPlayedIds,
                        mostPlayedIds = mostPlayedIds,
                        gradientBrush = gradientBrush
                    )
                }
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@UnstableApi
@Composable
private fun PulseAppContent(
    container: com.example.di.AppContainer,
    userPrefs: UserPreferences,
    playerState: com.example.playback.PulsePlayerState,
    songs: List<Song>,
    favoriteIds: List<Long>,
    playlists: List<com.example.data.db.entity.PlaylistEntity>,
    recentlyPlayedIds: List<Long>,
    mostPlayedIds: List<Long>,
    gradientBrush: Brush
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isNowPlayingExpanded by remember { mutableStateOf(false) }
    var isQueueOpen by remember { mutableStateOf(false) }

    // Intercept back press when Now Playing is expanded to collapse it smoothly
    BackHandler(enabled = isNowPlayingExpanded) {
        isNowPlayingExpanded = false
    }

    // Show error toast/snackbar if player encounters corrupt file
    LaunchedEffect(playerState.errorMessage) {
        playerState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(gradientBrush)
        ) {
            // Main Navigation Host
            NavHost(
                navController = navController,
                startDestination = "library",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("library") {
                    LibraryScreen(
                        songs = songs,
                        favoriteSongIds = favoriteIds,
                        playlists = playlists,
                        recentlyPlayedIds = recentlyPlayedIds,
                        mostPlayedIds = mostPlayedIds,
                        playerState = playerState,
                        playerClient = container.playerClient,
                        musicRepository = container.musicRepository,
                        userPreferences = userPrefs,
                        preferencesRepository = container.preferencesRepository,
                        onNavigateToSearch = { navController.navigate("search") },
                        onNavigateToEqualizer = { navController.navigate("equalizer") },
                        onNavigateToAppearance = { navController.navigate("appearance") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToAboutDeveloper = { navController.navigate("developer_profile") },
                        onToggleFavorite = { id, isFav ->
                            scope.launch { container.musicRepository.toggleFavorite(id, isFav) }
                        }
                    )
                }

                composable("search") {
                    SearchScreen(
                        allSongs = songs,
                        playerState = playerState,
                        playerClient = container.playerClient,
                        onNavigateBack = { navController.popBackStack() },
                        onToggleFavorite = { id, isFav ->
                            scope.launch { container.musicRepository.toggleFavorite(id, isFav) }
                        }
                    )
                }

                composable("equalizer") {
                    EqualizerScreen(
                        playerState = playerState,
                        playerClient = container.playerClient,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("appearance") {
                    AppearanceScreen(
                        userPreferences = userPrefs,
                        preferencesRepository = container.preferencesRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        userPreferences = userPrefs,
                        preferencesRepository = container.preferencesRepository,
                        musicRepository = container.musicRepository,
                        playerState = playerState,
                        playerClient = container.playerClient,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAboutDeveloper = { navController.navigate("developer_profile") }
                    )
                }

                composable("developer_profile") {
                    AlexisMupoleScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Persistent Mini Player docked above navigation bar
            if (playerState.currentSong != null && !isNowPlayingExpanded) {
                MiniPlayer(
                    playerState = playerState,
                    onExpand = { isNowPlayingExpanded = true },
                    onTogglePlayPause = { container.playerClient.togglePlayPause() },
                    onSkipNext = { container.playerClient.skipToNext() },
                    onSkipPrevious = { container.playerClient.skipToPrevious() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }

            // Fullscreen Now Playing Animated Transition
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    playerState = playerState,
                    playerClient = container.playerClient,
                    onDismiss = { isNowPlayingExpanded = false },
                    onNavigateToEqualizer = {
                        isNowPlayingExpanded = false
                        navController.navigate("equalizer")
                    },
                    onOpenQueue = { isQueueOpen = true },
                    onToggleFavorite = { id, isFav ->
                        scope.launch { container.musicRepository.toggleFavorite(id, isFav) }
                    },
                    gradientBrush = gradientPreset.brush
                )
            }

            // Queue Bottom Sheet
            if (isQueueOpen) {
                QueueBottomSheet(
                    playerState = playerState,
                    playerClient = container.playerClient,
                    onDismiss = { isQueueOpen = false }
                )
            }
        }
    }
}
