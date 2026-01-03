package com.optimisticbyte.expensetracker.core

import com.optimisticbyte.expensetracker.data.Transaction
import java.util.regex.Pattern

/**
 * Enhanced SMS Parser for Indian Bank Transaction Messages
 * Supports: HDFC, ICICI, SBI, Axis and other major banks
 * Handles: UPI, NEFT, IMPS, Card, ATM, POS transactions
 */
object SmsParser {

    /**
     * Main parsing function - parses SMS and returns Transaction if it's a DEBIT transaction
     * Credit transactions are parsed but not returned (for future income tracking)
     */
    fun parseSms(sender: String, message: String, timestamp: Long): Transaction? {
        // Check if this is a transaction message
        if (!BankSmsPatterns.isTransactionMessage(message)) {
            return null
        }

        // Determine transaction type
        val transactionType = BankSmsPatterns.getTransactionType(message)
        
        // Only process DEBIT transactions for expense tracking
        if (transactionType != "DEBIT") {
            return null
        }

        // Extract amount
        val amount = extractAmount(message) ?: return null

        // Extract merchant/payee
        val merchant = extractMerchant(sender, message)

        // Extract date - use system timestamp as primary for now
        // Actual date parsing from string returns String, which causes type mismatch
        val transactionDate = timestamp

        return Transaction(
            amount = amount,
            merchant = merchant,
            date = transactionDate,
            sender = sender,
            fullMessage = message
        )
    }

    /**
     * Extracts amount from message
     */
    private fun extractAmount(message: String): Double? {
        // 1. Try POS amount pattern (e.g., "Amount (INR) 985.00")
        var matcher = BankSmsPatterns.AMOUNT_POS_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        // 2. Try standard amount pattern
        matcher = BankSmsPatterns.AMOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        // 3. Try "by/of" pattern
        matcher = BankSmsPatterns.AMOUNT_BY_OF_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        return null
    }

    /**
     * Extracts merchant/payee name from message using a hierarchical approach:
     * 1. UPI ID/Merchant (High priority)
     * 2. Bank-Specific Patterns
     * 3. Generic Keywords
     */
    private fun extractMerchant(sender: String, message: String): String {
        // 1. UPI Priority
        if (BankSmsPatterns.UPI_PATTERN.matcher(message).find()) {
            val upiId = extractUpiId(message)
            if (upiId != null) {
                val name = upiId.substringBefore("@")
                if (name.length > 2) return cleanMerchantName(name)
            }
        }

        // 2. Bank-Specific Logic
        val bank = identifyBankCombined(sender, message)
        val bankMerchant = when (bank) {
            "HDFC" -> extractHdfcMerchant(message)
            "ICICI" -> extractIciciMerchant(message)
            "SBI" -> extractSbiMerchant(message)
            "Axis" -> extractAxisMerchant(message)
            "Indian Bank" -> extractIndianBankMerchant(message)
            else -> null
        }
        if (!bankMerchant.isNullOrBlank()) {
            val cleaned = cleanMerchantName(bankMerchant)
            if (cleaned != "Unknown") return cleaned
        }

        // Spoofing / Multi-bank mention protection: 
        // If specified bank failed, check if another bank is mentioned in the content
        val mentionBank = identifyBankFromContent(message)
        if (mentionBank != "Unknown" && mentionBank != bank) {
            val alternativeMerchant = when (mentionBank) {
                "HDFC" -> extractHdfcMerchant(message)
                "ICICI" -> extractIciciMerchant(message)
                "SBI" -> extractSbiMerchant(message)
                "Axis" -> extractAxisMerchant(message)
                "Indian Bank" -> extractIndianBankMerchant(message)
                else -> null
            }
            if (!alternativeMerchant.isNullOrBlank()) {
                val cleanedAlt = cleanMerchantName(alternativeMerchant)
                if (cleanedAlt != "Unknown") return cleanedAlt
            }
        }

        // 3. Generic Keywords (Fallback)
        val genericMerchant = extractGenericMerchant(message)
        return cleanMerchantName(genericMerchant)
    }

    private fun identifyBankFromContent(message: String): String {
        val lowerMessage = message.lowercase()
        return when {
            lowerMessage.contains("hdfc") -> "HDFC"
            lowerMessage.contains("icici") -> "ICICI"
            lowerMessage.contains("sbi") || lowerMessage.contains("state bank") || lowerMessage.contains("statbk") -> "SBI"
            lowerMessage.contains("axis") -> "Axis"
            lowerMessage.contains("indian bank") || lowerMessage.contains("indibk") -> "Indian Bank"
            else -> "Unknown"
        }
    }

