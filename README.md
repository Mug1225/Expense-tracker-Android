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
  - `parseSms()`: Extracts amount, merchant name, and date from raw SMS text. currently supports generic formats like "Rs. 500 debited at Amazon".

#### Data Layer (`/data`)
- **`Transaction.kt`**: The Room Entity representing a single expense record in the database.
- **`TransactionDao.kt`**: Data Access Object defining SQL queries (INSERT, SELECT ALL, SELECT SUM).
- **`AppDatabase.kt`**: The Room Database abstract class.
- **`TransactionRepository.kt`**: A clean API for the UI and Receiver to access data. Hides the complexity of Room and Coroutines.

#### Dependency Injection (`/di`)
- **`DatabaseModule.kt`**: A Hilt module that provides singleton instances of `AppDatabase` and `TransactionDao` to the dependency graph.

#### UI Layer (`/ui`)
- **`TransactionViewModel.kt`**: The state holder for the UI.
  - Exposes `transactions` (List) and `totalExpense` (Double) as `StateFlow` streams.
  - Survives configuration changes (rotation).
- **`HomeScreen.kt`**: The main dashboard.
  - Displays the total expense summary.
  - Lists individual transactions using a `LazyColumn`.
- **`PermissionRequestScreen.kt`**: A user-friendly screen explaining why the app needs SMS permissions.

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
