package de.pyxissapiens.feature.map

import android.content.Context
import org.maplibre.android.MapLibre

/**
 * Initialises the MapLibre native SDK.
 *
 * MapLibre requires [MapLibre.getInstance] to be called **before** any `MapView` is created
 * (otherwise `MapView` throws a `MapLibreConfigurationException`). Call this from
 * `Application.onCreate()` so the SDK is always ready before the first UI is composed.
 */
object MapInitializer {
    fun initialize(context: Context) {
        MapLibre.getInstance(context.applicationContext)
    }
}