    private fun identifyBankCombined(sender: String, message: String): String {
        // Try sender ID first
        val bankBySender = BankSmsPatterns.identifyBank(sender)
        if (bankBySender != "Unknown") return bankBySender

        // Fallback to message content
        val lowerMessage = message.lowercase()
        return when {
            lowerMessage.contains("hdfc") -> "HDFC"
            lowerMessage.contains("icici") -> "ICICI"
            lowerMessage.contains("sbi") || lowerMessage.contains("state bank") || lowerMessage.contains("statbk") -> "SBI"
            lowerMessage.contains("axis") -> "Axis"
            lowerMessage.contains("indian bank") || lowerMessage.contains("indibk") -> "Indian Bank"
            else -> "Unknown"
        }
    }

    private fun extractHdfcMerchant(message: String): String? {
        // HDFC Netbanking
        if (message.contains("NetBanking", ignoreCase = true)) {
            val netMatch = Pattern.compile("(?i)on account of\\s+([A-Za-z0-9\\s&.'-]{3,50})(?:\\s+transaction|\\s+using|\\.)")
                .matcher(message)
            if (netMatch.find()) return netMatch.group(1)
        }

        // Spent Rs.17072 On HDFC Bank Card 0511 At 84 ZIMSON SHOPPING ARCADE On 2025-10-04
        val atMatcher = Pattern.compile("(?i)\\bat\\s*:?\\s*([A-Za-z0-9][A-Za-z0-9\\s&.',-]{2,40}?)(?:\\s+on|\\.|\\s{2,}|$)")
            .matcher(message)
        if (atMatcher.find()) return atMatcher.group(1)

        // Sent Rs.2.00 ... To Mr Mugesh
        val toMatcher = BankSmsPatterns.PAYEE_TO_PATTERN.matcher(message)
        if (toMatcher.find()) return toMatcher.group(1)

        return null
    }

    private fun extractIciciMerchant(message: String): String? {
        // 1. VIJAY AQUA INDU credited
        val creditedMatcher = BankSmsPatterns.MERCHANT_CREDITED_PATTERN.matcher(message)
        if (creditedMatcher.find()) return creditedMatcher.group(1)

        // 2. purchase of ... on [DATE] on BOOKMYSHOW
        // We look for the 'on' that is NOT followed by a date
        val parts = message.split(Regex("(?i)\\bon\\b")).map { it.trim() }
        if (parts.size >= 2) {
            // Check the last 'on' part
            val lastPart = parts.last()
            if (!lastPart.first().isDigit()) {
                // If it doesn't start with a digit (like a date), it's likely the merchant
                return lastPart.split(Regex("[.;]")).first()
            }
        }

        // 3. towards ELITE4
        val towardsMatcher = BankSmsPatterns.MERCHANT_TOWARDS_PATTERN.matcher(message)
        if (towardsMatcher.find()) return towardsMatcher.group(1)
        
        // 4. Info: ...
        val infoMatcher = BankSmsPatterns.MERCHANT_INFO_PATTERN.matcher(message)
        if (infoMatcher.find()) return infoMatcher.group(1)

        return null
    }

    private fun extractSbiMerchant(message: String): String? {
        // 1. Terminal Owner Name State Project Monitori
        val ownerMatcher = BankSmsPatterns.MERCHANT_OWNER_PATTERN.matcher(message)
        if (ownerMatcher.find()) return ownerMatcher.group(1)

        // 2. trf to SARAVANAKUMAR V
        val trfMatcher = BankSmsPatterns.MERCHANT_TRF_TO_PATTERN.matcher(message)
        if (trfMatcher.find()) return trfMatcher.group(1)

        return null
    }

    private fun extractAxisMerchant(message: String): String? {
        // Transaction Info: FLIPKART PAYMENTS
        val infoMatcher = BankSmsPatterns.MERCHANT_INFO_PATTERN.matcher(message)
        if (infoMatcher.find()) return infoMatcher.group(1)
        return null
    }

    private fun extractIndianBankMerchant(message: String): String? {
        // to SRI NANDTHI
        val toMatcher = BankSmsPatterns.PAYEE_TO_PATTERN.matcher(message)
        while (toMatcher.find()) {
            val merchant = toMatcher.group(1)?.trim()
            if (!merchant.isNullOrBlank() && !merchant.matches(Regex("\\d{8,15}"))) {
                return merchant
            }
        }
        return null
    }

