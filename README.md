# Expense-tracker-Android
This repository contains the containerized application of the Expense-tracker app and another container to install and run the apk.

# Expense Tracker Android App

A native Android application that automatically tracks your expenses by parsing incoming SMS messages from banks. Built with Kotlin, Jetpack Compose, Room Database, and Hilt for Dependency Injection.

## Project Structure

### Root Directory
- **Containerfile**: Dockerfile to create a reproducible Android build environment.
- **build.gradle.kts**: Top-level build configuration.
- **settings.gradle.kts**: Project module settings.

### Core Components (`app/src/main/java/com/example/expensetracker/`)

#### Application Entry Points
- **`ExpenseTrackerApplication.kt`**: The application class annotated with `@HiltAndroidApp`. Triggers Dagger Hilt's code generation for dependency injection.
- **`MainActivity.kt`**: The single Activity for the app. It handles:
  - Checking `READ_SMS` and `RECEIVE_SMS` permissions.
  - Setting up the Jetpack Compose UI content.
  - Navigating between `PermissionRequestScreen` and `HomeScreen` based on permission state.
- **`SmsReceiver.kt`**: A `BroadcastReceiver` that listens for `android.provider.Telephony.SMS_RECEIVED`.
  - Intercepts incoming SMS.
  - Uses `SmsParser` to check for transaction details.
  - Asynchronously saves valid transactions to the database via `TransactionRepository`.

#### Core Logic (`/core`)
- **`SmsParser.kt`**: A utility object containing Regex logic.
  - `parseSms()`: Extracts amount, merchant name, and date from raw SMS text. Optimized for formats like "Rs. 500 debited on 22-Dec to AMAZON".

#### Data Layer (`/data`)
- **`Transaction.kt`**: The Room Entity. Includes `categoryId` and `isEdited` flags.
- **`Category.kt`**: Entity for user-defined expense categories with Material Icon support.
- **`MerchantMapping.kt`**: Maps specific merchant names to categories for auto-assignment.
- **`TransactionDao.kt` / `AdvancedDaos.kt`**: DAOs for complex queries including month-wise filtering and spending aggregation by category.
- **`AppDatabase.kt`**: The Room Database abstract class managing all three entities.
- **`TransactionRepository.kt`**: Orchestrates data flow between DAOs and UI, handling auto-categorization logic.

#### Dependency Injection (`/di`)
- **`DatabaseModule.kt`**: Hilt module providing database and DAO instances.

#### UI Layer (`/ui`)
- **`TransactionViewModel.kt`**: Manages month-based filtering, reactive spending summaries, and transaction updates.
- **`HomeScreen.kt`**: Dashboard featuring a month navigator, category spending summaries, and the transaction list.
- **`CategoryScreen.kt`**: Interface for managing categories with icon suggestions based on category names.
- **`EditTransactionDialog.kt`**: Provides manual override for transaction details and merchant-to-category mapping.
- **`IconHelper.kt`**: Maps category names to relevant Material Icons and provides suggestions.
- **`PermissionRequestScreen.kt`**: Screens explaining the need for SMS permissions.

## Key Features

- **Automated SMS Tracking**: Parses bank SMS alerts to log expenses instantly.
- **Smart Categorization**: Learns from manual edits to auto-assign categories to future transactions from the same merchant.
- **Category Management**: Create custom categories with a library of Material Icons.
- **Month-wise Analysis**: Navigate through months to see total spending and category breakdowns.
- **Manual Editing**: Correct parsed data or manually categorize any transaction.


## Build Instructions

### Standard Build
Open the project in Android Studio and run on an emulator or device.

### Containerized Build (Podman)
You can build the APK without installing Android Studio:

```bash
# 1. Build the builder image
podman build -t android-builder -f Containerfile .

# 2. Compile the APK
podman run --rm -v ".:/app" android-builder gradle assembleDebug
```
The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

### Running with Containerized Emulator

You can run the APK in a containerized Android emulator without installing Android Studio.

1. **Start the Emulator**:
   ```bash
   podman compose up -d
   ```

2. **Access the Emulator UI**:
   Open [http://localhost:6080](http://localhost:6080) in your web browser.

3. **Install and Run the APK**:
   If you have `adb` installed locally:
   ```bash
   adb connect localhost:5555
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   Or use the built-in ADB tools if you prefer.
