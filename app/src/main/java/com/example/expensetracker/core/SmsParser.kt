package com.example.expensetracker.core

import com.example.expensetracker.data.Transaction
import java.util.regex.Pattern

object SmsParser {

    // Regex to capture amount: "Rs. 123.00", "INR 123", "Rs 123"
    // Also captures merchant name often found after "at" or "to"
    // This is a simplified parser and would need refinement for specific banks.
    
    // Example: "Acct XX123 debited for Rs 500.00 on 12-Dec-23 at AMAZON. Bal Rs 1000"
    
    private val AMOUNT_PATTERN = Pattern.compile("(?i)(?:Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{2})?)")
    private val MERCHANT_PATTERN = Pattern.compile("(?i)(?:at|to|info)\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s|$)")

    fun parseSms(sender: String, message: String, timestamp: Long): Transaction? {
        // Basic filter: Only reliable bank senders usually headers like "XY-BANK"
        // For now, we process everything that looks like a debit.
        
        if (!message.contains("debited", ignoreCase = true) && !message.contains("spent", ignoreCase = true)) {
             return null
        }

        val amountMatcher = AMOUNT_PATTERN.matcher(message)
        val amount = if (amountMatcher.find()) {
            amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else {
            return null
        }

        val merchantMatcher = MERCHANT_PATTERN.matcher(message)
        val merchant = if (merchantMatcher.find()) {
            merchantMatcher.group(1)?.trim() ?: "Unknown"
        } else {
            "Unknown"
        }

        return Transaction(
            amount = amount,
            merchant = merchant,
            date = timestamp,
            sender = sender,
            fullMessage = message
        )
    }
}
