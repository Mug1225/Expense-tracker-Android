package com.optimisticbyte.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var amount: Double,
    var merchant: String,
    var date: Long,
    val sender: String, // Bank sender ID
    val fullMessage: String,
    var categoryId: Int? = null,
    var isEdited: Boolean = false,
    var tags: String? = null
)
