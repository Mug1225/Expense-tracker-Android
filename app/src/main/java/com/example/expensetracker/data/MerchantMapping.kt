package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_mappings")
data class MerchantMapping(
    @PrimaryKey val merchantName: String,
    val categoryId: Int,
    val tags: String? = null
)
