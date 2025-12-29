package com.optimisticbyte.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_limits")
data class SpendingLimit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val startDate: Long, // Start of the period (e.g. 1st of month)
    val endDate: Long,   // End of the period (e.g. last of month)
    val period: String, // "MONTHLY", "CUSTOM"
    val categoryIds: String // Comma separated IDs or "ALL"
)
