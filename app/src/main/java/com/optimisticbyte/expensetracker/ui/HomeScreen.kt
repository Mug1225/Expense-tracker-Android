package com.optimisticbyte.expensetracker.ui

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.optimisticbyte.expensetracker.data.*
import com.optimisticbyte.expensetracker.utils.AmountUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel = viewModel(),
    onCategoryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSmsImportClick: () -> Unit,
    onLimitsClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val customDateRange by viewModel.customDateRange.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()
    val merchantFilter by viewModel.merchantFilter.collectAsState()
    
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedTransactionIds.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddManual by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showFilterSelectDialog by remember { mutableStateOf(false) }

    // Backup & Restore Launchers
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.backupData(context, uri) { success ->
                android.widget.Toast.makeText(context, if (success) "Backup Successful" else "Backup Failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreData(context, uri) { success ->
                android.widget.Toast.makeText(context, if (success) "Restore Successful" else "Restore Failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showDatePicker) {
// ...
    }
    
    // Handle Back Press to exit selection mode
    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // TopAppBar Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SpendWise", 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        
                        Row {
                            IconButton(onClick = onSearchClick) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = onLimitsClick) {
                                Icon(Icons.Default.Notifications, contentDescription = "Limits")
                            }
                            IconButton(onClick = { showThemeDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Import from SMS") },
                                        onClick = {
                                            onSmsImportClick()
                                            showOverflowMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Message, contentDescription = null)
                                        }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Filter & Select (Bulk Delete)") },
                                        onClick = {
                                            showOverflowMenu = false
                                            showFilterSelectDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.FilterList, contentDescription = null)
                                        }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Backup Data (Export)") },
                                        onClick = {
                                            exportLauncher.launch("spendwise_backup_${System.currentTimeMillis()}.json")
                                            showOverflowMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Save, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Restore Data (Import)") },
                                        onClick = {
                                            importLauncher.launch(arrayOf("application/json"))
                                            showOverflowMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Restore, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MonthPicker(
                            selectedMonth = selectedMonth,
                            customDateRange = customDateRange,
                            onPrev = { viewModel.prevMonth() },
                            onNext = { viewModel.nextMonth() },
                            onDateClick = { showDatePicker = true }
                        )

                        // Filter chips
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (filterCategoryId != null) {
                                val filteredCategory = categories.find { it.id == filterCategoryId }
                                AssistChip(
                                    onClick = { viewModel.setCategoryFilter(null) },
                                    label = { Text("${filteredCategory?.name}") },
                                    leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                            
                            if (merchantFilter != null) {
                                AssistChip(
                                    onClick = { viewModel.setMerchantFilter(null) },
                                    label = { Text("$merchantFilter") },
                                    leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Expense",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "Rs. ${AmountUtils.format(totalExpense ?: 0.0)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (categoryExpenses.isNotEmpty() && !isSelectionMode) {
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
                val isSelected = selectedIds.contains(transaction.id)
                TransactionItem(
                    transaction = transaction,
                    category = category,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            viewModel.toggleSelection(transaction.id)
                        } else {
                            onTransactionClick(transaction)
                        }
                    },
                    onLongClick = {
                        viewModel.toggleSelection(transaction.id)
                    }
                )
            }
            
            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = if (filterCategoryId != null) "No transactions for this category" else "No transactions found" + if (customDateRange != null) " for this date range" else " for this month",
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
                onConfirm = { amount, merchant, date, catId, tags, comment ->
                    viewModel.addManualTransaction(amount, merchant, date, catId, tags, comment)
                    showAddManual = false
                }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { theme ->
                    viewModel.setTheme(theme)
                    showThemeDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete ${selectedIds.size} Transaction(s)?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteSelectedTransactions()
                        showDeleteConfirmDialog = false
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showDatePicker) {
            DateRangePickerModal(
                onDismiss = { showDatePicker = false },
                onDateSelected = { start, end ->
                   val adjustedEnd = Calendar.getInstance().apply {
                        timeInMillis = end
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                    }.timeInMillis
                    
                    viewModel.setCustomDateRange(start, adjustedEnd)
                    showDatePicker = false
                }
            )
        }

        if (showFilterSelectDialog) {
            FilterSelectDialog(
                categories = categories,
                onDismiss = { showFilterSelectDialog = false },
                onConfirm = { start, end, catId, merchant ->
                    viewModel.selectTransactionsByFilter(start, end, catId, merchant)
                    showFilterSelectDialog = false
                }
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (com.optimisticbyte.expensetracker.ui.theme.AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                com.optimisticbyte.expensetracker.ui.theme.AppTheme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = false, // We don't verify current theme here for simplicity, or we can pass it
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = theme.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSelectDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Long?, Long?, Int?, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var merchantQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    if (showDatePicker) {
        DateRangePickerModal(
            onDismiss = { showDatePicker = false },
            onDateSelected = { start, end ->
                startDate = start
                endDate = Calendar.getInstance().apply {
                    timeInMillis = end
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                showDatePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Select") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select criteria to find transactions:")

                // Date Range
                OutlinedTextField(
                    value = if (startDate != null && endDate != null) {
                        "${DateFormat.format("dd/MM/yyyy", Date(startDate!!))} - ${DateFormat.format("dd/MM/yyyy", Date(endDate!!))}"
                    } else "All Dates",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date Range") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Dates")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )

                // Category Dropdown
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategory = null
                                expanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Merchant
                OutlinedTextField(
                    value = merchantQuery,
                    onValueChange = { merchantQuery = it },
                    label = { Text("Merchant / Description") },
                    placeholder = { Text("e.g. Uber") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        startDate,
                        endDate,
                        selectedCategory?.id,
                        merchantQuery.ifBlank { null }
                    )
                }
            ) {
                Text("Select Matching")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPicker(
    selectedMonth: Calendar,
    customDateRange: Pair<Long, Long>?,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.IconButton(onClick = onPrev) {
            androidx.compose.material3.Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
        }
        
        val displayText = if (customDateRange != null) {
            val start = DateFormat.format("dd MMM", Date(customDateRange.first))
            val end = DateFormat.format("dd MMM", Date(customDateRange.second))
            "$start - $end"
        } else {
            DateFormat.format("MMMM yyyy", selectedMonth.time).toString()
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onDateClick() }
        )
        androidx.compose.material3.IconButton(onClick = onNext) {
            androidx.compose.material3.Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDismiss: () -> Unit,
    onDateSelected: (Long, Long) -> Unit
) {
    val datePickerState = rememberDateRangePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = datePickerState.selectedStartDateMillis
                    val end = datePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onDateSelected(start, end)
                    }
                },
                enabled = datePickerState.selectedStartDateMillis != null && datePickerState.selectedEndDateMillis != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = datePickerState,
            modifier = Modifier.weight(1f)
        )
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
                        Text("Rs. ${AmountUtils.format(expense.totalAmount)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tagList = remember(transaction.tags) { 
        transaction.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val displayTitle = tagList.firstOrNull() ?: transaction.merchant

    // Animate color or change it based on selection
    val containerColor = if (isSelected) 
        MaterialTheme.colorScheme.secondaryContainer 
    else 
        MaterialTheme.colorScheme.surface

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If in selection mode, show checkbox (or mock one using icon)
                if (isSelectionMode) {
                   Checkbox(
                       checked = isSelected,
                       onCheckedChange = { onClick() }, // Intercepted by parent click
                       modifier = Modifier.padding(end = 16.dp)
                   )
                }

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
                    text = "Rs. ${AmountUtils.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

