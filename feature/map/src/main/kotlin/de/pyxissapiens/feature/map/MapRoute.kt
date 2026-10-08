package de.pyxissapiens.feature.map

import android.Manifest
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.domain.model.Track
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun MapRoute(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.messageState.collectAsStateWithLifecycle()
    val dataTypes by viewModel.dataTypeList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedType by remember { mutableStateOf<String?>(null) }
    val shownMeasurements = state.measurements.filter { selectedType == null || it.typeId == selectedType }

    var styleReady by remember { mutableStateOf(false) }
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }
    val drawToolRef = remember { mutableStateOf(DrawTool.NONE) }
    val mapView = remember { MapView(context).apply { onCreate(null) } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.refreshLocation()
    }
    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    // Keep the click handler's draw-tool reference current.
    LaunchedEffect(state.drawTool) { drawToolRef.value = state.drawTool }

    // Initialise MapLibre and the style.
    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
        mapView.getMapAsync { map ->
            mapRef.value = map
            map.setStyle(Style.Builder().fromUri(state.styleUrl)) { style ->
                ensureLayers(style)
                styleReady = true
            }
            map.addOnMapClickListener { latLng ->
                if (drawToolRef.value != DrawTool.NONE) {
                    viewModel.addDraftPoint(GeoPoint(latLng.latitude, latLng.longitude, null, null))
                    true
                } else {
                    false
                }
            }
        }
    }

    // Lifecycle forwarding to the MapView.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    // Data-driven overlays.
    LaunchedEffect(styleReady, shownMeasurements, state.lineworks, state.tracks, state.draftPoints, state.showLinework, state.showTracks) {
        val map = mapRef.value ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        (style.getSource("measurements") as? GeoJsonSource)?.setGeoJson(measurementsGeoJson(shownMeasurements))
        if (state.showLinework) {
            (style.getSource("linework") as? GeoJsonSource)?.setGeoJson(lineworkGeoJson(state.lineworks))
        } else {
            (style.getSource("linework") as? GeoJsonSource)?.setGeoJson(emptyFeatureCollection())
        }
        val trackFeatures = if (state.showTracks) state.tracks else emptyList()
        (style.getSource("tracks") as? GeoJsonSource)?.setGeoJson(tracksGeoJson(trackFeatures))
        (style.getSource("draft") as? GeoJsonSource)?.setGeoJson(draftGeoJson(state.draftPoints))
    }

    Box(modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Column(
            Modifier.align(androidx.compose.ui.Alignment.TopCenter).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = {
                    val fix = state.lastFix
                    if (fix != null) mapRef.value?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(fix.latitude, fix.longitude), 15.0),
                    )
                }) { Text("Standort") }
                Button(onClick = { if (state.tracking) viewModel.stopTracking() else viewModel.startTracking() }) {
                    Text(if (state.tracking) "Stop" else "Tracking")
                }
                OutlinedButton(onClick = { viewModel.exportLastTrackGpx() }) { Text("GPX") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = state.drawTool == DrawTool.LINE, onClick = { viewModel.setDrawTool(DrawTool.LINE) }, label = { Text("Linie") })
                FilterChip(selected = state.drawTool == DrawTool.POLYGON, onClick = { viewModel.setDrawTool(DrawTool.POLYGON) }, label = { Text("Polygon") })
                if (state.drawTool != DrawTool.NONE) {
                    Button(onClick = { viewModel.finishDraw() }) { Text("Fertig") }
                    OutlinedButton(onClick = { viewModel.cancelDraw() }) { Text("Abbruch") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = state.showLinework, onClick = { viewModel.toggleShowLinework() }, label = { Text("Linework") })
                FilterChip(selected = state.showTracks, onClick = { viewModel.toggleShowTracks() }, label = { Text("Tracks") })
            }
            if (dataTypes.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = selectedType == null, onClick = { selectedType = null }, label = { Text("Alle Typen") })
                    dataTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type.id,
                            onClick = { selectedType = if (selectedType == type.id) null else type.id },
                            label = { Text(type.name) },
                        )
                    }
                }
            }
        }

        Column(
            Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.tracking) {
                Text(
                    "Tracking: ${String.format(java.util.Locale.US, "%.0f", state.distanceMeters)} m · ${state.currentPoints.size} Punkte",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (state.drawTool != DrawTool.NONE) {
                Text("Zeichnen: ${state.draftPoints.size} Punkte – auf die Karte tippen", style = MaterialTheme.typography.labelMedium)
            }
            message?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

private fun ensureLayers(style: Style) {
    if (style.getSource("measurements") == null) {
        style.addSource(GeoJsonSource("measurements", emptyFeatureCollection()))
        style.addLayer(
            CircleLayer("measurements-layer", "measurements").withProperties(
                PropertyFactory.circleRadius(6f),
                PropertyFactory.circleColor(AndroidColor.rgb(255, 176, 0)),
                PropertyFactory.circleStrokeWidth(1.5f),
                PropertyFactory.circleStrokeColor(AndroidColor.BLACK),
            ),
        )
    }
    if (style.getSource("linework") == null) {
        style.addSource(GeoJsonSource("linework", emptyFeatureCollection()))
        style.addLayer(
            LineLayer("linework-layer", "linework").withProperties(
                PropertyFactory.lineColor(AndroidColor.rgb(78, 161, 255)),
                PropertyFactory.lineWidth(3f),
            ),
        )
    }
    if (style.getSource("tracks") == null) {
        style.addSource(GeoJsonSource("tracks", emptyFeatureCollection()))
        style.addLayer(
            LineLayer("tracks-layer", "tracks").withProperties(
                PropertyFactory.lineColor(AndroidColor.rgb(55, 214, 122)),
                PropertyFactory.lineWidth(3f),
            ),
        )
    }
    if (style.getSource("draft") == null) {
        style.addSource(GeoJsonSource("draft", emptyFeatureCollection()))
        style.addLayer(
            LineLayer("draft-layer", "draft").withProperties(
                PropertyFactory.lineColor(AndroidColor.rgb(255, 92, 92)),
                PropertyFactory.lineWidth(3f),
            ),
        )
    }
}

private fun emptyFeatureCollection() = """{"type":"FeatureCollection","features":[]}"""

private fun measurementsGeoJson(measurements: List<Measurement>): String {
    val features = measurements.mapNotNull { m ->
        val loc = m.location ?: return@mapNotNull null
        val color = when (m.kind) {
            MeasurementKind.PLANE -> "#FFB000"
            MeasurementKind.LINE -> "#37D67A"
            MeasurementKind.BEARING -> "#FF5C5C"
        }
        """{"type":"Feature","geometry":{"type":"Point","coordinates":[${loc.longitude},${loc.latitude}]},"properties":{"color":"$color"}}"""
    }
    return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
}

private fun lineworkGeoJson(lineworks: List<Linework>): String {
    val features = lineworks.mapNotNull { lw ->
        if (lw.coordinates.size < 2) return@mapNotNull null
        val ring = lw.coordinates.joinToString(",") { "[${it.longitude},${it.latitude}]" }
        val geometry = if (lw.kind == LineworkKind.POLYGON) {
            """{"type":"Polygon","coordinates":[[$ring]]}"""
        } else {
            """{"type":"LineString","coordinates":[$ring]}"""
        }
        """{"type":"Feature","geometry":$geometry,"properties":{"kind":"${lw.kind.name}"}}"""
    }
    return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
}

private fun tracksGeoJson(tracks: List<Track>): String {
    val features = tracks.mapNotNull { t ->
        if (t.points.size < 2) return@mapNotNull null
        val line = t.points.joinToString(",") { "[${it.longitude},${it.latitude}]" }
        """{"type":"Feature","geometry":{"type":"LineString","coordinates":[$line]},"properties":{}}"""
    }
    return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
}

private fun draftGeoJson(points: List<GeoPoint>): String {
    if (points.size < 2) return emptyFeatureCollection()
    val line = points.joinToString(",") { "[${it.longitude},${it.latitude}]" }
    return """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"LineString","coordinates":[$line]},"properties":{}}]}"""
}
