package de.pyxissapiens.feature.map

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
    var styleUrl by remember(state.styleUrl) { mutableStateOf(state.styleUrl) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importMbtiles(it) }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Basiskarten")

        PyxisPanel {
            Text("Online-Style", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = styleUrl,
                onValueChange = { styleUrl = it },
                label = { Text("Style-URL") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = { viewModel.setStyleUrl(styleUrl) }) { Text("Speichern") }
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

        state.message?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
