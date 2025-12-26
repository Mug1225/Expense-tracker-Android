package com.example.expensetracker

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.expensetracker.ui.HomeScreen
import com.example.expensetracker.ui.AnalyticsScreen
import com.example.expensetracker.ui.PermissionRequestScreen
import com.example.expensetracker.ui.CategoryScreen
import com.example.expensetracker.ui.SearchScreen
import com.example.expensetracker.ui.SmsImportScreen
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
    var hasSmsPermission by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("home") } // "home", "analytics", "categories"
    var currentTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Analytics
    
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
             // BackHandler for Analytics -> Home
             androidx.activity.compose.BackHandler(enabled = currentScreen == "analytics") {
                 currentScreen = "home"
                 currentTab = 0
             }
             
             Scaffold(
                bottomBar = {
                    if (currentScreen == "home" || currentScreen == "analytics") {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                selected = currentTab == 0,
                                onClick = { 
                                    currentTab = 0 
                                    currentScreen = "home"
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.PieChart, contentDescription = "Analytics") },
                                label = { Text("Analytics") },
                                selected = currentTab == 1,
                                onClick = { 
                                    currentTab = 1
                                    currentScreen = "analytics"
                                }
                            )
                        }
                    }
                }
             ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            onCategoryClick = { currentScreen = "categories" },
                            onSearchClick = { currentScreen = "search" },
                            onSmsImportClick = { currentScreen = "smsImport" },
                            onTransactionClick = { editingTransaction = it }
                        )
                        "analytics" -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                        "categories" -> CategoryScreen(
                            viewModel = viewModel,
                            onBack = { 
                                currentScreen = "home"
                                currentTab = 0
                            }
                        )
                        "search" -> SearchScreen(
                            viewModel = viewModel,
                            onBack = { 
                                currentScreen = "home"
                                currentTab = 0
                            },
                            onMerchantSelected = { merchantName ->
                                viewModel.setMerchantFilter(merchantName)
                            }
                        )
                        "smsImport" -> SmsImportScreen(
                            viewModel = viewModel,
                            onBack = {
                                currentScreen = "home"
                                currentTab = 0
                            }
                        )
                    }
                }
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
