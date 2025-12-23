package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Category
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long, Int?, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var date by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                TextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant") }, modifier = Modifier.fillMaxWidth())
                TextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags (comma separated)") }, modifier = Modifier.fillMaxWidth())
                
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
                    onConfirm(amt, finalMerchant, date, selectedCategoryId, finalTags)
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
