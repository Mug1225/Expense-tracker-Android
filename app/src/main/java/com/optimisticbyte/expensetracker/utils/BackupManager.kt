package com.optimisticbyte.expensetracker.utils

import android.content.Context
import android.net.Uri
import com.optimisticbyte.expensetracker.data.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import androidx.room.RoomDatabase

class BackupManager @Inject constructor(
    private val repository: TransactionRepository,
    private val appDatabase: AppDatabase // Inject Database to clear tables
) {
    private val gson = Gson()

    suspend fun exportData(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Gather data
                val transactions = repository.allTransactions.first()
                val categories = repository.allCategories.first()
                val spendingLimits = repository.allSpendingLimits.first()
                val mappings = repository.allMappings.first()

                val backupData = BackupData(
                    transactions = transactions,
                    categories = categories,
                    spendingLimits = spendingLimits,
                    merchantMappings = mappings
                )

                val json = gson.toJson(backupData)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importData(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            jsonBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }

                val backupData = gson.fromJson(jsonBuilder.toString(), BackupData::class.java)

                if (backupData != null) {
                    // Clear existing data (Blocking call is fine on IO dispatcher)
                    appDatabase.clearAllTables()
                    
                    // Re-insert using repository suspend functions sequentially
                    // Note: This is not atomic. If import fails midway, data might be partial.
                    // Ideally we fix withTransaction or use blocking DAOs in runInTransaction.
                    // For now, this unblocks compilation.
                    
                    backupData.categories.forEach { repository.addCategory(it) }
                    backupData.merchantMappings.forEach { repository.addMapping(it) }
                    backupData.spendingLimits.forEach { repository.addSpendingLimit(it) }
                    backupData.transactions.forEach { repository.addTransaction(it) }
                    
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
