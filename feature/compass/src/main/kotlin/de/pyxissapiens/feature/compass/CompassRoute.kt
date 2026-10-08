package de.pyxissapiens.feature.compass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.domain.measure.ContactSurface
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.ui.component.ClinometerGauge
import de.pyxissapiens.core.ui.component.CompassFace
import de.pyxissapiens.core.ui.component.HoldButton
import de.pyxissapiens.core.ui.component.InterferenceMeter
import de.pyxissapiens.core.ui.component.NorthRefToggle
import de.pyxissapiens.core.ui.component.NotationToggle
import de.pyxissapiens.core.ui.component.NumericReadout
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.QualityBadge
import de.pyxissapiens.core.ui.component.StabilizationRing
import de.pyxissapiens.core.ui.component.StatusPill
import de.pyxissapiens.core.ui.theme.LocalPyxisTokens
import java.util.Locale

private val modeLabels = listOf("Fläche", "Linear", "Peilung")
private val contactLabels = listOf("Rückseite", "Bildschirm")

@Composable
fun CompassRoute(
    modifier: Modifier = Modifier,
    viewModel: CompassViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tokens = LocalPyxisTokens.current

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = "Sensor " + accuracyLabel(state.sensorAccuracy),
                color = if ((state.sensorAccuracy ?: 3) >= 3) tokens.ok else tokens.warn,
            )
            if (state.declinationDeg != null) {
                StatusPill(
                    text = "Dekl " + fmt(state.declinationDeg, signed = true) + "°",
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(Modifier.weight(1f))
            if (state.microTesla != null) {
                InterferenceMeter(
                    microTesla = state.microTesla!!,
                    expectedMicroTesla = state.expectedMicroTesla?.toFloat(),
                )
            }
        }

        // Mode
        NotationToggle(
            options = modeLabels,
            selectedIndex = state.mode.ordinal,
            onSelected = { viewModel.setMode(MeasurementKind.values()[it]) },
        )

        // Compass + clinometer
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            val azimuth = when (state.mode) {
                MeasurementKind.BEARING -> state.liveBearing
                MeasurementKind.LINE -> state.liveTrend
                MeasurementKind.PLANE -> state.liveDipDirection
            } ?: 0.0
            CompassFace(azimuthDeg = azimuth.toFloat(), modifier = Modifier.size(200.dp))
        }

        // Live readouts
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            when (state.mode) {
                MeasurementKind.PLANE -> {
                    if (state.liveDip != null) NumericReadout("DIP", fmt(state.liveDip), "°")
                    ClinometerGauge(dipDeg = (state.liveDip ?: 0.0).toFloat(), modifier = Modifier.size(90.dp))
                    if (state.liveDipDirection != null) NumericReadout("DIP-DIR", fmt(state.liveDipDirection), "°")
                }
                MeasurementKind.LINE -> {
                    if (state.liveTrend != null) NumericReadout("TREND", fmt(state.liveTrend), "°")
                    if (state.livePlunge != null) NumericReadout("PLUNGE", fmt(state.livePlunge), "°")
                }
                MeasurementKind.BEARING -> {
                    if (state.liveBearing != null) NumericReadout("PEILUNG", fmt(state.liveBearing), "°")
                }
            }
        }

        // Stability + quality preview
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StabilizationRing(
                progress = state.stability,
                modifier = Modifier.size(56.dp),
                label = "${state.sampleCount}",
            )
            Column {
                QualityBadge(score = (state.stability * 100).toInt())
                Text(
                    text = "Stabilität ${"%.0f".format(Locale.US, state.stability * 100)} %",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Held measurement or measure action
        val held = state.held
        if (held != null) {
            PyxisPanel {
                Text("Gehaltene Messung", style = MaterialTheme.typography.titleMedium)
                when (held.kind) {
                    MeasurementKind.PLANE ->
                        Text("Dip ${fmt(held.dip)}° / Dip-Dir ${fmt(held.dipDirection)}°", style = MaterialTheme.typography.headlineSmall)
                    MeasurementKind.LINE ->
                        Text("Trend ${fmt(held.trend)}° / Plunge ${fmt(held.plunge)}°", style = MaterialTheme.typography.headlineSmall)
                    MeasurementKind.BEARING ->
                        Text("Peilung ${fmt(held.bearing)}°", style = MaterialTheme.typography.headlineSmall)
                }
                QualityBadge(score = held.quality.score, uncertaintyDeg = held.quality.sigmaDeg)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HoldButton(text = "SPEICHERN", onClick = { viewModel.save() }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { viewModel.discard() }, modifier = Modifier.height(72.dp)) {
                        Text("Verwerfen")
                    }
                }
            }
        } else {
            HoldButton(text = "MESSEN", onClick = { viewModel.hold() })
        }

        Spacer(Modifier.weight(1f))

        // Reference + contact toggles
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NorthRefToggle(geographic = state.geographic, onValueChange = { viewModel.setGeographic(it) })
        }
        NotationToggle(
            options = contactLabels,
            selectedIndex = if (state.contact == ContactSurface.BACK_ON_PLANE) 0 else 1,
            onSelected = {
                viewModel.setContact(if (it == 0) ContactSurface.BACK_ON_PLANE else ContactSurface.SCREEN_ON_PLANE)
            },
        )
    }
}

private fun fmt(value: Double?, signed: Boolean = false): String {
    if (value == null) return "–"
    val pattern = if (signed) "%+.1f" else "%.1f"
    return String.format(Locale.US, pattern, value)
}

private fun accuracyLabel(accuracy: Int?): String = when (accuracy) {
    3 -> "gut"
    2 -> "mittel"
    1 -> "niedrig"
    0 -> "unzuverlässig"
    else -> "?"
}
