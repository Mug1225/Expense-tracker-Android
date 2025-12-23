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

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategoryWithStrategy(category: Category, unlinkTransactions: Boolean) {
        if (unlinkTransactions) {
            categoryDao.unlinkTransactions(category.id)
        }
        categoryDao.deleteCategory(category)
    }

    suspend fun isCategoryNameDuplicate(name: String): Boolean {
        return categoryDao.isNameDuplicate(name)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun addMapping(mapping: MerchantMapping) {
        merchantMappingDao.insertMapping(mapping)
        transactionDao.updateTagsForMerchant(mapping.merchantName, mapping.tags)
    }

    suspend fun getMappingForMerchant(merchantName: String) = 
        merchantMappingDao.getMappingForMerchant(merchantName)
}
