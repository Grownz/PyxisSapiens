package de.pyxissapiens.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Structural-geology symbol icons. Drawn as strokes so a caller's [ImageVector] tint applies.
 * Conventional simplified representations of common field symbols.
 */
object PyxisIcons {

    private fun strokeIcon(name: String, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            pathBuilder = block,
        ).build()

    /** Strike line with a dip tick. */
    val StrikeDip: ImageVector by lazy {
        strokeIcon("StrikeDip") {
            moveTo(4f, 9f); lineTo(20f, 9f)
            moveTo(9f, 9f); lineTo(9f, 16f)
            moveTo(9f, 16f); lineTo(12f, 13f)
        }
    }

    /** Lineation: line with an arrowhead. */
    val Lineation: ImageVector by lazy {
        strokeIcon("Lineation") {
            moveTo(4f, 12f); lineTo(18f, 12f)
            moveTo(18f, 12f); lineTo(14f, 9f)
            moveTo(18f, 12f); lineTo(14f, 15f)
        }
    }

    /** Foliation: line with repeated ticks. */
    val Foliation: ImageVector by lazy {
        strokeIcon("Foliation") {
            moveTo(4f, 11f); lineTo(20f, 11f)
            moveTo(8f, 11f); lineTo(8f, 15f)
            moveTo(12f, 11f); lineTo(12f, 15f)
            moveTo(16f, 11f); lineTo(16f, 15f)
        }
    }

    /** Fault: line with opposing ticks. */
    val Fault: ImageVector by lazy {
        strokeIcon("Fault") {
            moveTo(4f, 12f); lineTo(20f, 12f)
            moveTo(8f, 12f); lineTo(11f, 8f)
            moveTo(16f, 12f); lineTo(13f, 16f)
        }
    }

    /** Joint: a single planar line. */
    val Joint: ImageVector by lazy {
        strokeIcon("Joint") {
            moveTo(4f, 12f); lineTo(20f, 12f)
        }
    }

    /** Fold axis: a zig-zag. */
    val Fold: ImageVector by lazy {
        strokeIcon("Fold") {
            moveTo(4f, 16f); lineTo(9f, 8f); lineTo(14f, 16f); lineTo(19f, 8f)
        }
    }

    /** Bearing/peilung: a needle pointing north. */
    val Bearing: ImageVector by lazy {
        strokeIcon("Bearing") {
            moveTo(12f, 20f); lineTo(12f, 5f)
            moveTo(8f, 9f); lineTo(12f, 5f); lineTo(16f, 9f)
        }
    }
}
