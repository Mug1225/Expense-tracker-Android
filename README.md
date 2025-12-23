# SpendWise 🚀

SpendWise is a premium, native Android application designed to make expense tracking effortless. By automatically parsing incoming SMS alerts from banks and merchants, SpendWise provides real-time insights into your spending habits with a clean, modern interface.

Built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **Hilt** for Dependency Injection.

## ✨ Key Features

- **Automated SMS Tracking**: Instantly logs expenses by parsing bank SMS alerts using optimized regex logic.
- **🏷️ Merchant Tagging**: Organize your spending with multiple tags. Tags replace merchant names as primary labels, providing a cleaner and more personalized view.
- **📊 Interactive Category Summaries**: Tap on any category in the summary tile to instantly filter your transactions for the month.
- **🧠 Smart Categorization**: Learns from your input to automatically assign categories and tags to future transactions from the same merchant.
- **🛠️ Manual Management**: Add manual transactions, edit parsed data, and manage categories with custom icons and intelligent deletion strategies.
- **📅 Month-wise Analysis**: Seamlessly navigate through months with reactive spending summaries and aggregated category data.

## 🏗️ Project Structure

### Root Directory
- **Containerfile**: Dockerfile for a reproducible Android build environment.
- **compose.yaml**: Orchestration for the containerized Android emulator.

### Core Components (`app/src/main/java/com/example/expensetracker/`)

#### Application Entry & Logic
- **`MainActivity.kt`**: The single Activity managing navigation, permissions, and the SpendWise UI.
- **`SmsReceiver.kt`**: Listens for incoming SMS alerts and triggers the parsing/auto-categorization engine.
- **`SmsParser.kt`**: Core utility for extracting financial details from raw message text.

#### Data Layer (`/data`)
- **`Transaction.kt`**: Room Entity supporting amounts, dates, categories, and custom **tags**.
- **`MerchantMapping.kt`**: Stores user-defined mappings for merchants, categories, and tags to enable "smart learning."
- **`AdvancedDaos.kt`**: DAOs for complex queries, including month-wise filtering and spending aggregation.

#### UI Layer (`/ui`)
- **`HomeScreen.kt`**: The main dashboard featuring interactive summaries, filtered transaction lists, and a sleek branding header.
- **`CategoryScreen.kt`**: Full category management with specialized deletion strategies (keep vs. unlink spending).
- **`EditTransactionDialog.kt`**: Comprehensive dialog for overriding transaction data, tags, and merchant mappings.
- **`AddTransactionDialog.kt`**: Fluid interface for manual transaction entry with full tag support.

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

1. **Start the Emulator**:
   ```bash
   podman compose up -d
   ```

2. **Access the UI**:
   Open [http://localhost:6080](http://localhost:6080) in your browser.

3. **Install the APK**:
   ```bash
   adb connect localhost:5555
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
