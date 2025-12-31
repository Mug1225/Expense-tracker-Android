package com.optimisticbyte.expensetracker.utils

object AmountUtils {
    /**
     * Formats amount based on value:
     * - < 10,000: Shows 2 decimals
     * - >= 10,000: No decimals
     */
    fun format(amount: Double): String {
        return if (amount >= 10000) {
            String.format("%.0f", amount)
        } else {
            String.format("%.2f", amount)
        }
    }
}
