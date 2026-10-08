package de.pyxissapiens.feature.map

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import de.pyxissapiens.core.data.TrackRepository
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Track
import de.pyxissapiens.core.export.GpxExporter
import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Foreground service that records a GPS track while the app is in the background.
 * The in-progress track is written to the database continuously (so the map shows the live path)
 * and finalised (ended + GPX) when stopped.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var location: LocationPort
    @Inject lateinit var tracks: TrackRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var current: Track? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> {
                scope.launch {
                    stopRecording()
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun startRecording() {
        if (job != null) return
        startForegroundCompat()
        val now = System.currentTimeMillis()
        var track = Track(
            id = UUID.randomUUID().toString(),
            projectId = "default",
            startedAt = now,
            endedAt = null,
            points = emptyList(),
            distanceMeters = 0.0,
            durationMillis = 0,
        )
        current = track
        job = scope.launch {
            val points = ArrayList<GeoPoint>()
            var distance = 0.0
            location.fixes().collect { fix ->
                val point = GeoPoint(fix.latitude, fix.longitude, fix.altitudeMeters, fix.accuracyMeters)
                points.lastOrNull()?.let {
                    distance += GeoMath.haversineMeters(it.latitude, it.longitude, point.latitude, point.longitude)
                }
                points.add(point)
                track = track.copy(
                    points = points.toList(),
                    distanceMeters = distance,
                    durationMillis = System.currentTimeMillis() - now,
                )
                tracks.upsert(track)
            }
        }
    }

    private suspend fun stopRecording() {
        job?.cancel()
        job = null
        current?.let { track ->
            val finished = track.copy(endedAt = System.currentTimeMillis())
            tracks.upsert(finished)
            exportGpx(finished)
        }
        current = null
    }

    private fun exportGpx(track: Track) {
        runCatching {
            val dir = File(getExternalFilesDir(null) ?: filesDir, "exports").apply { mkdirs() }
            File(dir, "track_${track.id.take(8)}.gpx")
                .writeText(GpxExporter.export("PyxisSapiens Track", track.points))
        }
    }

    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Tracking", NotificationManager.IMPORTANCE_LOW),
                )
            }
        }
        val notification: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("PyxisSapiens")
                    .setContentText("GPS-Tracking aktiv")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
                    .setContentTitle("PyxisSapiens")
                    .setContentText("GPS-Tracking aktiv")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build()
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        scope.launch { stopRecording() }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "de.pyxissapiens.feature.map.TRACK_START"
        const val ACTION_STOP = "de.pyxissapiens.feature.map.TRACK_STOP"
        private const val CHANNEL_ID = "pyxis_tracking"
        private const val NOTIFICATION_ID = 4211
    }
}
