package com.optimisticbyte.expensetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class, Category::class, MerchantMapping::class, SpendingLimit::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun spendingLimitDao(): SpendingLimitDao
}
