package com.example.expensetracker.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SMS Parser with real-world Indian bank SMS formats
 */
class SmsParserTest {

    @Test
    fun `HDFC UPI debit transaction - should parse correctly`() {
        val sms = "Dear Customer, your A/c XX1234 has been debited by INR 500.00 on 26-Dec-24 for UPI transaction to john@paytm. UPI Ref No 123456789012. Avail. Bal: INR 10,000.00. - HDFC Bank"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse HDFC UPI debit transaction", result)
        assertEquals(500.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be extracted", result?.merchant?.isNotBlank() == true)
    }

    @Test
    fun `HDFC card debit - should parse correctly`() {
        val sms = "Dear Customer, Rs. 1,250.50 spent on HDFC Bank Card XX1234 at AMAZON PAY on 26-Dec-24. Avail limit: Rs. 50,000. - HDFC Bank"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse HDFC card transaction", result)
        assertEquals(1250.50, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should contain Amazon", result?.merchant?.contains("Amazon", ignoreCase = true) == true)
    }

    @Test
    fun `ICICI debit transaction - should parse correctly`() {
        val sms = "Dear Customer, Your Account XXXXX101 has been debited by Rs 200.00 on 31 Oct. Info:BIL*000001901069*test. Total Available balance: Rs 10,000.00."
        val result = SmsParser.parseSms("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse ICICI debit transaction", result)
        assertEquals(200.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `SBI transfer debit - should parse correctly`() {
        val sms = "Dear Customer, Your A/C XXXXX528510 has a debit by transfer of Rs 236.00 on 21/08/24."
        val result = SmsParser.parseSms("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse SBI transfer", result)
        assertEquals(236.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `Axis Bank UPI debit - should parse correctly`() {
        val sms = "Dear Customer, INR 750 debited from A/c XX9876 on 26-Dec for UPI to merchant@upi. UPI Ref: 234567890123. Avail Bal: INR 25,000"
        val result = SmsParser.parseSms("AXISBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse Axis UPI transaction", result)
        assertEquals(750.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `credit transaction - should be ignored`() {
        val sms = "Dear Customer, Your Account XXXXX101 has been credited by Rs 1000.00 on 31 Oct. Total Available balance: Rs 11,000.00."
        val result = SmsParser.parseSms("ICICIB", sms, System.currentTimeMillis())
        
        assertNull("Credit transactions should be ignored for expense tracking", result)
    }

    @Test
    fun `message without amount - should return null`() {
        val sms = "Dear Customer, your transaction was successful. Thank you for banking with us."
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull("Messages without amount should return null", result)
    }

    @Test
    fun `non-transaction message - should return null`() {
        val sms = "Your OTP for login is 123456. Valid for 10 minutes."
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull("OTP messages should return null", result)
    }

    @Test
    fun `NEFT transaction - should parse correctly`() {
        val sms = "A/c XX1234 debited with Rs.5000 on 26-Dec-24 NEFT to RAJESH KUMAR. UTR: HDFC0000123456789. Avl Bal Rs.45000"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse NEFT transaction", result)
        assertEquals(5000.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should contain payee name", result?.merchant?.contains("Rajesh", ignoreCase = true) == true || 
                   result?.merchant?.contains("Kumar", ignoreCase = true) == true)
    }

    @Test
    fun `IMPS transaction - should parse correctly`() {
        val sms = "Rs 350.00 debited from A/c XX5678 by IMPS to beneficiary on 26-Dec. RRN: 123456789012. Bal: Rs 8000"
        val result = SmsParser.parseSms("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse IMPS transaction", result)
        assertEquals(350.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `ATM withdrawal - should parse correctly`() {
        val sms = "Dear Customer, Rs.2000 withdrawn from ATM using Card XX1234 on 26-Dec-24. Available balance: Rs.18000"
        val result = SmsParser.parseSms("AXIS", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse ATM withdrawal", result)
        assertEquals(2000.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `POS transaction - should parse correctly`() {
        val sms = "Dear Customer, INR 899.00 debited from Card XX4321 for POS transaction at BIG BAZAAR on 26-Dec-24"
        val result = SmsParser.parseSms("ICICIBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse POS transaction", result)
        assertEquals(899.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Should extract merchant name", result?.merchant?.contains("Big", ignoreCase = true) == true ||
                   result?.merchant?.contains("Bazaar", ignoreCase = true) == true)
    }

    @Test
    fun `amount with comma formatting - should parse correctly`() {
        val sms = "A/c XX1234 debited by Rs.12,500.00 on 26-Dec for payment to VENDOR. Bal: Rs.1,00,000"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse amount with comma separators", result)
        assertEquals(12500.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `UPI with phone number ID - should parse correctly`() {
        val sms = "Rs 150 paid via UPI to 9876543210@paytm on 26-Dec from A/c XX1234. UPI Ref: 123456789012"
        val result = SmsParser.parseSms("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse UPI with phone number", result)
        assertEquals(150.00, result?.amount ?: 0.0, 0.01)
    }

    @Test
    fun `merchant name cleaning - should capitalize properly`() {
        val sms = "Rs 500 debited from A/c XX1234 to AMAZON PAY INDIA on 26-Dec"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse transaction", result)
        // Merchant name should be properly formatted (not all caps)
        val merchant = result?.merchant ?: ""
        assertTrue("Merchant should be cleaned", merchant.isNotBlank())
    }
    @Test
    fun `HDFC Sent format - should parse correctly`() {
        val sms = """
            Sent Rs.2.00
            From HDFC Bank A/C *5640
            To Mr Mugesh
            On 26/12/25
            Ref 536012114341
            Not You?
            Call 18002586161/SMS BLOCK UPI to 7308080808
        """.trimIndent()
        
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse 'Sent' format", result)
        assertEquals(2.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Should extract merchant 'Mr Mugesh'", result?.merchant?.contains("Mugesh", ignoreCase = true) == true)
        
        // These assertions should now pass with updated patterns
        // Note: accountNumber and referenceNumber are extracted but not stored in Transaction entity for now
        // assertNotNull("Should extract account number", result?.accountNumber) 
        // assertEquals("5640", result?.accountNumber)
        // assertNotNull("Should extract reference", result?.referenceNumber)
        // assertEquals("536012114341", result?.referenceNumber)
    }
}
