package com.example.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
}

@Dao
interface MerchantMappingDao {
    @Query("SELECT * FROM merchant_mappings")
    fun getAllMappings(): Flow<List<MerchantMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: MerchantMapping)

    @Query("SELECT * FROM merchant_mappings WHERE merchantName = :merchantName")
    suspend fun getMappingForMerchant(merchantName: String): MerchantMapping?
}
