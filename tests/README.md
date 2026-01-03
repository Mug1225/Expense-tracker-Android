# SMS Parser Test Cases

This document contains a collection of SMS message samples from various Indian banks along with the expected extracted values for amount and merchant/payee. This serves as the ground truth for our SMS parsing logic.

| Bank | SMS Message | Expected Amount | Expected Merchant |
| :--- | :--- | :--- | :--- |
| **HDFC (Card)** | Spent Rs.17072 On HDFC Bank Card 0511 At 84 ZIMSON SHOPPING ARCADE On 2025-10-04:13:29:33.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808 | 17072.00 | 84 Zimson Shopping Arcade |
| **HDFC (UPI)** | Sent Rs.2.00 From HDFC Bank A/C *5640 To Mr Mugesh On 26/12/25 Ref 536012114341 Not You? Call 18002586161/SMS BLOCK UPI to 7308080808 | 2.00 | Mr Mugesh |
| **Indian Bank** | A/c *8031 debited Rs. 179.00 on 20-12-25 to SRI NANDTHI . UPI:535411655315. Not you? SMS BLOCK to 9289592895, Dial 1930 for Cyber Fraud - Indian Bank | 179.00 | Sri Nandthi |
| **ICICI (CC)** | ICICI Bank Credit Card XX5008 debited for INR 40.00 on 24-Dec-25 for UPI-572492856855-JUICE. To dispute call 18001080/SMS BLOCK 5008 to 9215676766 | 40.00 | Juice |
| **ICICI (Acct)** | CICI Bank Acct XX728 debited for Rs 10.00 on 24-Dec-25; VIJAY AQUA INDU credited. UPI:116138852721. Call 18002662 for dispute. SMS BLOCK 728 to 9215676766. | 10.00 | Vijay Aqua Indu |
| **ICICI (Card)** | INR 1,153.40 spent using ICICI Bank Card XX3004 on 10-Oct-25 on BOOKMYSHOW. Avl Limit: INR 2,98,281.84. If not you, call 1800 2662/SMS BLOCK 3004 to 9215676766.? | 1153.40 | Bookmyshow |
| **HDFC (Card)** | Spent Rs.2999 On HDFC Bank Card 0511 At BATA INDIA, On 2026-01-02:13:08:57.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808 | 2999.00 | Bata India |
| **HDFC (Net)** | Dear Customer, This is to inform you that an amount of Rs. 700.00 has been debited from your account No. XXXX3221 on account of National Electronic Funds Transfer transaction using HDFC Bank NetBanking. | 700.00 | National Electronic Funds Transfer |
| **SBI (POS)** | Dear Valued SBI Debit Card Holder, Terminal Owner Name State Project Monitori. Terminal Id 43R24163. Date & Time Jan 16, 2024, 17:34. Transaction Number 401617479902. Amount (INR) 985.00. | 985.00 | State Project Monitori |
| **Axis** | INR 100.00 was debited from your A/c no. XX1236. Date & Time: 19-12-24, 10:19:54 IST. Transaction Info: FLIPKART PAYMENTS | 100.00 | Flipkart Payments |
| **SBI (UPI)** | Dear UPI user A/C X2481 debited by 120.0 on date 03Nov25 trf to SARAVANAKUMAR V Refno 530717244504 If not u? call-1800111109 for other services-18001234-SBI | 120.00 | Saravanakumar V |

**Commands**
podman exec android-emulator adb install -r /tmp/app.apk                   
Performing Streamed Install                                                                           
Success                                         
PS C:\programs\calculator> podman exec android-emulator adb emu sms send JX-SBIUPI-S  "Dear UPI user A/C X2481 debited by 120.0 on date 03Nov25 trf to SARAVANAKUMAR V Refno 530717244504 If not u? call-1800111109 for other services-18001234-SBI"
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send JX-HDFCBK-S  "Sent Rs.2810.17 From HDFC Bank A/C *5640 To TARAN SIVAA On 03/10/25 Ref 564274018417 Not You? Call 18002586161/SMS BLOCK UPI to 7308080808"
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send JX-HDFCBK-S  "Spent Rs.2999 On HDFC Bank Card 0511 At BATA INDIA, On 2026-01-02:13:08:57.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808"
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send BT-INDBNK-S  "A/c *8031 debited Rs. 501.67 on 02-01-26 to TARAN SIVAA. UPI:600219036513. Not you? SMS BLOCK to 9289592895, Dial 1930 for Cyber Fraud - Indian Bank"
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send VM-ICICIT-S  "INR 1,153.40 spent using ICICI Bank Card XX3004 on 10-Oct-25 on BOOKMYSHOW. Avl Limit: INR 2,98,281.84. If not you, call 1800 2662/SMS BLOCK 3004 to 9215676766."
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send JM-HDFCBK-S  "Spent Rs.17072 On HDFC Bank Card 0511 At 84 ZIMSON SHOPPING ARCADE On 2025-10-04:13:29:33.Not You? To Block+Reissue Call 18002586161/SMS BLOCK CC 0511 to 7308080808"
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send JM-ICICIT-S  "ICICI Bank Acct XX728 debited for Rs 10.00 on 24-Dec-25; VIJAY AQUA INDU credited. UPI:116138852721. Call 18002662 for dispute. SMS BLOCK 728 to 9215676766."
OK
PS C:\programs\calculator> podman exec android-emulator adb emu sms send AD-AIRTEL-S  "Never run out of data during important moments. Get 75 GB + 200 GB rollover on Postpaid at just Rs.449. Upgrade now https://i.airtel.in/goldencohort"                
OK