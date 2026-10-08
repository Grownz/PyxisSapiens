package de.pyxissapiens.feature.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.ui.component.EmptyState
import de.pyxissapiens.core.ui.component.MeasurementListItem
import de.pyxissapiens.core.ui.component.SectionHeader
import de.pyxissapiens.core.ui.icon.PyxisIcons
import java.util.Locale

@Composable
fun MeasurementsRoute(
    modifier: Modifier = Modifier,
    viewModel: MeasurementsViewModel = hiltViewModel(),
) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var historyVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader("Messungen (${measurements.size})", modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Rückgängig")
            }
            IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Wiederherstellen")
            }
        }
        message?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }

        if (measurements.isEmpty()) {
            EmptyState(
                title = "Keine Messungen",
                subtitle = "Erfasse im Reiter „Messen\" die erste Messung.",
                icon = PyxisIcons.StrikeDip,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(measurements, key = { it.id }) { measurement ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MeasurementListItem(
                            symbol = symbolFor(measurement.kind),
                            primary = primaryText(measurement),
                            secondary = secondaryText(measurement),
                            qualityScore = measurement.quality?.score,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.showHistory(measurement.id)
                                historyVisible = true
                            },
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { viewModel.delete(measurement.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                        }
                    }
                }
            }
        }
    }

    if (historyVisible) {
        AlertDialog(
            onDismissRequest = { historyVisible = false; viewModel.clearHistory() },
            confirmButton = {
                TextButton(onClick = { historyVisible = false; viewModel.clearHistory() }) { Text("Schließen") }
            },
            title = { Text("Historie") },
            text = {
                if (history.isEmpty()) {
                    Text("Keine Einträge", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        history.forEach { entry ->
                            Text(
                                "${entry.field}: ${entry.oldValue ?: "–"} → ${entry.newValue ?: "–"}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            },
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
        )
    }
}

private fun symbolFor(kind: MeasurementKind): ImageVector = when (kind) {
    MeasurementKind.PLANE -> PyxisIcons.StrikeDip
    MeasurementKind.LINE -> PyxisIcons.Lineation
    MeasurementKind.BEARING -> PyxisIcons.Bearing
}

private fun primaryText(m: Measurement): String = when (m.kind) {
    MeasurementKind.PLANE -> "${fmt(m.attitude?.dipDirection)} / ${fmt(m.attitude?.dip)}"
    MeasurementKind.LINE -> "${fmt(m.lineation?.trend)} → ${fmt(m.lineation?.plunge)}"
    MeasurementKind.BEARING -> "${fmt(m.bearingDeg)}°"
}

private fun secondaryText(m: Measurement): String = when (m.kind) {
    MeasurementKind.PLANE -> "Aufschluss · Fläche"
    MeasurementKind.LINE -> "Aufschluss · Linear"
    MeasurementKind.BEARING -> "Aufschluss · Peilung"
}

private fun fmt(value: Double?): String =
    if (value == null) "–" else String.format(Locale.US, "%.1f", value)
