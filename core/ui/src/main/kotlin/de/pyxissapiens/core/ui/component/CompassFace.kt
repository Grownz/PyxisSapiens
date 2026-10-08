package de.pyxissapiens.core.ui.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import de.pyxissapiens.core.ui.theme.LocalPyxisTokens
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compass rose with tick marks, cardinal letters and an azimuth needle (docs/04 §2.3/§4.2).
 * [azimuthDeg] is clockwise from north.
 */
@Composable
fun CompassFace(
    azimuthDeg: Float,
    modifier: Modifier = Modifier,
    showCardinals: Boolean = true,
) {
    val tokens = LocalPyxisTokens.current
    val accent = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = tokens.grid

    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 6.dp.toPx()
        val strokeWidth = 2.dp.toPx()

        drawCircle(color = grid, radius = radius, center = center, style = Stroke(width = strokeWidth))

        for (i in 0 until 36) {
            val angleRad = Math.toRadians(i * 10.0)
            val isMajor = i % 3 == 0
            val inner = radius - (if (isMajor) 14.dp.toPx() else 7.dp.toPx())
            val dx = sin(angleRad).toFloat()
            val dy = -cos(angleRad).toFloat()
            drawLine(
                color = if (i == 0) tokens.north else grid,
                start = Offset(center.x + dx * inner, center.y + dy * inner),
                end = Offset(center.x + dx * radius, center.y + dy * radius),
                strokeWidth = if (i == 0) 3.dp.toPx() else strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        // Azimuth needle
        val a = Math.toRadians(azimuthDeg.toDouble())
        val tip = Offset(
            center.x + sin(a).toFloat() * (radius - 20.dp.toPx()),
            center.y - cos(a).toFloat() * (radius - 20.dp.toPx()),
        )
        drawLine(color = accent, start = center, end = tip, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = accent, radius = 5.dp.toPx(), center = center)

        if (showCardinals) {
            val paint = Paint().apply {
                color = labelColor.toArgb()
                textSize = 14.dp.toPx()
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }
            val rText = radius - 26.dp.toPx()
            drawIntoCanvas { canvas ->
                fun put(label: String, angleDeg: Double) {
                    val rad = Math.toRadians(angleDeg)
                    val x = center.x + sin(rad).toFloat() * rText
                    val y = center.y - cos(rad).toFloat() * rText + paint.textSize / 3f
                    canvas.nativeCanvas.drawText(label, x, y, paint)
                }
                put("N", 0.0)
                put("E", 90.0)
                put("S", 180.0)
                put("W", 270.0)
            }
        }
    }
}

/** Convenience: a square compass face. */
@Composable
fun CompassFaceSquare(azimuthDeg: Float, modifier: Modifier = Modifier) {
    CompassFace(azimuthDeg = azimuthDeg, modifier = modifier.fillMaxSize())
}
