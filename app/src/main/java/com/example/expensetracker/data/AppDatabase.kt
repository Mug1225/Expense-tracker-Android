package com.example.expensetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class, Category::class, MerchantMapping::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
}
