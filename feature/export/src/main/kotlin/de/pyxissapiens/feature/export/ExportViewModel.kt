package de.pyxissapiens.feature.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.pyxissapiens.core.data.MeasurementRepository
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import de.pyxissapiens.core.export.CsvExporter
import de.pyxissapiens.core.export.CsvImporter
import de.pyxissapiens.core.export.ExportValidator
import de.pyxissapiens.core.export.GeoJsonExporter
import de.pyxissapiens.core.export.KmlExporter
import de.pyxissapiens.core.export.KmzExporter
import de.pyxissapiens.core.export.PdfReportExporter
import de.pyxissapiens.core.export.ProjectArchive
import de.pyxissapiens.core.geology.math.GeoMath
import de.pyxissapiens.core.geology.stereo.Projection
import de.pyxissapiens.core.geology.stereo.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject

enum class ExportFormat(val label: String, val extension: String) {
    CSV("CSV", "csv"),
    GEOJSON("GeoJSON", "geojson"),
    KML("KML", "kml"),
    KMZ("KMZ", "kmz"),
    PDF("PDF", "pdf"),
    ARCHIVE("Projektarchiv", "pyxis"),
}

data class ExportStatus(val message: String, val sharedFile: File? = null)

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MeasurementRepository,
) : ViewModel() {

    val measurements: StateFlow<List<Measurement>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _status = MutableStateFlow<ExportStatus?>(null)
    val status: StateFlow<ExportStatus?> = _status

    fun clearStatus() { _status.value = null }

    fun export(format: ExportFormat, password: String?) {
        val data = measurements.value
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runExport(format, data, password) }
            _status.value = result
        }
    }

    fun exportAll(password: String?) {
        val data = measurements.value
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for (format in ExportFormat.entries) runExport(format, data, password)
            }
            _status.value = ExportStatus("Alle Formate exportiert nach ${exportDir().absolutePath}")
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            val message = runImport(uri)
            _status.value = ExportStatus(message)
        }
    }

    private fun runExport(format: ExportFormat, data: List<Measurement>, password: String?): ExportStatus {
        val validation = ExportValidator.validate(data)
        val dir = exportDir()
        val reportLine = if (validation.ok) "" else " (${validation.warnings.size} Warnungen)"
        return when (format) {
            ExportFormat.CSV -> {
                val file = File(dir, "pyxis_export.csv")
                file.writeText(CsvExporter.export(data))
                ExportStatus("CSV exportiert${reportLine}: ${file.name}", file)
            }
            ExportFormat.GEOJSON -> {
                val file = File(dir, "pyxis_export.geojson")
                file.writeText(GeoJsonExporter.export(data))
                ExportStatus("GeoJSON exportiert${reportLine}: ${file.name}", file)
            }
            ExportFormat.KML -> {
                val file = File(dir, "pyxis_export.kml")
                file.writeText(KmlExporter.export(data, "PyxisSapiens"))
                ExportStatus("KML exportiert: ${file.name}", file)
            }
            ExportFormat.KMZ -> {
                val file = File(dir, "pyxis_export.kmz")
                file.writeBytes(KmzExporter.export(KmlExporter.export(data, "PyxisSapiens")))
                ExportStatus("KMZ exportiert: ${file.name}", file)
            }
            ExportFormat.PDF -> {
                val file = File(dir, "pyxis_report.pdf")
                file.writeBytes(PdfReportExporter.export("Feldbericht", data, Projection.SCHMIDT, statsText(data)))
                ExportStatus("PDF exportiert: ${file.name}", file)
            }
            ExportFormat.ARCHIVE -> {
                val files = mapOf(
                    "manifest.txt" to "PyxisSapiens project archive\nmeasurements=${data.size}\n".toByteArray(),
                    "export.csv" to CsvExporter.export(data).toByteArray(),
                    "export.geojson" to GeoJsonExporter.export(data).toByteArray(),
                )
                val name = if (password.isNullOrEmpty()) "pyxis_project.pyxis" else "pyxis_project_encrypted.pyxis"
                val file = File(dir, name)
                file.writeBytes(ProjectArchive.write(files, password))
                ExportStatus("Archiv exportiert: ${file.name}", file)
            }
        }
    }

    private suspend fun runImport(uri: Uri): String {
        val text = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        } ?: return "Datei konnte nicht gelesen werden"
        val name = uri.lastPathSegment?.lowercase() ?: ""
        val trimmed = text.trimStart()
        val result = when {
            name.endsWith(".kml") || trimmed.startsWith("<?xml") || trimmed.contains("<kml") ->
                de.pyxissapiens.core.export.KmlImporter.parse(text)
            else -> CsvImporter.parse(text)
        }
        if (result.measurements.isEmpty()) {
            return "Keine Datensätze importiert. ${result.warnings.joinToString("; ")}"
        }
        return try {
            result.measurements.forEach { repository.add(it) }
            "Import: ${result.measurements.size} Datensätze (${result.warnings.size} Warnungen)"
        } catch (e: Exception) {
            "Import fehlgeschlagen: ${e.message}"
        }
    }

    private fun statsText(data: List<Measurement>): String {
        val lines = data.filter { it.kind == MeasurementKind.LINE && it.lineation != null }
            .map { it.lineation!! }
        val planes = data.filter { it.kind == MeasurementKind.PLANE && it.attitude != null }
            .map { it.attitude!! }
        val sb = StringBuilder()
        if (planes.isNotEmpty()) {
            val normals = planes.map { GeoMath.planeToPole(it.dipDirection, it.dip) }
            val mean = Statistics.meanPlane(normals)
            sb.append("Flächen n=${planes.size} · mittlere Fläche ${fmt(mean.first)}/${fmt(mean.second)}\n")
        }
        if (lines.isNotEmpty()) {
            val vectors = lines.map { GeoMath.lineationToVector(it.trend, it.plunge) }
            val fisher = Statistics.fisher(vectors)
            val eigen = Statistics.eigen(vectors)
            val (ft, fp) = GeoMath.vectorToLineation(Statistics.foldAxis(eigen))
            sb.append("Lineare n=${lines.size} · κ=${fmt(fisher.kappa)} α95=${fmt(fisher.alpha95Deg)}° · Fold-Achse ${fmt(ft)}→${fmt(fp)}°\n")
        }
        return sb.toString().trimEnd()
    }

    private fun fmt(v: Double) = String.format(Locale.US, "%.1f", v)

    private fun exportDir(): File =
        (context.getExternalFilesDir(null) ?: context.filesDir).resolve("exports").apply { mkdirs() }
}
