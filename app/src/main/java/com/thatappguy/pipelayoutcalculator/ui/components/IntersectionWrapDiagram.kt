package com.thatappguy.pipelayoutcalculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thatappguy.pipelayoutcalculator.ui.util.DiagramConstants
import kotlin.math.*

/**
 * Renders a programmatic diagram showing how the 16 ordinates
 * translate to a flattened paper wrap template.
 */
@Composable
fun IntersectionWrapDiagram(
    headerNps: String,
    branchNps: String,
    angleDegrees: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(DiagramConstants.StandardDiagramHeight)
            .padding(vertical = 12.dp),
        color = DiagramConstants.DiagramBackground,
        shape = MaterialTheme.shapes.medium
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Normalized internal drawing viewport (0-100 units)
            val s = w / 100f
            val sy = h / 100f // Separate vertical scale for the diagram height

            // 1. Draw Technical Coordinate Grid
            val gridStroke = Stroke(width = 0.5f * s, cap = StrokeCap.Round)
            
            // Baseline (Bottom ordinate boundary)
            drawLine(
                color = DiagramConstants.MarkColor,
                start = Offset(x = 10f * s, y = 80f * sy),
                end = Offset(x = 90f * s, y = 80f * sy),
                strokeWidth = gridStroke.width
            )

            // Multiple coordinate station reference lines (8 standard)
            for (i in 0..8) {
                val xPos = (10f + (i * 10f)) * s
                drawLine(
                    color = DiagramConstants.MarkColor,
                    start = Offset(xPos, 30f * sy),
                    end = Offset(xPos, 80f * sy),
                    strokeWidth = gridStroke.width
                )
            }

            // 2. Draw Simulated Sine-Wave Cut Line
            // This curve visually skews based on the input angle (sharper curve = sharper angle)
            val skewFactor = 1.0 + (abs(90.0 - angleDegrees) / 180.0)
            val cutPath = Path().apply {
                moveTo(10f * s, 40f * sy)
                
                // Uses Bezier pathing to create the programmatic saddle profile
                for (x in 1..8) {
                    val angleOffset = Math.toRadians((x * 45.0) * skewFactor)
                    val y = 55f - (15f * sin(angleOffset))
                    lineTo((10f + (x * 10f)) * s, y.toFloat() * sy)
                }
            }
            
            drawPath(
                path = cutPath,
                color = DiagramConstants.ActiveSecondary, // Our theme green
                style = Stroke(width = 3f * s, cap = StrokeCap.Round)
            )

            // 3. Technical Label Annotation (Simulated ordinates)
            drawCircle(color = DiagramConstants.ActiveSecondary, radius = 2.5f * s, center = Offset(x = 50f * s, y = 55f * sy))
            drawLine(color = Color.Black, start = Offset(x = 50f * s, y = 55f * sy), end = Offset(x = 50f * s, y = 80f * sy), strokeWidth = 1f * s)
        }
    }
}
