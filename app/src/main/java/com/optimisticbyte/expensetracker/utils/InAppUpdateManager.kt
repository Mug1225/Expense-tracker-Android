package com.optimisticbyte.expensetracker.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

private const val TAG = "InAppUpdateManager"

@Composable
fun rememberInAppUpdateManager(): InAppUpdateManager {
    val context = LocalContext.current
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    val updateManager = remember { InAppUpdateManager(context, appUpdateManager) }

    DisposableEffect(Unit) {
        updateManager.registerListener()
        onDispose {
            updateManager.unregisterListener()
        }
    }

    return updateManager
}

class InAppUpdateManager(
    private val context: Context,
    private val appUpdateManager: AppUpdateManager
) {
    var updateInfo by mutableStateOf<AppUpdateInfo?>(null)
        private set

    var installStatus by mutableStateOf<Int>(InstallStatus.UNKNOWN)
        private set

    private val listener = InstallStateUpdatedListener { state ->
        installStatus = state.installStatus()
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Log.d(TAG, "Update downloaded")
        }
    }

    fun registerListener() {
        appUpdateManager.registerListener(listener)
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(listener)
    }

    fun checkForUpdate(onUpdateAvailable: (AppUpdateInfo) -> Unit = {}) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateInfo = info
                onUpdateAvailable(info)
            } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                installStatus = InstallStatus.DOWNLOADED
            }
        }
    }

    fun startFlexibleUpdate(activity: Activity, info: AppUpdateInfo, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.startUpdateFlowForResult(
            info,
            launcher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        )
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}
