package com.optimisticbyte.expensetracker

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
import com.optimisticbyte.expensetracker.ui.HomeScreen
import com.optimisticbyte.expensetracker.ui.AnalyticsScreen
import com.optimisticbyte.expensetracker.ui.PermissionRequestScreen
import com.optimisticbyte.expensetracker.ui.CategoryScreen
import com.optimisticbyte.expensetracker.ui.SearchScreen
import com.optimisticbyte.expensetracker.ui.SmsImportScreen
import com.optimisticbyte.expensetracker.ui.EditTransactionDialog
import com.optimisticbyte.expensetracker.ui.TransactionViewModel
import com.optimisticbyte.expensetracker.ui.theme.SpendWiseTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.optimisticbyte.expensetracker.data.Transaction
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import com.optimisticbyte.expensetracker.utils.rememberInAppUpdateManager
import com.google.android.play.core.install.model.InstallStatus
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val updateManager = rememberInAppUpdateManager()
    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            android.util.Log.e("MainActivity", "Update flow failed! Result code: ${result.resultCode}")
        }
    }

    LaunchedEffect(updateManager.installStatus) {
        if (updateManager.installStatus == InstallStatus.DOWNLOADED) {
            val result = snackbarHostState.showSnackbar(
                message = "An update has just been downloaded.",
                actionLabel = "RESTART",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                updateManager.completeUpdate()
            }
        }
    }

    LaunchedEffect(Unit) {
        updateManager.checkForUpdate { info ->
            updateManager.startFlexibleUpdate(
                context as android.app.Activity,
                info,
                updateLauncher
            )
        }
    }
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
                val permissions = mutableListOf(
                    Manifest.permission.READ_SMS, 
                    Manifest.permission.RECEIVE_SMS
                )
                if (android.os.Build.VERSION.SDK_INT >= 33) { // Android 13+
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                launcher.launch(permissions.toTypedArray())
            }
        } else {
             // BackHandler for Analytics/Limits -> Home
             androidx.activity.compose.BackHandler(enabled = currentScreen == "analytics" || currentScreen == "limits") {
                 currentScreen = "home"
                 currentTab = 0
             }
             
             Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            onLimitsClick = { currentScreen = "limits" },
                            onTransactionClick = { editingTransaction = it }
                        )
                        "analytics" -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                        "limits" -> com.optimisticbyte.expensetracker.ui.LimitsScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                currentScreen = "home"
                                currentTab = 0
                            }
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
