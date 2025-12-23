package com.example.expensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deletingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { editingCategory = category }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            IconHelper.getIcon(category.iconName),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditCategoryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, icon ->
                    viewModel.addCategory(name, icon) { success ->
                        if (success) showAddDialog = false
                        // Else: Show error (handled in dialog below)
                    }
                },
                viewModel = viewModel
            )
        }

        editingCategory?.let { category ->
            AddEditCategoryDialog(
                category = category,
                onDismiss = { editingCategory = null },
                onConfirm = { name, icon ->
                    viewModel.updateCategory(category.copy(name = name, iconName = icon)) { success ->
                        if (success) editingCategory = null
                    }
                },
                onDelete = { 
                    deletingCategory = category
                    editingCategory = null
                },
                viewModel = viewModel
            )
        }

        deletingCategory?.let { category ->
            DeleteCategoryDialog(
                category = category,
                onDismiss = { deletingCategory = null },
                onConfirm = { unlink ->
                    viewModel.deleteCategory(category, unlink)
                    deletingCategory = null
                }
            )
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    viewModel: TransactionViewModel
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var iconName by remember { mutableStateOf(category?.iconName ?: "Default") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        iconName = IconHelper.suggestIcon(it)
                        errorMessage = null
                    },
                    label = { Text("Category Name") },
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Icon Suggestion: $iconName", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(IconHelper.getIcon(iconName), contentDescription = null, modifier = Modifier.size(48.dp))
                }
                Text("Tap icon to change (Icon Picker TBD)", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isBlank()) {
                    errorMessage = "Name cannot be empty"
                } else {
                    onConfirm(name, iconName)
                }
            }) {
                Text(if (category == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun DeleteCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = {
            Column {
                Text("How would you like to handle existing transactions for '${category.name}'?")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onConfirm(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Keep money spent (Transactions keep category label)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onConfirm(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear categories (Transactions become unnamed)")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