    private fun extractGenericMerchant(message: String): String {
        // Current fallback logic
        val keywords = listOf(
            BankSmsPatterns.PAYEE_FOR_PATTERN,
            BankSmsPatterns.PAYEE_TO_PATTERN,
            BankSmsPatterns.MERCHANT_AT_PATTERN,
            BankSmsPatterns.MERCHANT_TOWARDS_PATTERN,
            BankSmsPatterns.MERCHANT_INFO_PATTERN
        )

        for (pattern in keywords) {
            val matcher = pattern.matcher(message)
            while (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                // Skip if merchant is just a phone number or reference number (long numeric string)
                if (!merchant.isNullOrBlank() && !merchant.matches(Regex("\\d{8,15}"))) {
                    return merchant
                }
            }
        }

        return "Unknown"
    }

    /**
     * Cleans up merchant name (removes extra characters, capitalizes properly)
     */
    private fun cleanMerchantName(name: String): String {
        var cleaned = name.trim()

        // 1. Remove Bank-Specific Jargon Prefixes
        val jargonPrefixes = listOf(
            Regex("(?i)^UPI-?\\d+-"),
            Regex("(?i)^ICICI Bank (Credit )?Card XX\\d+"),
            Regex("(?i)^ICICI Bank Account XX\\d+"),
            Regex("(?i)^HDFC Bank (Card|A/C) [\\*\\d]+"),
            Regex("(?i)^A/c [\\*\\dX]+"),
            Regex("(?i)^Terminal Owner Name"),
            Regex("(?i)^Transaction Info:"),
            Regex("(?i)^Dear Customer,?"),
            Regex("(?i)^INR\\s+[\\d,.]+\\s+spent using")
        )
        
        for (regex in jargonPrefixes) {
            cleaned = cleaned.replace(regex, "").trim()
        }

        // 2. Remove trailing punctuation and common footers
        cleaned = cleaned.split(Regex("(?i)\\b(on|at|for|ref|avail|avl|not you|not u|call|limit|to block|transaction|using|other services|if not)\\b")).first().trim()
        cleaned = cleaned.replace(Regex("[.;,:]$"), "").trim()

        // 3. Formatting
        val finalName = cleaned
            .replace(Regex("[^a-zA-Z0-9\\s&'-]"), "")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                if (word.length <= 3 && word.all { it.isUpperCase() }) word.uppercase()
                else word.lowercase().replaceFirstChar { it.uppercase() }
            }

        // 4. Final safety check: if it's just a long number, it's likely a phone or ref, not a merchant
        if (finalName.matches(Regex("\\d{8,15}"))) return "Unknown"

        return if (finalName.length < 2) "Unknown" else finalName.take(40)
    }

    /**
     * Extracts date string from message
     */
    private fun extractDate(message: String): String? {
        // Try different date patterns
        var matcher = BankSmsPatterns.DATE_PATTERN_DMY.matcher(message)
        if (matcher.find()) {
            return matcher.group(1)
        }

        matcher = BankSmsPatterns.DATE_PATTERN_SPACE.matcher(message)
        if (matcher.find()) {
            return matcher.group(1)
        }

        matcher = BankSmsPatterns.DATE_PATTERN_SLASH.matcher(message)
        if (matcher.find()) {
            return matcher.group(1)
        }

        return null
    }

    /**
     * Extracts account number (last 4 digits)
     */
    private fun extractAccountNumber(message: String): String? {
        val matcher = BankSmsPatterns.ACCOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    /**
     * Extracts UPI ID from message
     */
    private fun extractUpiId(message: String): String? {
        val matcher = BankSmsPatterns.UPI_ID_PATTERN.matcher(message)
        if (matcher.find()) {
            val upiId = matcher.group(1)
            // Validate it looks like a UPI ID (has @ and reasonable format)
            if (upiId?.contains("@") == true) {
                return upiId
            }
        }
        return null
    }

    /**
     * Extracts reference number based on transaction mode
     */
    private fun extractReferenceNumber(message: String, mode: String?): String? {
        return when (mode) {
            "UPI" -> {
                val matcher = BankSmsPatterns.UPI_REF_PATTERN.matcher(message)
                if (matcher.find()) matcher.group(1) else null
            }
            "NEFT" -> {
                val matcher = BankSmsPatterns.UTR_PATTERN.matcher(message)
                if (matcher.find()) matcher.group(1) else null
            }
            "IMPS" -> {
                val matcher = BankSmsPatterns.RRN_PATTERN.matcher(message)
                if (matcher.find()) matcher.group(1) else null
            }
            else -> null
        }
    }
}
