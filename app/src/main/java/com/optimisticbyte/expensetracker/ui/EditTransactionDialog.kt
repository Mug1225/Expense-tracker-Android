package com.optimisticbyte.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optimisticbyte.expensetracker.data.Category
import com.optimisticbyte.expensetracker.data.Transaction

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Transaction, Int?, String?) -> Unit, // Int? is categoryId, String? is tags
    onDelete: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var merchant by remember { mutableStateOf(transaction.merchant) }
    var selectedCategoryId by remember { mutableStateOf(transaction.categoryId) }
    var tags by remember { mutableStateOf(transaction.tags ?: "") }
    var comment by remember { mutableStateOf(transaction.comment ?: "") }
    var selectedDate by remember { mutableStateOf(transaction.date) }
    var saveMapping by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showTags by remember { mutableStateOf(transaction.tags?.isNotBlank() == true) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Preserve time, only update date
                        val originalCal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDate }
                        val newCal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        
                        originalCal.set(java.util.Calendar.YEAR, newCal.get(java.util.Calendar.YEAR))
                        originalCal.set(java.util.Calendar.MONTH, newCal.get(java.util.Calendar.MONTH))
                        originalCal.set(java.util.Calendar.DAY_OF_MONTH, newCal.get(java.util.Calendar.DAY_OF_MONTH))
                        
                        selectedDate = originalCal.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCalculator) {
        CalculatorDialog(
            initialValue = amount,
            onDismiss = { showCalculator = false },
            onConfirm = { 
                amount = it
                showCalculator = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Original Message:", style = MaterialTheme.typography.labelLarge)
                Text(transaction.fullMessage, style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date Picker Field
                OutlinedTextField(
                    value = android.text.format.DateFormat.format("MMM dd, yyyy h:mm aa", selectedDate).toString(),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }, // This clickable might be blocked by TextField
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Edit Date")
                        }
                    }, 
                    enabled = false, // Disable typing, handling click via IconButton or wrapping Box
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                TextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("Amount") }, 
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showCalculator = true }) {
                            Icon(Icons.Default.Calculate, contentDescription = "Calculate")
                        }
                    }
                )

                TextField(
                    value = merchant, 
                    onValueChange = { merchant = it }, 
                    label = { Text("Merchant") }, 
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showTags = !showTags }) {
                            Icon(Icons.Default.Label, contentDescription = "Toggle Tags", tint = if (showTags) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )

                if (showTags) {
                    TextField(
                        value = tags, 
                        onValueChange = { tags = it }, 
                        label = { Text("Tags (comma separated)") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) }
                )
                
                Text("Category", style = MaterialTheme.typography.labelLarge)
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                        Text(category.name)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = saveMapping, onCheckedChange = { saveMapping = it })
                    Text("Always use this details for '$merchant'")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = transaction.copy(
                    amount = amount.toDoubleOrNull() ?: transaction.amount,
                    merchant = merchant,
                    categoryId = selectedCategoryId,
                    isEdited = true,
                    date = selectedDate,
                    tags = if (tags.isNotBlank()) tags else null,
                    comment = if (comment.isNotBlank()) comment else null
                )
                onSave(updated, if (saveMapping) selectedCategoryId else null, if (saveMapping) tags else null)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { onDelete(transaction) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
