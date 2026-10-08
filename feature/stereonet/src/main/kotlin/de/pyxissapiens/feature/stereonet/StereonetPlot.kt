package de.pyxissapiens.feature.stereonet

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import de.pyxissapiens.core.geology.stereo.DensityMethod
import de.pyxissapiens.core.geology.stereo.Point2
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class PlotColors(
    val grid: Color,
    val plane: Color,
    val line: Color,
    val contour: Color,
    val axis: Color,
    val label: Color,
)

private fun DrawScope.diskToScreen(p: Point2, center: Offset, radius: Float): Offset =
    Offset(center.x + (p.x * radius).toFloat(), center.y - (p.y * radius).toFloat())

/** Draws the stereonet. Shared by the on-screen canvas and the PNG export. */
fun DrawScope.drawStereonet(
    model: StereonetRenderModel,
    controls: StereonetControls,
    heatmap: ImageBitmap?,
    colors: PlotColors,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = (size.minDimension / 2f) - 8f
    val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }

    // Density heatmap (clipped to the disk)
    if (controls.showDensity && heatmap != null && model.gridSize > 0) {
        clipPath(circle) {
            drawImage(
                image = heatmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(heatmap.width, heatmap.height),
                dstOffset = IntOffset((center.x - radius).roundToInt(), (center.y - radius).roundToInt()),
                dstSize = IntSize((2 * radius).roundToInt(), (2 * radius).roundToInt()),
            )
        }
    }

    // Contour lines
    for ((a, b) in model.contours) {
        drawLine(
            color = colors.contour,
            start = diskToScreen(a, center, radius),
            end = diskToScreen(b, center, radius),
            strokeWidth = 1.2f,
        )
    }

    // Great circles
    if (controls.showGreatCircles) {
        for (gc in model.greatCircles) {
            val path = Path()
            gc.forEachIndexed { i, p ->
                val s = diskToScreen(p, center, radius)
                if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
            }
            drawPath(path, colors.plane, style = Stroke(width = 1.5f))
        }
    }

    // Poles (crosses)
    if (controls.showPoles) {
        for (p in model.polePoints) {
            val s = diskToScreen(p, center, radius)
            val t = 4f
            drawLine(colors.plane, Offset(s.x - t, s.y), Offset(s.x + t, s.y), strokeWidth = 1.5f)
            drawLine(colors.plane, Offset(s.x, s.y - t), Offset(s.x, s.y + t), strokeWidth = 1.5f)
        }
    }

    // Lineations (filled dots)
    if (controls.showLines) {
        for (p in model.linePoints) {
            drawCircle(colors.line, radius = 3.5f, center = diskToScreen(p, center, radius))
        }
    }

    // Outline + cardinals
    drawCircle(colors.grid, radius = radius, center = center, style = Stroke(width = 2f))
    val paint = Paint().apply {
        color = colors.label.toArgb()
        textSize = 26f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    drawIntoCanvas { canvas ->
        fun put(label: String, angleDeg: Double) {
            val rad = Math.toRadians(angleDeg)
            val x = center.x + sin(rad).toFloat() * (radius - 18f)
            val y = center.y - cos(rad).toFloat() * (radius - 18f) + paint.textSize / 3f
            canvas.nativeCanvas.drawText(label, x, y, paint)
        }
        put("N", 0.0); put("E", 90.0); put("S", 180.0); put("W", 270.0)
    }
}

/** Renders the same model to an SVG document. */
object StereonetSvg {

    fun render(model: StereonetRenderModel, controls: StereonetControls, sizePx: Int = 1000): String {
        val c = sizePx / 2.0
        val r = c - 20.0
        fun sx(x: Double) = c + x * r
        fun sy(y: Double) = c - y * r
        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$sizePx" height="$sizePx" viewBox="0 0 $sizePx $sizePx">""")
        sb.append("""<rect width="$sizePx" height="$sizePx" fill="#0B0F14"/>""")
        sb.append("""<circle cx="$c" cy="$c" r="$r" fill="none" stroke="#2A3644" stroke-width="2"/>""")
        sb.append("""<text x="$c" y="30" fill="#E8EEF5" font-size="28" text-anchor="middle">N</text>""")

        for ((a, b) in model.contours) {
            sb.append("""<line x1="${sx(a.x)}" y1="${sy(a.y)}" x2="${sx(b.x)}" y2="${sy(b.y)}" stroke="#FFB000" stroke-width="1.2" opacity="0.7"/>""")
        }
        if (controls.showGreatCircles) {
            for (gc in model.greatCircles) {
                val pts = gc.joinToString(" ") { "${sx(it.x)},${sy(it.y)}" }
                sb.append("""<polyline points="$pts" fill="none" stroke="#4EA1FF" stroke-width="1.5"/>""")
            }
        }
        if (controls.showPoles) {
            for (p in model.polePoints) {
                sb.append("""<line x1="${sx(p.x) - 5}" y1="${sy(p.y)}" x2="${sx(p.x) + 5}" y2="${sy(p.y)}" stroke="#FF5C5C" stroke-width="1.5"/>""")
                sb.append("""<line x1="${sx(p.x)}" y1="${sy(p.y) - 5}" x2="${sx(p.x)}" y2="${sy(p.y) + 5}" stroke="#FF5C5C" stroke-width="1.5"/>""")
            }
        }
        if (controls.showLines) {
            for (p in model.linePoints) {
                sb.append("""<circle cx="${sx(p.x)}" cy="${sy(p.y)}" r="3.5" fill="#37D67A"/>""")
            }
        }
        sb.append("</svg>")
        return sb.toString()
    }

    fun summary(model: StereonetRenderModel): String = buildString {
        append("n=${model.count}")
        model.fisherText?.let { append("\n$it") }
        model.eigenText?.let { append("\n$it") }
        model.woodcockText?.let { append("\n$it") }
        model.foldAxisText?.let { append("\n$it") }
        model.meanPlaneText?.let { append("\n$it") }
    }

    @Suppress("unused")
    private fun fmt(v: Double) = String.format(Locale.US, "%.1f", v)
}
