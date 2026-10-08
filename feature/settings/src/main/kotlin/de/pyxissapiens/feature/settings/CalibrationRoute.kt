package de.pyxissapiens.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader
import java.util.Locale

@Composable
fun CalibrationRoute(
    modifier: Modifier = Modifier,
    viewModel: CalibrationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Kalibrierung")

        PyxisPanel {
            Text("Sensoren", style = MaterialTheme.typography.titleMedium)
            Text("Magnetometer: ${if (state.magnetometerAvailable) "vorhanden" else "fehlt"}", style = MaterialTheme.typography.bodySmall)
            Text("Gyroskop: ${if (state.gyroscopeAvailable) "vorhanden" else "fehlt"}", style = MaterialTheme.typography.bodySmall)
        }

        PyxisPanel {
            Text("Magnetometer (Hard-/Soft-Iron)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Das Gerät mehrmals in einer Acht drehen, dann abschließen. Gespeichert wird pro Gerät.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.measuring) {
                Text("Erfasste Proben: ${state.sampleCount}", style = MaterialTheme.typography.labelMedium)
                Button(onClick = { viewModel.stopMagCalibration() }) { Text("Kalibrierung abschließen") }
            } else {
                Button(onClick = { viewModel.startMagCalibration() }) { Text("Figur-8 starten") }
            }
            state.magnetometer?.let {
                Text(
                    "Offset (${fmt(it.offsetX)}, ${fmt(it.offsetY)}, ${fmt(it.offsetZ)}) µT · " +
                        "Qualität ${(it.quality * 100).toInt()} % · n=${it.sampleCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        PyxisPanel {
            Text("Auflagefläche / Bump", style = MaterialTheme.typography.titleMedium)
            Text(
                "Gerät flach auf eine ebene Referenz legen und die Korrektur setzen (kompensiert nicht-ebene Rückseiten).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { viewModel.captureTiltReference() }) { Text("Referenz setzen") }
            state.tilt?.let {
                Text("Auflageflächen-Korrektur aktiv", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }

        OutlinedButton(onClick = { viewModel.clearAll() }) { Text("Kalibrierung löschen") }

        state.message?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

private fun fmt(v: Double) = String.format(Locale.US, "%.1f", v)
