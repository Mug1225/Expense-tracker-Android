package com.optimisticbyte.expensetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class, Category::class, MerchantMapping::class, SpendingLimit::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun spendingLimitDao(): SpendingLimitDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN comment TEXT")
            }
        }
    }
}
