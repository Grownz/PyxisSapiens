package de.pyxissapiens.feature.map

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader

@Composable
fun BasemapsRoute(
    modifier: Modifier = Modifier,
    viewModel: BasemapsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var styleUrl by remember(state.styleUrl) { mutableStateOf(state.styleUrl) }
    var geologyUrl by remember(state.geologyUrl) { mutableStateOf(state.geologyUrl ?: "") }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importMbtiles(it) }
    }
    val lineworkLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importLinework(it) }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Basiskarten")

        PyxisPanel {
            Text("Online-Style", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = styleUrl, onValueChange = { styleUrl = it }, label = { Text("Style-URL") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.setStyleUrl(styleUrl) }) { Text("Speichern") }
        }

        PyxisPanel {
            Text("Geologischer Layer (WMS/XYZ)", style = MaterialTheme.typography.titleMedium)
            Text(
                "XYZ-Template (mit {z}/{x}/{y}) oder WMS-GetMap-URL. Wird als Raster-Overlay eingebunden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(value = geologyUrl, onValueChange = { geologyUrl = it }, label = { Text("Layer-URL") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.setGeologyUrl(geologyUrl) }) { Text("Speichern") }
                FilterChip(selected = state.geologyEnabled, onClick = { viewModel.setGeologyEnabled(!state.geologyEnabled) }, label = { Text("Aktiv") })
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://macrostrat.org/map")))
                }) { Text("Link-out") }
            }
        }

        PyxisPanel {
            Text("Offline-Karten (MBTiles)", style = MaterialTheme.typography.titleMedium)
            if (state.files.isEmpty()) {
                Text("Keine importierten Karten.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.files.forEach { file ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        file.name + if (state.active == file.absolutePath) "  (aktiv)" else "",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (state.active == file.absolutePath) {
                        OutlinedButton(onClick = { viewModel.setActive(null) }) { Text("Aus") }
                    } else {
                        Button(onClick = { viewModel.setActive(file.absolutePath) }) { Text("An") }
                    }
                    OutlinedButton(onClick = { viewModel.delete(file) }) { Text("Löschen") }
                }
            }
            Button(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Text("MBTiles importieren") }
        }

        PyxisPanel {
            Text("Linework importieren", style = MaterialTheme.typography.titleMedium)
            Text("GeoJSON- oder KML-Linien/-Polygone in die Kartierung übernehmen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { lineworkLauncher.launch(arrayOf("*/*")) }) { Text("GeoJSON/KML importieren") }
        }

        state.message?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
