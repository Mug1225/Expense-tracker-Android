package com.example.expensetracker.core

import com.example.expensetracker.data.Transaction
import java.util.regex.Pattern

object SmsParser {

    // Regex to capture amount: "Rs. 123.00", "INR 123", "Rs 123"
    // Also captures merchant name often found after "at" or "to"
    // This is a simplified parser and would need refinement for specific banks.
    
    // Format: "... debited ... Rs. 500.00 on 22-Dec to AMAZON PAY ..."
    
    private val AMOUNT_PATTERN = Pattern.compile("(?i)Rs\\.?\\s*([0-9,]+(?:\\.[0-9]{2})?)")
    private val DATE_PATTERN = Pattern.compile("(?i)on\\s+([0-9]{1,2}-[A-Za-z]{3}(?:-[0-9]{2,4})?)")
    private val MERCHANT_PATTERN = Pattern.compile("(?i)to\\s+(.+?)(?:\\.|\\s{2,}|$)")

    fun parseSms(sender: String, message: String, timestamp: Long): Transaction? {
        if (!message.contains("debited", ignoreCase = true) && !message.contains("spent", ignoreCase = true)) {
             return null
        }

        val amountMatcher = AMOUNT_PATTERN.matcher(message)
        val amount = if (amountMatcher.find()) {
            amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else {
            return null
        }

        val dateMatcher = DATE_PATTERN.matcher(message)
        // We still use current timestamp as primary, but we could try to parse the date string if needed.
        // For now, let's just stick to extracting the merchant name.
        
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
