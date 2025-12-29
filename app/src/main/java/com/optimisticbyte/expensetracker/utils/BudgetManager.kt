package com.optimisticbyte.expensetracker.utils

import com.optimisticbyte.expensetracker.data.LimitStatus
import com.optimisticbyte.expensetracker.data.SpendingLimit
import com.optimisticbyte.expensetracker.data.Transaction
import javax.inject.Inject
import java.util.Calendar

class BudgetManager @Inject constructor() {

    fun checkLimits(limits: List<SpendingLimit>, transactions: List<Transaction>): List<LimitStatus> {
        val statuses = mutableListOf<LimitStatus>()

        for (limit in limits) {
            val spent = calculateSpentAmount(limit, transactions)
            statuses.add(LimitStatus(limit, spent, spent > limit.amount))
        }

        return statuses
    }

    private fun calculateSpentAmount(limit: SpendingLimit, transactions: List<Transaction>): Double {
        // Filter transactions based on limit criteria (date range and categories)
        // For MVP, simple date check and category match
        
        // 1. Filter by Date (assuming limit.startDate/endDate are timestamps)
        // If Custom: use start/end.
        // If Monthly: we might need logic to determine "current month" relative to limit? 
        // Or assume limit stores the absolute range for the period it covers.
        // Let's assume for now limit has explicit start/end timestamps that are updated or set correctly.
        
        // Actually, for "Monthly" limits that recur, the logic usually checks against the *current* month.
        // But storing explicit start/end is easier for query. 
        // Let's stick to the definition: limit has start/end.
        
        val filteredByDate = transactions.filter { txn ->
            txn.date >= limit.startDate && txn.date <= limit.endDate
        }

        // 2. Filter by Category
        val relevantTransactions = if (limit.categoryIds == "ALL") {
            filteredByDate
        } else {
            val ids = limit.categoryIds.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            filteredByDate.filter { txn ->
                txn.categoryId != null && ids.contains(txn.categoryId)
            }
        }

        return relevantTransactions.sumOf { it.amount }
    }
}
