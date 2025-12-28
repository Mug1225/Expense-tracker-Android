package com.example.expensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.LimitStatus
import com.example.expensetracker.data.SpendingLimit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(
    viewModel: TransactionViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val limitStatuses by viewModel.limitStatuses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showLimitDialog by remember { mutableStateOf(false) }
    var limitToEdit by remember { mutableStateOf<SpendingLimit?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Limits") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    limitToEdit = null
                    showLimitDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Limit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (limitStatuses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No limits set. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(limitStatuses) { status ->
                        LimitCard(
                            status = status,
                            onEdit = {
                                limitToEdit = status.limit
                                showLimitDialog = true
                            },
                            onDelete = { viewModel.deleteSpendingLimit(status.limit) }
                        )
                    }
                }
            }
        }

        if (showLimitDialog) {
            LimitDialog(
                categories = categories,
                limitToEdit = limitToEdit,
                onDismiss = { showLimitDialog = false },
                onConfirm = { limit ->
                    if (limit.id == 0) {
                        viewModel.addSpendingLimit(limit)
                    } else {
                        viewModel.updateSpendingLimit(limit)
                    }
                    showLimitDialog = false
                }
            )
        }
    }
}

@Composable
fun LimitCard(
    status: LimitStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = (status.spentAmount / status.limit.amount).toFloat().coerceIn(0f, 1f)
    val color = if (status.isBreached) Color.Red else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status.limit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: ${status.spentAmount}")
                Text("Limit: ${status.limit.amount}")
            }
            if (status.isBreached) {
                Text(
                    text = "Limit Exceeded!",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitDialog(
    categories: List<com.example.expensetracker.data.Category>,
    limitToEdit: SpendingLimit? = null,
    onDismiss: () -> Unit,
    onConfirm: (SpendingLimit) -> Unit
) {
    var name by remember { mutableStateOf(limitToEdit?.name ?: "") }
    var amount by remember { mutableStateOf(limitToEdit?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(
        if (limitToEdit != null && limitToEdit.categoryIds != "ALL") {
            categories.find { it.id.toString() == limitToEdit.categoryIds }
        } else null
    ) }
    
    // Date Logic
    var isCustomDate by remember { mutableStateOf(limitToEdit != null && limitToEdit.endDate != Long.MAX_VALUE) }
    var startDate by remember { mutableStateOf(limitToEdit?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(limitToEdit?.endDate ?: System.currentTimeMillis()) }
    
    var expanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis ?: startDate
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = datePickerState.selectedDateMillis ?: endDate
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (limitToEdit == null) "Add Spending Limit" else "Edit Spending Limit") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "All Categories",
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = { selectedCategory = null; expanded = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; expanded = false }
                            )
                        }
                    }
                }

                // Date Range Mode
                Text("Duration", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isCustomDate, onClick = { isCustomDate = false })
                    Text("Ongoing (From Now)", modifier = Modifier.clickable { isCustomDate = false })
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = isCustomDate, onClick = { isCustomDate = true })
                    Text("Custom Range", modifier = Modifier.clickable { isCustomDate = true })
                }

                if (isCustomDate) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         OutlinedTextField(
                            value = dateFormatter.format(startDate),
                            onValueChange = {},
                            label = { Text("Start Date") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Start Date")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        OutlinedTextField(
                            value = dateFormatter.format(endDate),
                            onValueChange = {},
                            label = { Text("End Date") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showEndDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "End Date")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (startDate > endDate) {
                        Text(
                            text = "Start date cannot be after end date",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (name.isNotBlank() && amt != null) {
                        val finalStartDate = if (isCustomDate) startDate else {
                             // Force start of today for Ongoing
                             Calendar.getInstance().apply {
                                 set(Calendar.HOUR_OF_DAY, 0)
                                 set(Calendar.MINUTE, 0)
                                 set(Calendar.SECOND, 0)
                                 set(Calendar.MILLISECOND, 0)
                             }.timeInMillis
                        }
                        val finalEndDate = if (isCustomDate) endDate else Long.MAX_VALUE
                        
                        val newLimit = limitToEdit?.copy(
                            name = name,
                            amount = amt,
                            categoryIds = selectedCategory?.id?.toString() ?: "ALL",
                            startDate = finalStartDate,
                            endDate = finalEndDate
                        ) ?: SpendingLimit(
                            name = name,
                            amount = amt,
                            startDate = finalStartDate,
                            endDate = finalEndDate,
                            period = "MONTHLY",
                            categoryIds = selectedCategory?.id?.toString() ?: "ALL"
                        )
                        
                        onConfirm(newLimit)
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null && (!isCustomDate || startDate <= endDate)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
