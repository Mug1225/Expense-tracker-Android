package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val merchant: String,
    val date: Long,
    val sender: String, // Bank sender ID
    val fullMessage: String
)
