package de.pyxissapiens.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val hasPassphrase by viewModel.hasPassphrase.collectAsStateWithLifecycle()
    var passphrase by remember { mutableStateOf("") }
    var confirmErase by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Sicherheit")

        PyxisPanel {
            Text(
                "Datenbank verschlüsselt (SQLCipher)",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Der Schlüssel liegt im Android Keystore. Optional kann eine zusätzliche Passphrase " +
                    "gesetzt werden – sie gilt beim nächsten App-Start.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(if (hasPassphrase) "Passphrase ersetzen/entfernen" else "Passphrase setzen") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.setPassphrase(passphrase); passphrase = "" }) {
                    Text(if (hasPassphrase) "Aktualisieren" else "Setzen")
                }
                OutlinedButton(onClick = { viewModel.setPassphrase(""); passphrase = "" }) {
                    Text("Entfernen")
                }
            }
        }

        SectionHeader("Daten")
        PyxisPanel {
            Text("Datenhoheit", style = MaterialTheme.typography.titleMedium)
            Text(
                "Alle Projekte, Messungen, Linework, Tracks und Medien dauerhaft löschen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { confirmErase = true }) { Text("Alle Daten löschen") }
        }

        message?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text("Alle Daten löschen?") },
            text = { Text("Dieser Vorgang kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = { confirmErase = false; viewModel.eraseAll() }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text("Abbrechen") } },
        )
    }
}
