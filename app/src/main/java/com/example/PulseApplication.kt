package com.example

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.example.di.AppContainer

@UnstableApi
class PulseApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
