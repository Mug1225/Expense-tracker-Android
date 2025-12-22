package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val merchantMappingDao: MerchantMappingDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allMappings: Flow<List<MerchantMapping>> = merchantMappingDao.getAllMappings()

    fun getTransactionsForMonth(startTime: Long, endTime: Long) = 
        transactionDao.getTransactionsForMonth(startTime, endTime)

    fun getTotalExpenseForMonth(startTime: Long, endTime: Long) = 
        transactionDao.getTotalExpenseForMonth(startTime, endTime)

    fun getExpensesByCategory(startTime: Long, endTime: Long) = 
        transactionDao.getExpensesByCategory(startTime, endTime)

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun addMapping(mapping: MerchantMapping) {
        merchantMappingDao.insertMapping(mapping)
    }

    suspend fun getMappingForMerchant(merchantName: String) = 
        merchantMappingDao.getMappingForMerchant(merchantName)
}
