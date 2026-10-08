package de.pyxissapiens.feature.export

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader
import java.io.File

@Composable
fun ExportRoute(
    modifier: Modifier = Modifier,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    var format by remember { mutableStateOf(ExportFormat.CSV) }
    var password by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.import(it) }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Export / Import (${measurements.size} Messungen)")

        SectionHeader("Format")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExportFormat.entries.take(3).forEach { f ->
                FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f.label) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExportFormat.entries.drop(3).forEach { f ->
                FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f.label) })
            }
        }

        if (format == ExportFormat.ARCHIVE) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort (optional, verschlüsselt)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.export(format, password.ifBlank { null }) }) {
                Text("Exportieren")
            }
            OutlinedButton(onClick = { viewModel.exportAll(password.ifBlank { null }) }) {
                Text("Alle Formate")
            }
        }

        SectionHeader("Import")
        OutlinedButton(onClick = {
            importLauncher.launch(
                arrayOf(
                    "text/csv", "text/comma-separated-values", "text/plain",
                    "application/vnd.google-earth.kml+xml", "*/*",
                ),
            )
        }) {
            Text("CSV oder KML importieren")
        }

        status?.let { s ->
            PyxisPanel {
                Text(s.message, style = MaterialTheme.typography.bodySmall)
                s.sharedFile?.let { file ->
                    Button(onClick = { shareFile(context, file) }) { Text("Teilen") }
                }
            }
        }
    }
}

private fun shareFile(context: android.content.Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(context, "de.pyxissapiens.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType(file.name)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export teilen"))
}

private fun mimeType(name: String): String = when (name.substringAfterLast('.', "")) {
    "csv" -> "text/csv"
    "geojson" -> "application/geo+json"
    "kml" -> "application/vnd.google-earth.kml+xml"
    "kmz" -> "application/vnd.google-earth.kmz"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}
