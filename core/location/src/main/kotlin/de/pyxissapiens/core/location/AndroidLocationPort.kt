package de.pyxissapiens.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.ports.FixSample
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location port backed by the platform [LocationManager] (no Play Services dependency).
 * Emits GPS fixes when the runtime location permission has been granted.
 */
@Singleton
class AndroidLocationPort @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationPort {

    private val manager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override fun fixes(): Flow<FixSample> = callbackFlow {
        val lm = manager
        if (lm == null || !hasPermission()) {
            close()
            return@callbackFlow
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) { trySend(location.toFix()) }
            @Deprecated("Deprecated in API 29") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        runCatching {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener)
        }
        awaitClose { runCatching { lm.removeUpdates(listener) } }
    }

    @SuppressLint("MissingPermission")
    override suspend fun lastKnown(): FixSample? {
        val lm = manager ?: return null
        if (!hasPermission()) return null
        val location = runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            ?: runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        return location?.toFix()
    }

    private fun Location.toFix() = FixSample(
        timestampMillis = time,
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = if (hasAltitude()) altitude else null,
        accuracyMeters = if (hasAccuracy()) accuracy else null,
    )
}
