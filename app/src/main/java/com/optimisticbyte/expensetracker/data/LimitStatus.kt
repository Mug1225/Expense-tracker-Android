package com.optimisticbyte.expensetracker.data

data class LimitStatus(
    val limit: SpendingLimit,
    val spentAmount: Double,
    val isBreached: Boolean
)
