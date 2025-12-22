package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
}
