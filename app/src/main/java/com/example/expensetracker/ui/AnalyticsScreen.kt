package com.example.expensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Check
import com.example.expensetracker.ui.charts.DonutChart
import com.example.expensetracker.ui.charts.LineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: TransactionViewModel
) {
    val pieData by viewModel.pieChartData.collectAsState()
    val trendData by viewModel.trendLineData.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val customDateRange by viewModel.customDateRange.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // TopAppBar
        TopAppBar(
            title = { 
                Text(
                    "Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            actions = {
                // Category Filter Dropdown
                Box {
                    IconButton(onClick = { showCategoryMenu = true }) {
                        Icon(
                            if (filterCategoryId != null) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            contentDescription = "Filter by Category",
                            tint = if (filterCategoryId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.setCategoryFilter(null)
                                showCategoryMenu = false
                            },
                            leadingIcon = {
                                if (filterCategoryId == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Uncategorized") },
                            onClick = {
                                viewModel.setCategoryFilter(0)
                                showCategoryMenu = false
                            },
                            leadingIcon = {
                                if (filterCategoryId == 0) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        Divider()
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.setCategoryFilter(category.id)
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    if (filterCategoryId == category.id) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        // Reuse MonthPicker for consistency
        MonthPicker(
            selectedMonth = selectedMonth,
            customDateRange = customDateRange,
            onPrev = { viewModel.prevMonth() },
            onNext = { viewModel.nextMonth() },
            onDateClick = { showDatePicker = true }
        )

        if (filterCategoryId != null) {
            val catName = categories.find { it.id == filterCategoryId }?.name ?: "Unknown"
            Text(
                "Filter: $catName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Donut Chart Section
        Card(
            modifier = Modifier.fillMaxWidth(), // Removed fixed height
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Spending Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (pieData.isNotEmpty()) {
                    DonutChart(
                        data = pieData,
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Legend
                    Text("Details", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        pieData.forEach { slice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { /* Future: Drill down filter */ },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                                        drawCircle(color = slice.color)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = slice.label, 
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = String.format("%.1f%%", slice.percentage * 100), 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No data for period")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trend Line Section
        Card(
            modifier = Modifier.fillMaxWidth(),
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
             Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Daily Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (trendData.isNotEmpty() && trendData.size > 1) { // Need at least 2 points for a line
                    LineChart(
                        data = trendData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Fixed height for chart only
                    )
                } else {
                     Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Not enough data for trend")
                    }
                }
             }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for Nav
    }

    if (showDatePicker) {
        DateRangePickerModal(
            onDismiss = { showDatePicker = false },
            onDateSelected = { start, end ->
                 val adjustedEnd = java.util.Calendar.getInstance().apply {
                    timeInMillis = end
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                }.timeInMillis
                viewModel.setCustomDateRange(start, adjustedEnd)
                showDatePicker = false
            }
        )
    }
    }
}
