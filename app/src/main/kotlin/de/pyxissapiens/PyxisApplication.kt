package de.pyxissapiens

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.pyxissapiens.feature.map.MapInitializer

@HiltAndroidApp
class PyxisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // MapLibre must be initialised before any MapView is created.
        MapInitializer.initialize(this)
    }
}
