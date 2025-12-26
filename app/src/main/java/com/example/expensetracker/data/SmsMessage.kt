package com.example.expensetracker.data

data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val date: Long,
    val isRead: Boolean
)
