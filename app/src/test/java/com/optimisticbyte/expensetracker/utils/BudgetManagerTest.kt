package com.optimisticbyte.expensetracker.utils

import com.optimisticbyte.expensetracker.data.LimitStatus
import com.optimisticbyte.expensetracker.data.SpendingLimit
import com.optimisticbyte.expensetracker.data.Transaction
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

    @Test
    fun `checkLimits correctly sums multiple categories`() {
        val limits = listOf(
            SpendingLimit(
                id = 1,
                name = "Leisure",
                amount = 500.0,
                startDate = 1000,
                endDate = 2000,
                period = "MONTHLY",
                categoryIds = "1,2"
            )
        )
        val transactions = listOf(
            Transaction(id = 1, amount = 100.0, merchant = "Movie", date = 1500, sender = "", fullMessage = "", categoryId = 1, isEdited = false, tags = null),
            Transaction(id = 2, amount = 200.0, merchant = "Lunch", date = 1600, sender = "", fullMessage = "", categoryId = 2, isEdited = false, tags = null),
            Transaction(id = 3, amount = 50.0,  merchant = "Other", date = 1700, sender = "", fullMessage = "", categoryId = 3, isEdited = false, tags = null)
        )

        val statuses = budgetManager.checkLimits(limits, transactions)

        assertEquals(1, statuses.size)
        assertEquals(300.0, statuses[0].spentAmount, 0.01)
        assertFalse(statuses[0].isBreached)
    }
}
