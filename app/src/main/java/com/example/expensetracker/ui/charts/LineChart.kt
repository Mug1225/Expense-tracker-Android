package com.example.expensetracker.ui.charts

import android.graphics.Paint
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.math.roundToInt

@Composable
fun LineChart(
    data: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        val maxAmount = data.maxOfOrNull { it.amount } ?: 100.0
        val minAmount = 0.0 // Always start from 0 for expenses? Or minAmount? Let's say 0.
        
        // Y-Axis scale
        val yRange = maxAmount - minAmount
        val yScale = if (yRange == 0.0) 1.0 else (height - padding * 2) / yRange
        
        // X-Axis scale
        val xStep = (width - padding * 2) / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        
        data.forEachIndexed { index, point ->
            val x = padding + index * xStep
            val y = height - padding - ((point.amount - minAmount) * yScale).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Draw points
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Draw X-labels (dates) - simplified to first, middle, last or step
            if (data.size < 7 || index % (data.size / 5) == 0) {
                 drawContext.canvas.nativeCanvas.drawText(
                    DateFormat.format("dd/MM", Date(point.dateMillis)).toString(),
                    x,
                    height - 10f,
                    Paint().apply {
                        color = labelColor
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Y-Axis labels (approx)
        val ySteps = 3
        for (i in 0..ySteps) {
            val value = minAmount + (yRange / ySteps) * i
            val y = height - padding - ((value - minAmount) * yScale).toFloat()
            drawContext.canvas.nativeCanvas.drawText(
                value.roundToInt().toString(),
                padding - 10f,
                y + 10f,
                Paint().apply {
                    color = labelColor
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                }
            )
        }
    }
}
