package com.optimisticbyte.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingLimitDao {
    @Query("SELECT * FROM spending_limits ORDER BY id DESC")
    fun getAllSpendingLimits(): Flow<List<SpendingLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(limit: SpendingLimit)

    @Update
    suspend fun update(limit: SpendingLimit)

    @Delete
    suspend fun delete(limit: SpendingLimit)
}
