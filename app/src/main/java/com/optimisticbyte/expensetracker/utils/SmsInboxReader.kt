package com.optimisticbyte.expensetracker.utils

import android.content.Context
import android.provider.Telephony
import com.optimisticbyte.expensetracker.data.SmsMessage
import java.util.Calendar

object SmsInboxReader {
    
    /**
     * Reads SMS messages from the inbox, filtered by date range.
     * @param context Android context
     * @param daysBack Number of days to look back (default: 30)
     * @return List of SmsMessage sorted by date (newest first)
     */
    fun getRecentMessages(context: Context, daysBack: Int = 30): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        
        // Calculate cutoff date
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack)
        }.timeInMillis
        
        // Query SMS inbox
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ
        )
        
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(cutoffDate.toString())
        val sortOrder = "${Telephony.Sms.DATE} DESC"
        
        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val readIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
            
            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIndex)
                
                // Filter out likely non-transaction messages
                if (isPotentialTransaction(body)) {
                    messages.add(
                        SmsMessage(
                            id = cursor.getLong(idIndex),
                            sender = cursor.getString(addressIndex) ?: "Unknown",
                            body = body,
                            date = cursor.getLong(dateIndex),
                            isRead = cursor.getInt(readIndex) == 1
                        )
                    )
                }
            }
        }
        
        return messages
    }
    
    /**
     * Heuristic to filter potential transaction messages.
     * Uses BankSmsPatterns shared with the parser.
     */
    private fun isPotentialTransaction(body: String): Boolean {
        // Use the shared patterns for consistency
        return com.optimisticbyte.expensetracker.core.BankSmsPatterns.isTransactionMessage(body)
    }
}
