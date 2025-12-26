package com.example.expensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.SmsMessage
import com.example.expensetracker.utils.SmsInboxReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsImportScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var isLoading by remember { mutableStateOf(true) }
    var daysBack by remember { mutableStateOf(30) }
    var showDaysDialog by remember { mutableStateOf(false) }
    
    // Load messages on first composition
    LaunchedEffect(daysBack) {
        isLoading = true
        scope.launch {
            messages = SmsInboxReader.getRecentMessages(context, daysBack)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from SMS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDaysDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            if (selectedIds.isNotEmpty()) {
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedIds.size} selected",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = {
                                val selectedMessages = messages.filter { it.id in selectedIds }
                                viewModel.importFromSms(selectedMessages)
                                onBack()
                            }
                        ) {
                            Text("Import ${selectedIds.size}")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No transaction messages found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try adjusting the date range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "Last $daysBack days • ${messages.size} messages",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(messages, key = { it.id }) { message ->
                            SmsMessageCard(
                                message = message,
                                isSelected = message.id in selectedIds,
                                onToggle = {
                                    selectedIds = if (message.id in selectedIds) {
                                        selectedIds - message.id
                                    } else {
                                        selectedIds + message.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDaysDialog) {
        DaysBackDialog(
            currentDays = daysBack,
            onDismiss = { showDaysDialog = false },
            onConfirm = { newDays ->
                daysBack = newDays
                selectedIds = setOf() // Clear selection
                showDaysDialog = false
            }
        )
    }
}

@Composable
fun SmsMessageCard(
    message: SmsMessage,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = android.text.format.DateFormat.format("MMM dd, yyyy h:mm aa", message.date).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun DaysBackDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    val options = listOf(7, 15, 30, 60, 90, 180)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Date Range") },
        text = {
            Column {
                Text("Select how far back to search for SMS messages:")
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { days ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDays = days }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$days days")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDays) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
