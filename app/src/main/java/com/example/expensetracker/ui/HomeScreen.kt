package com.example.expensetracker.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel = viewModel(),
    onCategoryClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddManual by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "SpendWise", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                
                MonthPicker(
                    selectedMonth = selectedMonth,
                    onPrev = { viewModel.prevMonth() },
                    onNext = { viewModel.nextMonth() }
                )

                if (filterCategoryId != null) {
                    val filteredCategory = categories.find { it.id == filterCategoryId }
                    AssistChip(
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("Filtering: ${filteredCategory?.name}") },
                        leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "Total Expense",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Rs. ${totalExpense ?: 0.0}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showAddManual = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Manual")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(onClick = onCategoryClick) {
                    Icon(Icons.Default.Category, contentDescription = "Manage Categories")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (categoryExpenses.isNotEmpty()) {
                item {
                    CategorySummary(
                        categoryExpenses, 
                        categories,
                        onCategoryClick = { viewModel.setCategoryFilter(if (it == -1) null else it) },
                        selectedCategoryId = filterCategoryId
                    )
                }
            }

            items(transactions) { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                TransactionItem(
                    transaction = transaction,
                    category = category,
                    onClick = { onTransactionClick(transaction) }
                )
            }
            
            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = if (filterCategoryId != null) "No transactions for this category" else "No transactions found for this month",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (showAddManual) {
            AddTransactionDialog(
                categories = categories,
                onDismiss = { showAddManual = false },
                onConfirm = { amount, merchant, date, catId, tags ->
                    viewModel.addManualTransaction(amount, merchant, date, catId, tags)
                    showAddManual = false
                }
            )
        }
    }
}

@Composable
fun MonthPicker(
    selectedMonth: Calendar,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.IconButton(onClick = onPrev) {
            androidx.compose.material3.Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
        }
        Text(
            text = DateFormat.format("MMMM yyyy", selectedMonth.time).toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        androidx.compose.material3.IconButton(onClick = onNext) {
            androidx.compose.material3.Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun CategorySummary(
    expenses: List<CategoryExpense>,
    categories: List<Category>,
    onCategoryClick: (Int) -> Unit,
    selectedCategoryId: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("By Category (Tap to filter)", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            expenses.forEach { expense ->
                val category = categories.find { it.id == expense.categoryId }
                val isSelected = selectedCategoryId == expense.categoryId
                Surface(
                    onClick = { onCategoryClick(expense.categoryId ?: -1) },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                IconHelper.getIcon(category?.iconName),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category?.name ?: "Uncategorized",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text("Rs. ${expense.totalAmount}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    val tagList = remember(transaction.tags) { 
        transaction.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val displayTitle = tagList.firstOrNull() ?: transaction.merchant

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Icon(
                        imageVector = IconHelper.getIcon(category?.iconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = category?.name ?: "No Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Text(
                    text = "Rs. ${transaction.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = DateFormat.format("dd MMM yyyy, hh:mm a", Date(transaction.date)).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = androidx.compose.ui.unit.TextUnit.Unspecified // Default or small
                    )
                    if (transaction.isEdited) {
                        Text(
                            text = "Edited",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
