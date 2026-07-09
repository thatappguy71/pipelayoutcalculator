package com.thatappguy.pipelayoutcalculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thatappguy.pipelayoutcalculator.ui.util.DiagramConstants
import kotlin.math.*

/**
 * A technically inspired app icon featuring a miter-cut pipe and its flattened layout wrap.
 * Highlights the relationship between 3D geometry and 2D layout.
 */
@Composable
@Preview(showBackground = true, backgroundColor = 0xFF121212)
fun PipeLayoutAppIcon(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(512.dp)
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height

        // 1. Technical Background: Deep Graphite
        drawRect(color = Color(0xFF0D1117), size = size)

        // --- SECTION A: The 3D Mitered Pipe ---
        val pipeWidth = w * 0.16f
        val pipeBaseY = h * 0.72f
        val pipeLeftX = w * 0.10f
        val pipeHeightShort = h * 0.22f
        val pipeHeightLong = h * 0.42f

        val pipeBrush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF2D3748), Color(0xFF4A5568), Color(0xFF1A202C)),
            startX = pipeLeftX,
            endX = pipeLeftX + pipeWidth
        )

        val miterPath = Path().apply {
            moveTo(pipeLeftX, pipeBaseY)
            lineTo(pipeLeftX + pipeWidth, pipeBaseY)
            lineTo(pipeLeftX + pipeWidth, pipeBaseY - pipeHeightShort)
            lineTo(pipeLeftX, pipeBaseY - pipeHeightLong)
            close()
        }
        drawPath(path = miterPath, brush = pipeBrush)
        drawPath(path = miterPath, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))

        // Pipe Rim (Simulated)
        drawOval(
            color = Color(0xFF718096),
            topLeft = Offset(pipeLeftX, pipeBaseY - pipeHeightLong - 4f),
            size = Size(pipeWidth, 12f),
            style = Stroke(width = 1.dp.toPx())
        )

        // --- SECTION B: The Flattened Wrap Template (Primary Focal Point) ---
        val wrapLeft = w * 0.35f
        val wrapRight = w * 0.90f
        val wrapWidth = wrapRight - wrapLeft
        val wrapBaseY = h * 0.72f
        val wrapMaxHeight = h * 0.45f
        val wrapMinHeight = h * 0.18f
        
        // Wrap Background Glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x1A00E676), Color(0x0000E676)),
                startY = wrapBaseY - wrapMaxHeight,
                endY = wrapBaseY
            ),
            topLeft = Offset(wrapLeft, wrapBaseY - wrapMaxHeight),
            size = Size(wrapWidth, wrapMaxHeight)
        )

        // Vertical Ordinate Lines
        val ordinateCount = 12
        for (i in 0..ordinateCount) {
            val x = wrapLeft + (i.toFloat() / ordinateCount) * wrapWidth
            val angle = (i.toFloat() / ordinateCount) * 2.0 * PI
            val amplitude = (wrapMaxHeight - wrapMinHeight) / 2f
            val midY = wrapBaseY - (wrapMaxHeight + wrapMinHeight) / 2f
            val targetY = midY - amplitude * cos(angle.toFloat())
            
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(x, wrapBaseY),
                end = Offset(x, targetY),
                strokeWidth = 1.dp.toPx()
            )
            
            // Ordinate Bottom Mark (Simplified "number")
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = 1.5.dp.toPx(),
                center = Offset(x, wrapBaseY + 6.dp.toPx())
            )
        }

        // Sine-Wave Layout Line (The "Cut Line")
        val layoutPath = Path().apply {
            val divisions = 48
            for (i in 0..divisions) {
                val x = wrapLeft + (i.toFloat() / divisions) * wrapWidth
                val angle = (i.toFloat() / divisions) * 2.0 * PI
                val amplitude = (wrapMaxHeight - wrapMinHeight) / 2f
                val midY = wrapBaseY - (wrapMaxHeight + wrapMinHeight) / 2f
                val y = midY - amplitude * cos(angle.toFloat())
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        
        // Draw main curve with Intense Glow
        drawPath(
            path = layoutPath,
            color = DiagramConstants.ActiveSecondary.copy(alpha = 0.15f),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = layoutPath,
            color = DiagramConstants.ActiveSecondary.copy(alpha = 0.4f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = layoutPath,
            color = Color.White,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = layoutPath,
            color = DiagramConstants.ActiveSecondary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Base Reference Line
        drawLine(
            color = Color.White,
            start = Offset(wrapLeft, wrapBaseY),
            end = Offset(wrapRight, wrapBaseY),
            strokeWidth = 2.dp.toPx()
        )

        // --- SECTION C: Engineering Projections (Dash Lines) ---
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
        // Top projection
        drawLine(
            color = Color(0x33FFFFFF),
            start = Offset(pipeLeftX, pipeBaseY - pipeHeightLong),
            end = Offset(wrapLeft + wrapWidth / 2f, wrapBaseY - wrapMaxHeight),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
        // Bottom projection
        drawLine(
            color = Color(0x33FFFFFF),
            start = Offset(pipeLeftX + pipeWidth, pipeBaseY - pipeHeightShort),
            end = Offset(wrapLeft, wrapBaseY - wrapMinHeight),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
    }
}
