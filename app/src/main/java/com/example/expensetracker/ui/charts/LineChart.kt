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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import kotlin.math.roundToInt
import java.util.Date

@Composable
fun LineChart(
    data: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // Calculate index from x-coordinate
                        val padding = 40.dp.toPx()
                        val width = size.width
                        val xStep = (width - padding * 2) / (data.size - 1).coerceAtLeast(1)
                        
                        val index = ((offset.x - padding) / xStep).roundToInt().coerceIn(0, data.lastIndex)
                        selectedPointIndex = index
                        tryAwaitRelease()
                        selectedPointIndex = null
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        val maxAmount = data.maxOfOrNull { it.amount } ?: 100.0
        val minAmount = 0.0
        
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
            
            // Draw X-labels (Start and End only, or selected)
            if (index == 0 || index == data.lastIndex) {
                 drawContext.canvas.nativeCanvas.drawText(
                    DateFormat.format("dd/MM", Date(point.dateMillis)).toString(),
                    x,
                    height - 10f,
                    Paint().apply {
                        color = labelColor
                        textSize = 12.sp.toPx()
                        textAlign = if (index == 0) Paint.Align.LEFT else Paint.Align.RIGHT
                    }
                )
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Highlight Selected Point
        selectedPointIndex?.let { index ->
            val point = data[index]
            val x = padding + index * xStep
            val y = height - padding - ((point.amount - minAmount) * yScale).toFloat()
            
            // Vertical Line
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x, padding),
                end = Offset(x, height - padding),
                strokeWidth = 2f
            )
            
            // Larger Circle
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Tooltip
            val labelText = "${DateFormat.format("dd/MM", Date(point.dateMillis))}: ${point.amount}"
            val textLayoutResult = textMeasurer.measure(
                text = androidx.compose.ui.text.AnnotatedString(labelText),
                style = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )
            val tooltipWidth = textLayoutResult.size.width + 16.dp.toPx()
            val tooltipHeight = textLayoutResult.size.height + 8.dp.toPx()
            
            // Calculate tooltip position (try to keep inside bounds)
            var tooltipX = x - tooltipWidth / 2
            if (tooltipX < 0) tooltipX = 10f
            if (tooltipX + tooltipWidth > width) tooltipX = width - tooltipWidth - 10f
            
            val tooltipY = if (y - tooltipHeight - 10f > 0) y - tooltipHeight - 10f else y + 20f

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(tooltipX, tooltipY),
                size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(tooltipX + 8.dp.toPx(), tooltipY + 4.dp.toPx()),
                style = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
            )
        }
        
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
