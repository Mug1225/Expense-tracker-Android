package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getTransactionsForMonth(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE date >= :startTime AND date <= :endTime")
    fun getTotalExpenseForMonth(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as totalAmount FROM transactions WHERE date >= :startTime AND date <= :endTime GROUP BY categoryId")
    fun getExpensesByCategory(startTime: Long, endTime: Long): Flow<List<CategoryExpense>>

    @Query("UPDATE transactions SET tags = :tags WHERE merchant = :merchantName")
    suspend fun updateTagsForMerchant(merchantName: String, tags: String?)

    @Delete
    suspend fun deleteTransactions(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE (:minDate IS NULL OR date >= :minDate) AND (:maxDate IS NULL OR date <= :maxDate) AND (:categoryId IS NULL OR categoryId = :categoryId) AND (:merchant IS NULL OR merchant LIKE '%' || :merchant || '%')")
    suspend fun getTransactionsByFilter(minDate: Long?, maxDate: Long?, categoryId: Int?, merchant: String?): List<Transaction>
}

data class CategoryExpense(
    val categoryId: Int?,
    val totalAmount: Double
)
