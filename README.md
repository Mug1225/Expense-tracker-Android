# SpendWise 🚀

SpendWise is a premium, native Android application designed to make expense tracking effortless. By automatically parsing incoming SMS alerts from banks and merchants, SpendWise provides real-time insights into your spending habits with a clean, modern interface.

Built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **Hilt** for Dependency Injection.

## ✨ Key Features

- **🎨 Dynamic Themes**: Personalize your experience with premium themes including **Emerald**, **Ocean**, and **Charcoal**. Access them via the Settings gear.
- **📅 Custom Date Ranges**: Tap the month header to select specific dates (e.g., "Last 2 Weeks") for precise expense tracking.
- **⚙️ Composite Filtering**: Combine date ranges with category filters to see exactly what you spent on "Food" during your "Vacation".
- **🏷️ Custom Categories**: Create categories and choose from a library of icons to visually organize your spending.
- **⚡ Flexible Entry**: Add manual transactions quickly. Merchant and tags are optional, and validity is checked instantly.
- **Automated SMS Tracking**: Instantly logs expenses by parsing bank SMS alerts using optimized regex logic.
- **Merchant Tagging**: Organize your spending with multiple tags. Tags replace merchant names as primary labels, providing a cleaner and more personalized view.
- **Interactive Category Summaries**: Tap on any category in the summary tile to instantly filter your transactions.
- **Smart Categorization**: Learns from your input to automatically assign categories and tags to future transactions from the same merchant.

## 🏗️ Project Structure

### Root Directory
- **Containerfile**: Dockerfile for a reproducible Android build environment.
- **compose.yaml**: Orchestration for the containerized Android emulator.

### Core Components (`app/src/main/java/com/example/expensetracker/`)

#### Application Entry & Logic
- **`MainActivity.kt`**: Manages navigation and dynamic theming context.
- **`SmsReceiver.kt`**: Listens for incoming SMS alerts and triggers the parsing/auto-categorization engine.

#### Data Layer (`/data`)
- **`Transaction.kt`**: Room Entity supporting amounts, dates, categories, and custom **tags**.
- **`MerchantMapping.kt`**: Stores user-defined mappings for merchants, categories, and tags.

#### UI Layer (`/ui`)
- **`HomeScreen.kt`**: The main dashboard featuring interactive summaries, filtered lists, **Date Range Picker**, and **Settings** access.
- **`CategoryScreen.kt`**: Category management with a horizontal **Icon Picker**.
- **`AddTransactionDialog.kt`**: Enhanced dialog with smart defaults for manual entries.
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
