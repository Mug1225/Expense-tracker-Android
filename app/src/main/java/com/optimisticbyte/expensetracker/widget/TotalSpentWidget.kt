package com.optimisticbyte.expensetracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.optimisticbyte.expensetracker.data.AppDatabase
import com.optimisticbyte.expensetracker.utils.AmountUtils
import kotlinx.coroutines.flow.first
import java.util.*
import androidx.glance.appwidget.updateAll
// No GlanceTheme import needed if it causes clashing

import androidx.glance.appwidget.LinearProgressIndicator
import kotlinx.coroutines.flow.firstOrNull

class TotalSpentWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker_db"
        ).build()

        val (todayStart, todayEnd) = getTodayRange()
        val totalSpent = database.transactionDao().getTotalExpenseForMonth(todayStart, todayEnd).firstOrNull() ?: 0.0
        
        // Get the most relevant limit (highest progress)
        val limits = database.spendingLimitDao().getAllSpendingLimits().firstOrNull() ?: emptyList()
        val allTransactions = database.transactionDao().getAllTransactions().firstOrNull() ?: emptyList()
        
        val budgetManager = com.optimisticbyte.expensetracker.utils.BudgetManager()
        val statuses = budgetManager.checkLimits(limits, allTransactions)
        val topLimitStatus = statuses.maxByOrNull { (it.spentAmount / it.limit.amount) }

        provideContent {
            SpendWiseWidgetContent(totalSpent, topLimitStatus)
        }
    }

    @Composable
    private fun SpendWiseWidgetContent(totalSpent: Double, limitStatus: com.optimisticbyte.expensetracker.data.LimitStatus?) {
        val primaryColor = ColorProvider(Color(0xFF6750A4))
        val surfaceColor = ColorProvider(Color(0xFFFEF7FF))
        val onSurfaceColor = ColorProvider(Color(0xFF1D1B20))
        val secondaryColor = ColorProvider(Color(0xFF625B71))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colorProvider = surfaceColor)
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SpendWise",
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "Today",
                    style = TextStyle(
                        color = secondaryColor,
                        fontSize = 11.sp
                    )
                )
            }
            
            Spacer(GlanceModifier.height(12.dp))
            
            Text(
                text = "Rs. ${AmountUtils.format(totalSpent)}",
                style = TextStyle(
                    color = onSurfaceColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            if (limitStatus != null) {
                Spacer(GlanceModifier.height(16.dp))
                val progress = (limitStatus.spentAmount / limitStatus.limit.amount).toFloat().coerceIn(0f, 1f)
                val progressColor = when {
                    progress >= 0.9f -> Color.Red
                    progress >= 0.7f -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }

                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Budget: ${limitStatus.limit.name}",
                        style = TextStyle(
                            color = secondaryColor,
                            fontSize = 11.sp
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = TextStyle(
                            color = ColorProvider(progressColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(GlanceModifier.height(6.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                    color = ColorProvider(progressColor)
                )
            }
            
            Spacer(GlanceModifier.defaultWeight())
            
            Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Button(
                    text = "Refresh",
                    onClick = actionRunCallback<RefreshCallback>()
                )
            }
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        TotalSpentWidget().updateAll(context)
    }
}
