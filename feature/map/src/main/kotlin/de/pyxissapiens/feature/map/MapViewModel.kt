package de.pyxissapiens.feature.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.data.DataTypeRepository
import de.pyxissapiens.core.data.LineworkRepository
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.data.TrackRepository
import de.pyxissapiens.core.domain.model.DataType
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.Track
import de.pyxissapiens.core.ports.FixSample
import de.pyxissapiens.core.ports.LocationPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

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
    val editingLineworkId: String? = null,
    val showLinework: Boolean = true,
    val showTracks: Boolean = true,
    val styleUrl: String = DEFAULT_STYLE_URL,
    val activeMbtiles: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val measurements: MeasurementRepository,
    private val lineworks: LineworkRepository,
    private val tracks: TrackRepository,
    private val location: LocationPort,
    private val dataTypes: DataTypeRepository,
) : ViewModel() {

    val dataTypeList: StateFlow<List<DataType>> =
        dataTypes.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val lastFix = MutableStateFlow<GeoPoint?>(null)
    private val drawTool = MutableStateFlow(DrawTool.NONE)
    private val draft = MutableStateFlow<List<GeoPoint>>(emptyList())
    private val editingLinework = MutableStateFlow<String?>(null)
    private val showLinework = MutableStateFlow(true)
    private val showTracks = MutableStateFlow(true)
    private val styleUrl = MutableStateFlow(MapSettings.styleUrl(context))
    private val activeMbtiles = MutableStateFlow(MapSettings.activeMbtiles(context))
    private val message = MutableStateFlow<String?>(null)

    val messageState: StateFlow<String?> = message

    private val dataFlow = combine(
        measurements.observeAll(),
        lineworks.observeAll(),
        tracks.observeAll(),
    ) { m, l, t -> Triple(m, l, t) }

    val state: StateFlow<MapUiState> = combine(
        dataFlow,
        combine(styleUrl, activeMbtiles, lastFix) { s, a, f -> Triple(s, a, f) },
        combine(drawTool, draft, editingLinework) { t, d, e -> Triple(t, d, e) },
        combine(showLinework, showTracks) { a, b -> a to b },
    ) { (m, l, t), (style, active, fix), (tool, draftPts, editingId), (showLw, showTr) ->
        val activeTrack = t.firstOrNull { it.endedAt == null }
        MapUiState(
            measurements = m, lineworks = l, tracks = t,
            lastFix = fix,
            tracking = activeTrack != null,
            currentPoints = activeTrack?.points ?: emptyList(),
            distanceMeters = activeTrack?.distanceMeters ?: 0.0,
            drawTool = tool, draftPoints = draftPts, editingLineworkId = editingId,
            showLinework = showLw, showTracks = showTr,
            styleUrl = style, activeMbtiles = active,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    init { refreshLocation() }

    fun refreshLocation() {
        viewModelScope.launch { location.lastKnown()?.let { lastFix.value = it.toGeoPoint() } }
    }

    fun setStyleUrl(url: String) {
        MapSettings.setStyleUrl(context, url)
        styleUrl.value = MapSettings.styleUrl(context)
    }

    fun setActiveMbtiles(path: String?) {
        MapSettings.setActiveMbtiles(context, path)
        activeMbtiles.value = path
    }

    fun listMbtiles(): List<File> = MapSettings.listMbtiles(context)

    fun deleteMbtiles(file: File) {
        if (activeMbtiles.value == file.absolutePath) setActiveMbtiles(null)
        runCatching { file.delete() }
        message.value = "Karte gelöscht: ${file.name}"
    }

    fun setDrawTool(tool: DrawTool) {
        drawTool.value = tool
        if (tool == DrawTool.NONE) { draft.value = emptyList(); editingLinework.value = null }
    }

    fun toggleShowLinework() { showLinework.value = !showLinework.value }
    fun toggleShowTracks() { showTracks.value = !showTracks.value }
    fun clearMessage() { message.value = null }

    fun addDraftPoint(point: GeoPoint) { draft.value = draft.value + point }
    fun deleteLastDraftPoint() { draft.value = draft.value.dropLast(1) }
    fun cancelDraw() { draft.value = emptyList(); drawTool.value = DrawTool.NONE; editingLinework.value = null }

    fun editLinework(id: String) {
        val linework = state.value.lineworks.firstOrNull { it.id == id } ?: return
        editingLinework.value = id
        draft.value = linework.coordinates
        drawTool.value = if (linework.kind == LineworkKind.POLYGON) DrawTool.POLYGON else DrawTool.LINE
    }

    fun finishDraw() {
        val pts = draft.value
        val tool = drawTool.value
        if (pts.size < 2 || tool == DrawTool.NONE) { message.value = "Mindestens 2 Punkte nötig"; return }
        val now = System.currentTimeMillis()
        val kind = if (tool == DrawTool.POLYGON) LineworkKind.POLYGON else LineworkKind.CONTACT
        val coordinates = if (tool == DrawTool.POLYGON && pts.first() != pts.last()) pts + pts.first() else pts
        val existingId = editingLinework.value
        val linework = Linework(
            id = existingId ?: UUID.randomUUID().toString(),
            projectId = "default",
            kind = kind,
            coordinates = coordinates,
            createdAt = now, updatedAt = now,
        )
        viewModelScope.launch {
            lineworks.upsert(linework)
            draft.value = emptyList()
            drawTool.value = DrawTool.NONE
            editingLinework.value = null
            message.value = if (existingId == null) "Linienzug gespeichert" else "Linienzug aktualisiert"
        }
    }

    fun startTracking() {
        if (state.value.tracking) return
        val intent = Intent(context, TrackingService::class.java).setAction(TrackingService.ACTION_START)
        context.startForegroundService(intent)
        message.value = "Tracking gestartet"
    }

    fun stopTracking() {
        if (!state.value.tracking) return
        val intent = Intent(context, TrackingService::class.java).setAction(TrackingService.ACTION_STOP)
        context.startService(intent)
        message.value = "Tracking gestoppt"
    }

    fun importMbtiles(uri: Uri) {
        viewModelScope.launch {
            val dir = MapSettings.mbtilesDir(context).apply { mkdirs() }
            val target = File(dir, "map_${System.currentTimeMillis()}.mbtiles")
            val ok = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } != null
            }.getOrDefault(false)
            message.value = if (ok) "MBTiles importiert: ${target.name} (${MbtilesSource(target).tileCount()} Kacheln)" else "Import fehlgeschlagen"
        }
    }

    private fun FixSample.toGeoPoint() = GeoPoint(latitude, longitude, altitudeMeters, accuracyMeters)
}
