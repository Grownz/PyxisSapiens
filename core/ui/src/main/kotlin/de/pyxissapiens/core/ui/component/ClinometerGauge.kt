package de.pyxissapiens.core.ui.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
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
 * Semicircular clinometer gauge. [dipDeg] in 0..90 maps from the left (0°) to the top (90°).
 */
@Composable
fun ClinometerGauge(
    dipDeg: Float,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalPyxisTokens.current
    val accent = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier) {
        val stroke = 10.dp.toPx()
        val inset = stroke / 2f + 2.dp.toPx()
        val arcTopLeft = Offset(inset, inset)
        val arcSize = Size(size.width - 2f * inset, size.height - 2f * inset)
        val center = Offset(size.width / 2f, size.height - inset)

        // Track: upper semicircle (canvas 180° -> 360°).
        drawArc(
            color = tokens.grid,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        val dip = dipDeg.coerceIn(0f, 90f)
        val angleRad = Math.toRadians(180.0 + dip)
        val r = (size.minDimension / 2f) - inset - 6.dp.toPx()
        val tip = Offset(center.x + cos(angleRad).toFloat() * r, center.y + sin(angleRad).toFloat() * r)
        drawLine(color = accent, start = center, end = tip, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = accent, radius = 5.dp.toPx(), center = center)

        val paint = Paint().apply {
            color = labelColor.toArgb()
            textSize = 14.dp.toPx()
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText("0", inset + 4.dp.toPx(), center.y + 16.dp.toPx(), paint)
            canvas.nativeCanvas.drawText("90", center.x, inset + 14.dp.toPx(), paint)
        }
    }
}
