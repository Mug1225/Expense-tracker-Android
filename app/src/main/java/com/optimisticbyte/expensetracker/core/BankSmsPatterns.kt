package com.optimisticbyte.expensetracker.core

import java.util.regex.Pattern

/**
 * Comprehensive SMS patterns for Indian banks
 * Supports HDFC, ICICI, SBI, Axis and generic patterns
 */
object BankSmsPatterns {

    // Matches keywords that indicate a transaction (debit/spent)
    val TRANSACTION_TYPE_PATTERN = Pattern.compile(
        "(?i)\\b(debited|spent|withdrawn|paid|payment|transfer|sent|purchase|owner name|amount \\(inr\\))\\b"
    )

    // ========== AMOUNT PATTERNS ==========
    // Matches: Rs. 500.00, INR 500, Rs 500, Rs.500.00, Rs. 5000
    // Updated to allow unformatted numbers (no commas)
    val AMOUNT_PATTERN = Pattern.compile(
        "(?i)(?:Rs\\.?|INR)\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    )

    // Matches amount in POS transactions (e.g., "Amount (INR) 985.00")
    val AMOUNT_POS_PATTERN = Pattern.compile(
        "(?i)Amount\\s*\\((?:INR|Rs\\.?)\\)?\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    )

    // Alternative amount pattern for messages that use "by" or "of"
    // Example: "debited by 500.00"
    val AMOUNT_BY_OF_PATTERN = Pattern.compile(
        "(?i)(?:by|of)\\s+(?:Rs\\.?|INR)?\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    )

    // ========== DATE PATTERNS ==========
    // Matches: 22-Dec, 22-Dec-24, 22-Dec-2024
    val DATE_PATTERN_DMY = Pattern.compile(
        "(?i)on\\s+(\\d{1,2}-[A-Za-z]{3}(?:-\\d{2,4})?)"
    )

    // Matches: 31 Oct, 31 Oct 24
    val DATE_PATTERN_SPACE = Pattern.compile(
        "(?i)on\\s+(\\d{1,2}\\s+[A-Za-z]{3}(?:\\s+\\d{2,4})?)"
    )

    // Matches: 21/08/24, 21/08/2024
    val DATE_PATTERN_SLASH = Pattern.compile(
        "(?i)on\\s+(\\d{1,2}/\\d{1,2}/\\d{2,4})"
    )

    // ========== PAYEE/MERCHANT PATTERNS ==========
    // Matches text after "to" (for debits/transfers)
    // Updated to include @ for UPI IDs
    // Added negative lookahead (?!.*SMS BLOCK) to avoid capturing block messages, but regex lookarounds can be complex in Java/Kotlin 
    // Simplified: Ensure capturing group doesn't start with a number unless it's a UPI ID
    val PAYEE_TO_PATTERN = Pattern.compile(
        "(?i)\\b(?:to|paid to)\\s+([A-Za-z0-9][A-Za-z0-9\\s&.'_@]{2,30}?)(?:\\s+on|\\s+at|\\s+for|\\.|\\s{2,}|$)"
    )

    // Matches text after "for" (e.g. "debited for UPI-...")
    // Excludes "for INR", "for Rs", "for POS transaction" via negative lookahead
    val PAYEE_FOR_PATTERN = Pattern.compile(
        "(?i)\\bfor\\s+(?!INR|Rs\\.?|POS transaction)([A-Za-z0-9][A-Za-z0-9\\s&.'_@]{2,30}?)(?:\\s+on|\\s+at|\\.|\\s{2,}|$)"
    )

    // Matches text before "credited" (for debits where beneficiary is credited)
    // Refined to be more specific to avoid matching random words
    val BENEFICIARY_CREDITED_PATTERN = Pattern.compile(
        "(?i)(?:;|\\.)\\s*([A-Za-z0-9\\s&.'_]{2,30}?)\\s+credited"
    )

    // Matches text after "from" (for credits)
    val PAYEE_FROM_PATTERN = Pattern.compile(
        "(?i)\\b(?:from|received from)\\s+([A-Za-z0-9][A-Za-z0-9\\s&.'_]{2,30}?)(?:\\s+on|\\s+at|\\.|\\s{2,}|$)"
    )

    // Matches text after "at" (common for POS transactions)
    val MERCHANT_AT_PATTERN = Pattern.compile(
        "(?i)\\bat\\s*:?\\s*([A-Za-z0-9][A-Za-z0-9\\s&.'_]{2,30}?)(?:\\s+on|\\.|\\s{2,}|$)"
    )

    // Matches text after "towards" (common in netbanking)
    val MERCHANT_TOWARDS_PATTERN = Pattern.compile(
        "(?i)\\btowards\\s+([A-Za-z0-9][A-Za-z0-9\\s&.'_]{2,30}?)(?:\\s+on|\\s+at|\\.|\\s{2,}|$)"
    )

    // Matches text after "Info:" (ICICI/Axis)
    val MERCHANT_INFO_PATTERN = Pattern.compile(
        "(?i)Info\\s*:?\\s*([A-Za-z0-9][A-Za-z0-9\\s&.'*_-]{2,30}?)(?:\\s+on|\\s+at|\\.|\\s{2,}|$)"
    )

    // Matches text after "credited" preceded by a semicolon or period
    // Redefined to capture the beneficiary name
    val MERCHANT_CREDITED_PATTERN = Pattern.compile(
        "(?i)(?:;|\\.|on)\\s*([A-Za-z0-9][A-Za-z0-9\\s&.'_]{2,30}?)\\s+credited"
    )



    // ========== ACCOUNT NUMBER PATTERNS ==========
    // Matches: A/c XX1234, Account XXXXX101, A/C 1234, A/C *5640
    val ACCOUNT_PATTERN = Pattern.compile(
        "(?i)(?:A/c|A/C|Account)\\s+(?:No\\.?)?\\s*[Xx*]*(\\d{4})"
    )

    // ========== TRANSACTION MODE PATTERNS ==========
    val UPI_PATTERN = Pattern.compile("(?i)\\bUPI\\b")
    val NEFT_PATTERN = Pattern.compile("(?i)\\bNEFT\\b")
    val IMPS_PATTERN = Pattern.compile("(?i)\\bIMPS\\b")
    val CARD_PATTERN = Pattern.compile("(?i)\\b(?:Card|Debit Card|Credit Card)\\b")
    val ATM_PATTERN = Pattern.compile("(?i)\\bATM\\b")
    val POS_PATTERN = Pattern.compile("(?i)\\bPOS\\b")
    val TRANSFER_PATTERN = Pattern.compile("(?i)\\b(?:transfer|fund transfer)\\b")

    // ========== UPI SPECIFIC PATTERNS ==========
    // Matches UPI IDs: name@bank, mobile@upi
    val UPI_ID_PATTERN = Pattern.compile(
        "\\b([a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+)\\b"
    )

    // Matches text after "Terminal Owner Name" (SBI POS)
    val MERCHANT_OWNER_PATTERN = Pattern.compile(
        "(?i)Terminal Owner Name\\s+([A-Za-z0-9][A-Za-z0-9\\s&.',-]{2,50}?)[.;]"
    )

    // Matches "trf to MERCHANT" (SBI UPI)
    val MERCHANT_TRF_TO_PATTERN = Pattern.compile(
        "(?i)trf\\s+to\\s+([A-Za-z0-9][A-Za-z0-9\\s&.'-@]{2,30}?)(?:\\s+Refno|\\s+on|\\s+at|\\.|\\s{2,}|$)"
    )

    // Matches UPI Reference numbers (10-12 digits)
    val UPI_REF_PATTERN = Pattern.compile(
        "(?i)(?:UPI Ref|Txn ID|Ref No|Ref|UPI:)\\s*:?\\s*(\\d{10,12})"
    )

    // ========== NEFT/IMPS REFERENCE PATTERNS ==========
    // UTR number for NEFT (16 alphanumeric characters)
    val UTR_PATTERN = Pattern.compile(
        "(?i)(?:UTR|NEFT Ref)\\s*:?\\s*([A-Za-z0-9]{16})"
    )

    // RRN number for IMPS (12 digits)
    val RRN_PATTERN = Pattern.compile(
        "(?i)(?:RRN|IMPS Ref No)\\s*:?\\s*(\\d{12})"
    )

    // ========== BALANCE PATTERNS ==========
    // Matches: Avail. Bal: INR 10,000.00, Total Available balance: 5000.00
    val BALANCE_PATTERN = Pattern.compile(
        "(?i)(?:Avail\\.?\\s*Bal|Available Balance|Total Available balance|Available Credit Limit)\\s*:?\\s*(?:Rs\\.?|INR)?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{1,2})?)"
    )

    // ========== BANK SENDER ID MAPPING ==========
    val BANK_SENDERS = mapOf(
        "HDFC" to listOf("HDFCBK", "HDFC", "HDFCBN", "HDFCBANK", "HDFC BANK", "HDFCPR"),
        "ICICI" to listOf("ICICIB", "ICICI", "ICICIBK", "ICICI BANK", "ICICIP"),
        "SBI" to listOf("SBIINB", "SBIIN", "SBI", "SBIBANK", "SBIET", "SBIPS"),
        "Axis" to listOf("AXISBK", "AXIS", "AXISMB", "AXIS BANK", "AXISBT"),
        "Kotak" to listOf("KOTAKM", "KOTAK"),
        "PNB" to listOf("PNBSMS", "PNB"),
        "BOB" to listOf("BOBTXN", "BOB"),
        "Canara" to listOf("CANBNK", "CANARA"),
        "IDFC" to listOf("IDFCFB", "IDFC"),
        "Yes Bank" to listOf("YESBNK", "YESBANK"),
        "Indian Bank" to listOf("INDIAB", "INDIAN", "INDIBK")
    )

    /**
     * Identifies bank from sender ID
     */
    fun identifyBank(sender: String): String {
        val upperSender = sender.uppercase()
        for ((bank, senderIds) in BANK_SENDERS) {
            if (senderIds.any { upperSender.contains(it) }) {
                return bank
            }
        }
        return "Unknown"
    }

    /**
     * Checks if message is likely a transaction SMS
     */
    fun isTransactionMessage(message: String): Boolean {
        // Must contain keywords that indicate a transaction (debit/spent) with word boundaries
        val hasTransactionType = TRANSACTION_TYPE_PATTERN.matcher(message).find()
        
        // Must contain amount pattern
        val hasAmount = AMOUNT_PATTERN.matcher(message).find() || 
                       AMOUNT_BY_OF_PATTERN.matcher(message).find() ||
                       AMOUNT_POS_PATTERN.matcher(message).find()
        
        return hasTransactionType && hasAmount
    }

    /**
     * Determines if transaction is debit or credit
     */
    fun getTransactionType(message: String): String {
        val lowerMessage = message.lowercase()
        return when {
            // Check DEBIT first to handle cases like "debited ... credited to" (which are debits)
            // Also handles "Credit Card ... debited"
            lowerMessage.contains("debited") || lowerMessage.contains("paid") || 
            lowerMessage.contains("spent") || lowerMessage.contains("withdrawn") ||
            (lowerMessage.contains("debit") && !lowerMessage.contains("credit card")) || // Avoid equating "credit card" to debit unless "debited" is present
            lowerMessage.contains("sent") -> "DEBIT"

            lowerMessage.contains("credited") || lowerMessage.contains("received") || 
            lowerMessage.contains("credit") -> "CREDIT"
            
            else -> "UNKNOWN"
        }
    }

    /**
     * Identifies transaction mode from message
     */
    fun getTransactionMode(message: String): String? {
        return when {
            UPI_PATTERN.matcher(message).find() -> "UPI"
            NEFT_PATTERN.matcher(message).find() -> "NEFT"
            IMPS_PATTERN.matcher(message).find() -> "IMPS"
            ATM_PATTERN.matcher(message).find() -> "ATM"
            POS_PATTERN.matcher(message).find() -> "POS"
            CARD_PATTERN.matcher(message).find() -> "CARD"
            TRANSFER_PATTERN.matcher(message).find() -> "TRANSFER"
            else -> null
        }
    }
}
