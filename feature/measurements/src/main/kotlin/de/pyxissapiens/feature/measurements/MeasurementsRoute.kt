package de.pyxissapiens.feature.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import de.pyxissapiens.core.domain.model.DataType
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.domain.model.RockUnit
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
    val types by viewModel.dataTypes.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()

    var historyVisible by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Measurement?>(null) }
    var creating by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader("Messungen (${measurements.size})", modifier = Modifier.weight(1f))
            IconButton(onClick = { creating = true }) { Icon(Icons.Filled.Add, contentDescription = "Neu") }
            IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Rückgängig")
            }
            IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Wiederherstellen")
            }
        }

        if (types.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedType == null, onClick = { viewModel.setTypeFilter(null) }, label = { Text("Alle") })
                types.take(4).forEach { type ->
                    FilterChip(
                        selected = selectedType == type.id,
                        onClick = { viewModel.setTypeFilter(if (selectedType == type.id) null else type.id) },
                        label = { Text(type.name) },
                    )
                }
            }
        }

        message?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary) }

        if (measurements.isEmpty()) {
            EmptyState(
                title = "Keine Messungen",
                subtitle = "Erfasse im Reiter „Messen\" oder lege manuell eine Messung an.",
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
                            onClick = { viewModel.showHistory(measurement.id); historyVisible = true },
                        )
                        IconButton(onClick = { editing = measurement }) { Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten") }
                        IconButton(onClick = { viewModel.delete(measurement.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Löschen") }
                    }
                }
            }
        }
    }

    if (historyVisible) {
        AlertDialog(
            onDismissRequest = { historyVisible = false; viewModel.clearHistory() },
            confirmButton = { TextButton(onClick = { historyVisible = false; viewModel.clearHistory() }) { Text("Schließen") } },
            title = { Text("Historie") },
            text = {
                if (history.isEmpty()) Text("Keine Einträge", style = MaterialTheme.typography.bodySmall)
                else Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    history.forEach {
                        Text("${it.field}: ${it.oldValue ?: "–"} → ${it.newValue ?: "–"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
        )
    }

    if (creating || editing != null) {
        MeasurementEditDialog(
            initial = editing,
            types = types,
            units = units,
            onDismiss = { creating = false; editing = null },
            onSave = { kind, a, b, note, typeId, unitId ->
                viewModel.save(editing?.id, kind, a, b, note, typeId, unitId)
                creating = false; editing = null
            },
        )
    }
}

@Composable
private fun MeasurementEditDialog(
    initial: Measurement?,
    types: List<DataType>,
    units: List<RockUnit>,
    onDismiss: () -> Unit,
    onSave: (MeasurementKind, Double?, Double?, String?, String?, String?) -> Unit,
) {
    var kind by remember { mutableStateOf(initial?.kind ?: MeasurementKind.PLANE) }
    var a by remember {
        mutableStateOf(initial?.let { when (it.kind) {
            MeasurementKind.PLANE -> it.attitude?.dip
            MeasurementKind.LINE -> it.lineation?.trend
            MeasurementKind.BEARING -> it.bearingDeg
        } }?.let { fmt(it) } ?: "")
    }
    var b by remember {
        mutableStateOf(initial?.let { when (it.kind) {
            MeasurementKind.PLANE -> it.attitude?.dipDirection
            MeasurementKind.LINE -> it.lineation?.plunge
            MeasurementKind.BEARING -> null
        } }?.let { fmt(it) } ?: "")
    }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var typeId by remember { mutableStateOf(initial?.typeId) }
    var unitId by remember { mutableStateOf(initial?.unitId) }

    val (labelA, labelB) = when (kind) {
        MeasurementKind.PLANE -> "Dip (°)" to "Dip-Direction (°)"
        MeasurementKind.LINE -> "Trend (°)" to "Plunge (°)"
        MeasurementKind.BEARING -> "Peilung (°)" to null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Neue Messung" else "Messung bearbeiten") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = kind == MeasurementKind.PLANE, onClick = { kind = MeasurementKind.PLANE }, label = { Text("Fläche") })
                    FilterChip(selected = kind == MeasurementKind.LINE, onClick = { kind = MeasurementKind.LINE }, label = { Text("Linear") })
                    FilterChip(selected = kind == MeasurementKind.BEARING, onClick = { kind = MeasurementKind.BEARING }, label = { Text("Peilung") })
                }
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text(labelA) }, modifier = Modifier.fillMaxWidth())
                if (labelB != null) {
                    OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text(labelB) }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Notiz") }, modifier = Modifier.fillMaxWidth())
                if (types.isNotEmpty()) {
                    Text("Datentyp", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.take(4).forEach { type ->
                            FilterChip(selected = typeId == type.id, onClick = { typeId = if (typeId == type.id) null else type.id }, label = { Text(type.name) })
                        }
                    }
                }
                if (units.isNotEmpty()) {
                    Text("Einheit", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        units.take(4).forEach { unit ->
                            FilterChip(selected = unitId == unit.id, onClick = { unitId = if (unitId == unit.id) null else unit.id }, label = { Text(unit.code ?: unit.name) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(kind, a.toDoubleOrNull(), b.toDoubleOrNull(), note, typeId, unitId)
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
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

private fun fmt(value: Double?): String = if (value == null) "–" else String.format(Locale.US, "%.1f", value)
