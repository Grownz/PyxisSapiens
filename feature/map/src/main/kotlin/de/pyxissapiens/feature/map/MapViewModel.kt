package de.pyxissapiens.feature.map

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.data.LineworkRepository
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.data.TrackRepository
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.Track
import de.pyxissapiens.core.ports.FixSample
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class DrawTool { NONE, LINE, POLYGON }

const val DEFAULT_STYLE_URL = "https://demotiles.maplibre.org/style.json"

data class MapUiState(
    val measurements: List<Measurement> = emptyList(),
    val lineworks: List<Linework> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val lastFix: GeoPoint? = null,
    val tracking: Boolean = false,
    val currentPoints: List<GeoPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val drawTool: DrawTool = DrawTool.NONE,
    val draftPoints: List<GeoPoint> = emptyList(),
    val showLinework: Boolean = true,
    val showTracks: Boolean = true,
    val styleUrl: String = DEFAULT_STYLE_URL,
    val message: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val measurements: MeasurementRepository,
    private val lineworks: LineworkRepository,
    private val tracks: TrackRepository,
    private val location: LocationPort,
) : ViewModel() {

    private val styleUrl = MutableStateFlow(DEFAULT_STYLE_URL)
    private val lastFix = MutableStateFlow<GeoPoint?>(null)
    private val tracking = MutableStateFlow(false)
    private val currentPoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    private val distance = MutableStateFlow(0.0)
    private val drawTool = MutableStateFlow(DrawTool.NONE)
    private val draft = MutableStateFlow<List<GeoPoint>>(emptyList())
    private val showLinework = MutableStateFlow(true)
    private val showTracks = MutableStateFlow(true)
    private val message = MutableStateFlow<String?>(null)

    private var trackJob: Job? = null

    private val dataFlow = combine(
        measurements.observeAll(),
        lineworks.observeAll(),
        tracks.observeAll(),
    ) { m, l, t -> Triple(m, l, t) }

    val state: StateFlow<MapUiState> = combine(
        dataFlow,
        combine(styleUrl, lastFix) { s, f -> s to f },
        combine(tracking, currentPoints, distance) { a, b, c -> Triple(a, b, c) },
        combine(drawTool, draft) { a, b -> a to b },
        combine(showLinework, showTracks) { a, b -> a to b },
    ) { (m, l, t), (style, fix), (trackingOn, pts, dist), (tool, draftPts), (showLw, showTr) ->
        MapUiState(
            measurements = m, lineworks = l, tracks = t,
            lastFix = fix, tracking = trackingOn, currentPoints = pts, distanceMeters = dist,
            drawTool = tool, draftPoints = draftPts,
            showLinework = showLw, showTracks = showTr,
            styleUrl = style, message = null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    // message is exposed separately to avoid re-render churn
    val messageState: StateFlow<String?> = message

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            location.lastKnown()?.let { lastFix.value = it.toGeoPoint() }
        }
    }

    fun setStyleUrl(url: String) { styleUrl.value = url.ifBlank { DEFAULT_STYLE_URL } }
    fun setDrawTool(tool: DrawTool) { drawTool.value = tool; draft.value = emptyList() }
    fun toggleShowLinework() { showLinework.value = !showLinework.value }
    fun toggleShowTracks() { showTracks.value = !showTracks.value }
    fun clearMessage() { message.value = null }
    fun addDraftPoint(point: GeoPoint) { draft.value = draft.value + point }
    fun cancelDraw() { draft.value = emptyList(); drawTool.value = DrawTool.NONE }

    fun finishDraw() {
        val pts = draft.value
        val tool = drawTool.value
        if (pts.size < 2 || tool == DrawTool.NONE) {
            message.value = "Mindestens 2 Punkte nötig"
            return
        }
        val now = System.currentTimeMillis()
        val kind = if (tool == DrawTool.POLYGON) LineworkKind.POLYGON else LineworkKind.CONTACT
        val coordinates = if (tool == DrawTool.POLYGON && pts.first() != pts.last()) pts + pts.first() else pts
        val linework = Linework(
            id = UUID.randomUUID().toString(),
            projectId = "default",
            kind = kind,
            coordinates = coordinates,
            createdAt = now, updatedAt = now,
        )
        viewModelScope.launch {
            lineworks.upsert(linework)
            draft.value = emptyList()
            drawTool.value = DrawTool.NONE
            message.value = "Linienzug gespeichert (${coordinates.size} Punkte)"
        }
    }

    fun startTracking() {
        if (tracking.value) return
        tracking.value = true
        currentPoints.value = emptyList()
        distance.value = 0.0
        trackJob?.cancel()
        trackJob = viewModelScope.launch {
            location.fixes().collect { fix ->
                val point = GeoPoint(fix.latitude, fix.longitude, fix.altitudeMeters, fix.accuracyMeters)
                lastFix.value = point
                val prev = currentPoints.value.lastOrNull()
                if (prev != null) distance.value += haversine(prev, point)
                currentPoints.value = currentPoints.value + point
            }
        }
        message.value = "Tracking gestartet"
    }

    fun stopTracking() {
        if (!tracking.value) return
        tracking.value = false
        trackJob?.cancel()
        trackJob = null
        val pts = currentPoints.value
        if (pts.size < 2) {
            message.value = "Keine ausreichenden Punkte aufgezeichnet"
            return
        }
        val now = System.currentTimeMillis()
        val track = Track(
            id = UUID.randomUUID().toString(),
            projectId = "default",
            startedAt = now - (pts.size * 1000L),
            endedAt = now,
            points = pts,
            distanceMeters = distance.value,
            durationMillis = pts.size * 1000L,
        )
        viewModelScope.launch {
            tracks.upsert(track)
            currentPoints.value = emptyList()
            distance.value = 0.0
            message.value = "Track gespeichert: ${String.format(java.util.Locale.US, "%.0f", track.distanceMeters)} m"
        }
    }

    fun importMbtiles(uri: Uri) {        viewModelScope.launch {
            val dir = File(context.filesDir, "maps").apply { mkdirs() }
            val target = File(dir, "import_${System.currentTimeMillis()}.mbtiles")
            val ok = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } != null
            }.getOrDefault(false)
            message.value = if (ok) "MBTiles importiert: ${target.name} (${MbtilesSource(target).tileCount()} Kacheln)" else "Import fehlgeschlagen"
        }
    }

    fun exportLastTrackGpx() {
        val track = state.value.tracks.firstOrNull()
        if (track == null) { message.value = "Kein Track vorhanden"; return }
        viewModelScope.launch {
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "exports").apply { mkdirs() }
            val file = File(dir, "track_${track.id.take(8)}.gpx")
            file.writeText(de.pyxissapiens.core.export.GpxExporter.export("PyxisSapiens Track", track.points))
            message.value = "GPX exportiert: ${file.name}"
        }
    }

    private fun FixSample.toGeoPoint() = GeoPoint(latitude, longitude, altitudeMeters, accuracyMeters)

    private fun haversine(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * asin(sqrt(h))
    }
}
