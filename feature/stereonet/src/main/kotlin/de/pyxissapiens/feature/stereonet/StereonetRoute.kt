package de.pyxissapiens.feature.stereonet

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pyxissapiens.core.geology.stereo.DensityMethod
import de.pyxissapiens.core.geology.stereo.KambKernel
import de.pyxissapiens.core.geology.stereo.Projection
import de.pyxissapiens.core.ui.component.PyxisPanel
import de.pyxissapiens.core.ui.component.SectionHeader
import de.pyxissapiens.core.ui.theme.LocalPyxisTokens
import java.io.File

@Composable
fun StereonetRoute(
    modifier: Modifier = Modifier,
    viewModel: StereonetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val controls = state.controls
    val model = state.model
    val tokens = LocalPyxisTokens.current
    val context = LocalContext.current
    var exportMessage by remember { mutableStateOf<String?>(null) }

    val colors = PlotColors(
        grid = tokens.grid,
        plane = MaterialTheme.colorScheme.tertiary,
        line = tokens.ok,
        contour = MaterialTheme.colorScheme.primary,
        axis = tokens.critical,
        label = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader("Auswertung · Stereonet (${model.count} Daten)")

        // Plot
        Canvas(Modifier.fillMaxWidth().size(360.dp)) {
            drawStereonet(model, controls, state.heatmap, colors)
        }

        // Statistics
        PyxisPanel {
            Text(StereonetSvg.summary(model), style = MaterialTheme.typography.bodySmall)
        }

        SectionHeader("Projektion")
        OptionRow(
            options = listOf("Schmidt (flächen-treu)", "Wulff (winkeltreu)"),
            selected = if (controls.projection == Projection.SCHMIDT) 0 else 1,
            onSelected = { viewModel.setProjection(if (it == 0) Projection.SCHMIDT else Projection.WULFF) },
        )

        SectionHeader("Datensatz")
        OptionRow(
            options = listOf("Flächen (Pole)", "Lineare"),
            selected = if (controls.dataset == Dataset.PLANES) 0 else 1,
            onSelected = { viewModel.setDataset(if (it == 0) Dataset.PLANES else Dataset.LINES) },
        )

        SectionHeader("Dichte-Verfahren")
        OptionRow(
            options = listOf("Kamb", "Fisher-Kernel"),
            selected = if (controls.method == DensityMethod.KAMB) 0 else 1,
            onSelected = { viewModel.setMethod(if (it == 0) DensityMethod.KAMB else DensityMethod.FISHER_KERNEL) },
        )
        if (controls.method == DensityMethod.KAMB) {
            OptionRow(
                options = listOf("Raw", "Linear", "Exponentiell"),
                selected = when (controls.kernel) {
                    KambKernel.RAW -> 0
                    KambKernel.LINEAR -> 1
                    KambKernel.EXPONENTIAL -> 2
                },
                onSelected = {
                    viewModel.setKernel(
                        when (it) { 0 -> KambKernel.RAW; 1 -> KambKernel.LINEAR; else -> KambKernel.EXPONENTIAL },
                    )
                },
            )
            SliderRow(
                label = "Zählwinkel α = ${controls.countingAngleDeg.toInt()}°",
                value = controls.countingAngleDeg.toFloat(),
                range = 3f..30f,
                onChange = { viewModel.setCountingAngle(it.toDouble()) },
            )
        } else {
            SliderRow(
                label = "κ = ${controls.fisherKappa.toInt()}",
                value = controls.fisherKappa.toFloat(),
                range = 5f..200f,
                onChange = { viewModel.setKappa(it.toDouble()) },
            )
        }
        SliderRow(
            label = "Kontur-Level = ${(controls.contourLevel * 100).toInt()} %",
            value = controls.contourLevel.toFloat(),
            range = 0.1f..0.95f,
            onChange = { viewModel.setContourLevel(it.toDouble()) },
        )

        SectionHeader("Elemente")
        OptionRow(
            options = listOf(
                if (controls.showDensity) "Dichte ✓" else "Dichte",
                if (controls.showGreatCircles) "Großkreise ✓" else "Großkreise",
                if (controls.showPoles) "Pole ✓" else "Pole",
                if (controls.showLines) "Lineare ✓" else "Lineare",
            ),
            selected = -1,
            onSelected = {
                when (it) {
                    0 -> viewModel.toggleDensity()
                    1 -> viewModel.toggleGreatCircles()
                    2 -> viewModel.togglePoles()
                    3 -> viewModel.toggleLines()
                }
            },
        )

        SectionHeader("Export")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                exportMessage = exportSvg(context, model, controls)
            }) { Text("SVG") }
            Button(onClick = {
                exportMessage = exportPng(context, model, controls, state.heatmap, colors)
            }) { Text("PNG") }
        }
        exportMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = tokens.ok) }
    }
}

@Composable
private fun OptionRow(options: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selected,
                onClick = { onSelected(index) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

private fun exportDir(context: Context): File =
    (context.getExternalFilesDir(null) ?: context.filesDir).resolve("exports").apply { mkdirs() }

private fun exportSvg(context: Context, model: StereonetRenderModel, controls: StereonetControls): String {
    val file = File(exportDir(context), "stereonet.svg")
    file.writeText(StereonetSvg.render(model, controls))
    return "SVG exportiert: ${file.absolutePath}"
}

private fun exportPng(
    context: Context,
    model: StereonetRenderModel,
    controls: StereonetControls,
    heatmap: androidx.compose.ui.graphics.ImageBitmap?,
    colors: PlotColors,
): String {
    val sizePx = 1000
    val image = androidx.compose.ui.graphics.ImageBitmap(sizePx, sizePx)
    val canvas: GraphicsCanvas = GraphicsCanvas(image)
    CanvasDrawScope().draw(
        Density(1f, 1f),
        LayoutDirection.Ltr,
        canvas,
        Size(sizePx.toFloat(), sizePx.toFloat()),
    ) {
        drawStereonet(model, controls, heatmap, colors)
    }
    val file = File(exportDir(context), "stereonet.png")
    file.outputStream().use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    return "PNG exportiert: ${file.absolutePath}"
}
