package com.optimisticbyte.expensetracker.ui.charts

import androidx.compose.ui.graphics.Color

data class ChartData(
    val label: String,
    val value: Double,
    val color: Color,
    val percentage: Float
)

data class TrendPoint(
    val dateMillis: Long,
    val amount: Double
)
