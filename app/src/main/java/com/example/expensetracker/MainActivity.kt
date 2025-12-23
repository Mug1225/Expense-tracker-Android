package com.example.expensetracker

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.expensetracker.ui.HomeScreen
import com.example.expensetracker.ui.PermissionRequestScreen
import com.example.expensetracker.ui.CategoryScreen
import com.example.expensetracker.ui.EditTransactionDialog
import com.example.expensetracker.ui.TransactionViewModel
import com.example.expensetracker.ui.theme.SpendWiseTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.expensetracker.data.Transaction
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TransactionViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()
            
            SpendWiseTheme(theme = currentTheme) {
                MainContent(viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: TransactionViewModel = viewModel()) {
    var hasSmsPermission by remember {
        mutableStateOf(false)
    }
    
    // Simple state-based navigation
    var currentScreen by remember { mutableStateOf("home") }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val categories by viewModel.categories.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.READ_SMS] == true &&
                permissions[Manifest.permission.RECEIVE_SMS] == true
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val readGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val receiveGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        hasSmsPermission = readGranted && receiveGranted
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!hasSmsPermission) {
            PermissionRequestScreen {
                launcher.launch(
                    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                )
            }
        } else {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onCategoryClick = { currentScreen = "categories" },
                    onTransactionClick = { editingTransaction = it }
                )
                "categories" -> CategoryScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" }
                )
            }
        }
    }

        editingTransaction?.let { transaction ->
            EditTransactionDialog(
                transaction = transaction,
                categories = categories,
                onDismiss = { editingTransaction = null },
                onSave = { updated, mappingCategoryId, tags ->
                    viewModel.updateTransaction(updated)
                    mappingCategoryId?.let { 
                        viewModel.saveMerchantMapping(updated.merchant, it, tags)
                    }
                    editingTransaction = null
                },
                onDelete = {
                    viewModel.deleteTransaction(it)
                    editingTransaction = null
                }
            )
        }
}
