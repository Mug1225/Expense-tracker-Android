package com.example.expensetracker.utils

import com.example.expensetracker.data.LimitStatus
import com.example.expensetracker.data.SpendingLimit
import com.example.expensetracker.data.Transaction
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BudgetManagerTest {

    private lateinit var budgetManager: BudgetManager

    @Before
    fun setUp() {
        budgetManager = BudgetManager()
    }

    @Test
    fun `checkLimits returns correct status for breached limit`() {
        val limits = listOf(
            SpendingLimit(
                id = 1,
                name = "Food",
                amount = 100.0,
                startDate = 1000,
                endDate = 2000,
                period = "MONTHLY",
                categoryIds = "1"
            )
        )
        val transactions = listOf(
            Transaction(
                id = 1,
                amount = 150.0,
                merchant = "Grocery",
                date = 1500,
                sender = "",
                fullMessage = "",
                categoryId = 1,
                isEdited = false,
                tags = null
            )
        )

        val statuses = budgetManager.checkLimits(limits, transactions)

        assertEquals(1, statuses.size)
        assertTrue(statuses[0].isBreached)
        assertEquals(150.0, statuses[0].spentAmount, 0.01)
    }

    // Add more tests for non-breached, multiple categories, etc.
}
