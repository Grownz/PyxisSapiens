package de.pyxissapiens.feature.map

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import de.pyxissapiens.core.data.LineworkRepository
import de.pyxissapiens.core.export.LineworkImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class BasemapsUiState(
    val styleUrl: String = DEFAULT_STYLE_URL,
    val active: String? = null,
    val files: List<File> = emptyList(),
    val geologyUrl: String? = null,
    val geologyEnabled: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class BasemapsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lineworks: LineworkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(refresh())
    val state: StateFlow<BasemapsUiState> = _state

    private fun refresh(message: String? = _state.value.message) = BasemapsUiState(
        styleUrl = MapSettings.styleUrl(context),
        active = MapSettings.activeMbtiles(context),
        files = MapSettings.listMbtiles(context),
        geologyUrl = MapSettings.geologyUrl(context),
        geologyEnabled = MapSettings.geologyEnabled(context),
        message = message,
    )

    fun setStyleUrl(url: String) {
        MapSettings.setStyleUrl(context, url)
        _state.value = refresh("Style-URL gespeichert")
    }

    fun setActive(path: String?) {
        MapSettings.setActiveMbtiles(context, path)
        _state.value = refresh(if (path == null) "Offline-Karte deaktiviert" else "Offline-Karte aktiviert")
    }

    fun delete(file: File) {
        if (MapSettings.activeMbtiles(context) == file.absolutePath) MapSettings.setActiveMbtiles(context, null)
        runCatching { file.delete() }
        _state.value = refresh("Karte gelöscht: ${file.name}")
    }

    fun setGeologyUrl(url: String) {
        MapSettings.setGeologyUrl(context, url)
        _state.value = refresh("Geologie-Layer gespeichert")
    }

    fun setGeologyEnabled(enabled: Boolean) {
        MapSettings.setGeologyEnabled(context, enabled)
        _state.value = refresh(if (enabled) "Geologie-Layer aktiv" else "Geologie-Layer aus")
    }

    fun importMbtiles(uri: Uri) {
        viewModelScope.launch {
            val (ok, target) = withContext(Dispatchers.IO) {
                val dir = MapSettings.mbtilesDir(context).apply { mkdirs() }
                val target = File(dir, "map_${System.currentTimeMillis()}.mbtiles")
                val ok = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    } != null
                }.getOrDefault(false)
                ok to target
            }
            _state.value = refresh(
                if (ok) "Importiert: ${target.name} (${MbtilesSource(target).tileCount()} Kacheln)" else "Import fehlgeschlagen",
            )
        }
    }

    fun importLinework(uri: Uri) {
        viewModelScope.launch {
            val (count, message) = withContext(Dispatchers.IO) {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()
                if (text == null) return@withContext 0 to "Datei konnte nicht gelesen werden"
                val name = uri.lastPathSegment?.lowercase() ?: ""
                val imported = if (name.endsWith(".kml") || text.contains("<kml")) {
                    LineworkImporter.importKml(text)
                } else {
                    LineworkImporter.importGeoJson(text)
                }
                imported.forEach { lineworks.upsert(it) }
                imported.size to "Linework importiert: ${imported.size} Objekte"
            }
            _state.value = refresh(message)
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(message = null) }
}
