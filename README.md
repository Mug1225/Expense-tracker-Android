# SpendWise 🚀

SpendWise is a premium, native Android application designed to make expense tracking effortless. By automatically parsing incoming SMS alerts from banks and merchants, SpendWise provides real-time insights into your spending habits with a clean, modern interface.

Built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **Hilt** for Dependency Injection.

## ✨ Key Features

- **📊 Visual Analytics**: Dedicated Analytics tab with **Donut Chart** for spending breakdown and **Trend Line** for daily spending patterns. Filter charts by category using the dropdown menu.
- **🔍 Merchant Search**: Quickly find and filter transactions by merchant. Search through all unique merchants and tap to view their transactions.
- **📅 Custom Date Ranges**: Tap the month header to select specific dates (e.g., "Last 2 Weeks") for precise expense tracking. Works across both Home and Analytics screens.
- **✏️ Full Transaction Editing**: Edit transaction amounts, categories, merchants, tags, and even **dates**. Perfect for backdating expenses or correcting mistakes.
- **➕ Smart Manual Entry**: Add transactions with a date picker. Merchant and tags are optional—just enter the amount and date to get started.
- **🎨 Dynamic Themes**: Personalize your experience with premium themes including **Emerald**, **Ocean**, and **Charcoal**.
- **⚙️ Composite Filtering**: Combine date ranges with category or merchant filters to analyze specific spending patterns.
- **🏷️ Custom Categories**: Create categories and choose from a library of icons to visually organize your spending.
- **📩 Enhanced SMS Parsing**: Automatically extracts transaction details (Amount, Merchant, Date) from HDFC, SBI, ICICI, Axis, and other bank SMS. Supports UPI, NEFT, IMPS, ATM, and Card transactions.
- **🛡️ Smart Filtering**: Intelligently identifies and processes only **Debit** transactions for accurate expense tracking, filtering out promos and credit alerts.
- **Merchant Tagging**: Organize your spending with multiple tags. Tags replace merchant names as primary labels, providing a cleaner and more personalized view.
- **💰 Spending Limits**: Set monthly budgets for specific categories (e.g., "Food", "Entertainment") or your overall spending. Track progress with visual indicators.
- **🔔 Smart Notifications**: Get instant alerts when you exceed your set spending limits to stay on track.
- **💾 Backup & Restore**: Securely backup your data to a JSON file and restore it anytime, ensuring your financial data is safe even if you reinstall the app.
- **📂 Data Portability**: Export your data to standard JSON format, giving you full control and ownership of your information.
- **Interactive Category Summaries**: Tap on any category in the summary tile to instantly filter your transactions.
- **Hardware Back Button**: Navigate naturally from Analytics back to Home using your device's back button.

## 🏗️ Project Structure

### Root Directory
- **Containerfile**: Dockerfile for a reproducible Android build environment.
- **compose.yaml**: Orchestration for the containerized Android emulator.

### Core Components (`app/src/main/java/com/example/expensetracker/`)

#### Application Entry & Logic
- **`MainActivity.kt`**: Manages navigation (**Home**, **Analytics**, **Search**) with bottom navigation bar and hardware back button support.
- **`SmsReceiver.kt`**: Listens for incoming SMS alerts and triggers the parsing/auto-categorization engine.

#### Data Layer (`/data`)
- **`Transaction.kt`**: Room Entity supporting amounts, dates, categories, and custom **tags**.
- **`MerchantMapping.kt`**: Stores user-defined mappings for merchants, categories, and tags.

#### UI Layer (`/ui`)
- **`HomeScreen.kt`**: Main dashboard with interactive summaries, transaction lists, **Search** button, **Date Range Picker**, and **Settings** access.
- **`AnalyticsScreen.kt`**: Dedicated screen for **Visual Analytics**, displaying charts and trends with category filtering.
- **`SearchScreen.kt`**: Search and filter transactions by merchant name.
- **`charts/`**: Custom **Canvas-based** chart components (`DonutChart.kt`, `LineChart.kt`) for high-performance visualization.
- **`CategoryScreen.kt`**: Category management with a horizontal **Icon Picker**.
- **`AddTransactionDialog.kt`**: Enhanced dialog with **Date Picker** for manual entries.
- **`EditTransactionDialog.kt`**: Dialog for modifying transactions, including **Date Picker** for backdating.
- **`Theme.kt`**: Centralized, scalable theme engine supporting multiple palettes.

## 🛠️ Build Instructions

### Standard Build
Open the project in **Android Studio** and run on an emulator or physical device.

### Containerized Build (Podman/Docker)
Build the APK without any local Android dependencies:

```bash
# 1. Build the builder image
podman build -t android-builder -f Containerfile .

# 2. Compile the APK
podman run --rm -v ".:/app" android-builder gradle assembleDebug
```
The final APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### 📱 Running with Containerized Emulator

Use the included emulator to test the app without installing Android Studio.

#### 1. Start the Emulator
```bash
podman compose up -d
```
*Wait for the emulator to fully boot (check status with `podman inspect --format="{{.State.Health.Status}}" android-emulator`)*.

#### 2. Access the UI
Open **[http://localhost:6080](http://localhost:6080)** in your web browser to view the Android screen.

#### 3. Install the APK
You can install the APK using your local ADB or directly via the container.

**Option A: Using Container (No local setup required)**
```bash
# 1. Copy the APK into the container
podman cp app/build/outputs/apk/debug/app-debug.apk android-emulator:/tmp/app.apk

# 2. Install it using the container's ADB
podman exec android-emulator adb install -r /tmp/app.apk
```

**Option B: Using Local ADB (If installed)**
```bash
adb connect localhost:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### 4. Manual Testing (Send SMS)
Simulate a bank transaction SMS to test the parsing logic:
```bash
# Send a test SMS via ADB
podman exec android-emulator adb emu sms send IndianBank "A/c *8031 debited Rs. 179.00 on 20-12-25 to SRI NANDTHI. UPI:535411655315. Not you? SMS BLOCK to 9289592895. - Indian Bank"
```
*Check the app dashboard to see the transaction appear automatically!*

#### 5. Shutdown
To stop the emulator and free up resources:
```bash
podman compose down
```
