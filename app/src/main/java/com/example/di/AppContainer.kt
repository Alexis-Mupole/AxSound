package com.example.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.example.data.db.PulseDatabase
import com.example.data.preferences.PreferencesRepository
import com.example.data.repository.MusicRepository
import com.example.data.scanner.MediaStoreScanner
import com.example.playback.MusicPlayerClient

@UnstableApi
class AppContainer(private val context: Context) {
    val database: PulseDatabase by lazy {
        PulseDatabase.getInstance(context)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(context)
    }

    val mediaStoreScanner: MediaStoreScanner by lazy {
        MediaStoreScanner(context, database.musicDao())
    }

    val musicRepository: MusicRepository by lazy {
        MusicRepository(mediaStoreScanner, database.musicDao(), preferencesRepository)
    }

    val playerClient: MusicPlayerClient by lazy {
        MusicPlayerClient(context)
    }
}
