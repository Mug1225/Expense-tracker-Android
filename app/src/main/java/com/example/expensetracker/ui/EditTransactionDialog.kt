package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Transaction, Int?) -> Unit // Int? is the mapping categoryId if user wants to save mapping
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var merchant by remember { mutableStateOf(transaction.merchant) }
    var selectedCategoryId by remember { mutableStateOf(transaction.categoryId) }
    var saveMapping by remember { mutableStateOf(false) }

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
                
                TextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                TextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant") })
                
                Text("Category", style = MaterialTheme.typography.labelLarge)
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                        Text(category.name)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = saveMapping, onCheckedChange = { saveMapping = it })
                    Text("Always use this category for '$merchant'")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = transaction.copy(
                    amount = amount.toDoubleOrNull() ?: transaction.amount,
                    merchant = merchant,
                    categoryId = selectedCategoryId,
                    isEdited = true
                )
                onSave(updated, if (saveMapping) selectedCategoryId else null)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
