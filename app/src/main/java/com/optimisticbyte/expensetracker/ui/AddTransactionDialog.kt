package com.optimisticbyte.expensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optimisticbyte.expensetracker.data.Category
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long, Int?, String?, String?) -> Unit // added comment parameter
) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var date by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showTags by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Preserve time, only update date
                        val originalCal = Calendar.getInstance().apply { timeInMillis = date }
                        val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                        
                        originalCal.set(Calendar.YEAR, newCal.get(Calendar.YEAR))
                        originalCal.set(Calendar.MONTH, newCal.get(Calendar.MONTH))
                        originalCal.set(Calendar.DAY_OF_MONTH, newCal.get(Calendar.DAY_OF_MONTH))
                        
                        date = originalCal.timeInMillis
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
        title = { Text("Add Manual Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                
                // Date Picker Field
                OutlinedTextField(
                    value = android.text.format.DateFormat.format("MMM dd, yyyy h:mm aa", date).toString(),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Edit Date")
                        }
                    }, 
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0.0) {
                    val finalMerchant = if (merchant.isBlank()) "Manual" else merchant
                    val finalTags = if (tags.isNotBlank()) tags else null
                    val finalComment = if (comment.isNotBlank()) comment else null
                    onConfirm(amt, finalMerchant, date, selectedCategoryId, finalTags, finalComment)
                }
            }, enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
