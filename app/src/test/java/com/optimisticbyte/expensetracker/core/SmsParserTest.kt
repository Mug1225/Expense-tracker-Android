package com.optimisticbyte.expensetracker.core

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
    fun `HDFC card with At On format - should extract merchant correctly`() {
        // Spent Rs.17072 On HDFC Bank Card 0511 At 84 ZIMSON SHOPPING ARCADE On 2025-10-04:13:29:33
        val sms = "Spent Rs.17072 On HDFC Bank Card 0511 At 84 ZIMSON SHOPPING ARCADE On 2025-10-04:13:29:33.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse HDFC card At-On transaction", result)
        assertEquals(17072.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be extracted", result?.merchant?.contains("Zimson", ignoreCase = true) == true)
    }

    @Test
    fun `HDFC card At format without date - should extract merchant correctly`() {
        // Spent Rs.2999 On HDFC Bank Card 0511 At BATA INDIA, On 2026-01-02:13:08:57
        val sms = "Spent Rs.2999 On HDFC Bank Card 0511 At BATA INDIA, On 2026-01-02:13:08:57.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808"
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse BATA INDIA transaction", result)
        assertEquals(2999.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be Bata India", result?.merchant?.contains("Bata", ignoreCase = true) == true)
    }

    @Test
    fun `ICICI card with multiple On keywords - should extract merchant correctly`() {
        // INR 1,153.40 spent using ICICI Bank Card XX3004 on 10-Oct-25 on BOOKMYSHOW
        val sms = "INR 1,153.40 spent using ICICI Bank Card XX3004 on 10-Oct-25 on BOOKMYSHOW. Avl Limit: INR 2,98,281.84. If not you, call 1800 2662/SMS BLOCK 3004 to 9215676766.?"
        val result = SmsParser.parseSms("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse ICICI multi-on transaction", result)
        assertEquals(1153.40, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be Bookmyshow", result?.merchant?.contains("Bookmyshow", ignoreCase = true) == true)
    }

    @Test
    fun `Indian Bank to format - should extract merchant correctly`() {
        // A/c *8031 debited Rs. 179.00 on 20-12-25 to SRI NANDTHI
        val sms = "A/c *8031 debited Rs. 179.00 on 20-12-25 to SRI NANDTHI . UPI:535411655315. Not you? SMS BLOCK to 9289592895, Dial 1930 for Cyber Fraud - Indian Bank"
        val result = SmsParser.parseSms("IndianBank", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse Indian Bank transaction", result)
        assertEquals(179.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be SRI NANDTHI", result?.merchant?.contains("Nandthi", ignoreCase = true) == true)
    }

    @Test
    fun `HDFC Netbanking format - should extract merchant correctly`() {
        val sms = "Dear Customer, This is to inform you that an amount of Rs. 700.00 has been debited from your account No. XXXX3221 on account of National Electronic Funds Transfer transaction using HDFC Bank NetBanking."
        val result = SmsParser.parseSms("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse HDFC NetBanking", result)
        assertEquals(700.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be extracted", result?.merchant?.contains("National", ignoreCase = true) == true)
    }

    @Test
    fun `SBI POS format - should extract merchant correctly`() {
        val sms = "Dear Valued SBI Debit Card Holder, Terminal Owner Name State Project Monitori. Terminal Id 43R24163. Date & Time Jan 16, 2024, 17:34. Transaction Number 401617479902. Amount (INR) 985.00."
        val result = SmsParser.parseSms("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse SBI POS", result)
        assertEquals(985.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be extracted", result?.merchant?.contains("Monitori", ignoreCase = true) == true)
    }

    @Test
    fun `SBI UPI format with trf to - should extract merchant correctly`() {
        // Dear UPI user A/C X2481 debited by 120.0 on date 03Nov25 trf to SARAVANAKUMAR V Refno 530717244504 If not u? call-1800111109 for other services-18001234-SBI
        val sms = "Dear UPI user A/C X2481 debited by 120.0 on date 03Nov25 trf to SARAVANAKUMAR V Refno 530717244504 If not u? call-1800111109 for other services-18001234-SBI "
        val result = SmsParser.parseSms("JX-SBIUPI-S", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse SBI UPI trf to", result)
        assertEquals(120.0, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be SARAVANAKUMAR V", result?.merchant?.contains("Saravanakumar", ignoreCase = true) == true)
        assertFalse("Should not contain other services", result?.merchant?.contains("other services", ignoreCase = true) == true)
    }

    @Test
    fun `Axis Info format - should extract merchant correctly`() {
        val sms = "INR 100.00 was debited from your A/c no. XX1236. Date & Time: 19-12-24, 10:19:54 IST. Transaction Info: FLIPKART PAYMENTS"
        val result = SmsParser.parseSms("AXISBK", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse Axis info format", result)
        assertEquals(100.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be Flipkart", result?.merchant?.contains("Flipkart", ignoreCase = true) == true)
    }

    @Test
    fun `ICICI message from Indian Bank - should NOT use block number as merchant`() {
        val sms = "ICICI Bank Acct XX728 debited for Rs 10.00 on 24-Dec-25; VIJAY AQUA INDU credited. UPI:116138852721. Call 18002662 for dispute. SMS BLOCK 728 to 9215676766."
        val result = SmsParser.parseSms("IndianBank", sms, System.currentTimeMillis())
        
        assertNotNull("Should parse ICICI transaction even if from different sender", result)
        assertEquals(10.00, result?.amount ?: 0.0, 0.01)
        assertTrue("Merchant should be Vijay Aqua Indu, not block number", result?.merchant?.contains("Vijay", ignoreCase = true) == true)
        assertFalse("Merchant should NOT be the block number", result?.merchant?.contains("9215676766") == true)
    }

    @Test
    fun `Promotional airtel message - should NOT be parsed as transaction`() {
        val sms = "Never run out of data during important moments. Get 75 GB + 200 GB rollover on Postpaid at just Rs.449. Upgrade now https://i.airtel.in/goldencohort"
        val result = SmsParser.parseSms("Airtel", sms, System.currentTimeMillis())
        
        assertNull("Promotional message should not be parsed as transaction", result)
    }
}
