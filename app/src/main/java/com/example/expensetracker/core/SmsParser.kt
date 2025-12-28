package com.example.expensetracker.core

import com.example.expensetracker.data.Transaction

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
        val amount = extractAmount(message) ?: run {
            return null
        }

        // Extract merchant/payee
        val merchant = extractMerchant(message)

        // Extract transaction mode
        val transactionMode = BankSmsPatterns.getTransactionMode(message)

        // Extract reference number (UPI/NEFT/IMPS)
        val referenceNumber = extractReferenceNumber(message, transactionMode)

        // Extract UPI ID if it's a UPI transaction
        val upiId = if (transactionMode == "UPI") extractUpiId(message) else null

        // Extract account number (last 4 digits)
        val accountNumber = extractAccountNumber(message)

        // Extract date string (optional - we use timestamp as primary)
        val parsedDate = extractDate(message)

        // Identify bank
        val bank = BankSmsPatterns.identifyBank(sender)

        return Transaction(
            amount = amount,
            merchant = merchant,
            date = timestamp,
            sender = sender,
            fullMessage = message
        )
    }

    /**
     * Extracts amount from message
     */
    private fun extractAmount(message: String): Double? {
        // Try standard amount pattern first
        var matcher = BankSmsPatterns.AMOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        // Try "by/of" pattern (e.g., "debited by 500.00")
        matcher = BankSmsPatterns.AMOUNT_BY_OF_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        return null
    }

    /**
     * Extracts merchant/payee name from message
     */
    private fun extractMerchant(message: String): String {
        // 1. Try "credited" pattern (Highest priority for this specific format)
        var matcher = BankSmsPatterns.BENEFICIARY_CREDITED_PATTERN.matcher(message)
        if (matcher.find()) {
            val merchant = matcher.group(1)?.trim()
            if (!merchant.isNullOrBlank()) {
                return cleanMerchantName(merchant)
            }
        }

        // 2. Try "for" pattern (e.g. for UPI-...)
        matcher = BankSmsPatterns.PAYEE_FOR_PATTERN.matcher(message)
        // Loop to find valid match (in case first "for" is "for INR" skipping, though regex handles lookahead, loop is safer if multiple "for" exist)
        // Actually regex finds first match. If regex fails on "for INR", it might find next "for".
        if (matcher.find()) {
            val merchant = matcher.group(1)?.trim()
            // Ignore if "for" extracts "dispute", "POS transaction", or purely numeric
            if (!merchant.isNullOrBlank() && 
                !merchant.equals("dispute", ignoreCase = true) &&
                !merchant.contains("POS transaction", ignoreCase = true)) {
                return cleanMerchantName(merchant)
            }
        }

        // 3. Try "to" pattern (Standard)
        // Filter out if it extracted the block number (starts with digit and length > 6 is suspicious for "to 921...")
        matcher = BankSmsPatterns.PAYEE_TO_PATTERN.matcher(message)
        if (matcher.find()) {
            val merchant = matcher.group(1)?.trim()
            if (!merchant.isNullOrBlank()) {
                 // Heuristic: If it starts with digit and is long, it might be the block number interaction
                 val isLikelyBlockNumber = merchant.first().isDigit() && merchant.length > 5 && !merchant.contains("@") // @ for UPI
                 if (!isLikelyBlockNumber) {
                     return cleanMerchantName(merchant)
                 }
            }
        }

        // 4. Try "at" pattern (POS)
        matcher = BankSmsPatterns.MERCHANT_AT_PATTERN.matcher(message)
        if (matcher.find()) {
            val merchant = matcher.group(1)?.trim()
            if (!merchant.isNullOrBlank()) {
                return cleanMerchantName(merchant)
            }
        }

        // 5. Try extracting UPI ID as fallback
        val upiId = extractUpiId(message)
        if (upiId != null) {
            // Extract name before @ symbol
            val name = upiId.substringBefore("@")
            return cleanMerchantName(name)
        }

        return "Unknown"
    }

    /**
     * Cleans up merchant name (removes extra characters, capitalizes properly)
     */
    private fun cleanMerchantName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9\\s&'-]"), "")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                if (word.length <= 3) word.uppercase()
                else word.lowercase().replaceFirstChar { it.uppercase() }
            }
            .take(30) // Limit length
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
