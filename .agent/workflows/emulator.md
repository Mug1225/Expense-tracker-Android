---
description: How to setup and use the containerized Android emulator
---

This workflow guides you through setting up the containerized Android emulator and interacting with it (e.g., installing APKs, sending SMS).

### 1. Prerequisites
Ensure you have the following installed:
- Podman
- Podman Compose (or the Docker Compose provider for Podman)
- KVM support enabled (for hardware acceleration)

### 2. Startup
To start the emulator, run the following command from the project root:
// turbo
```bash
podman compose up -d
```
Access the emulator's web interface at [http://localhost:6080](http://localhost:6080).

### 3. Install the APK
Once the emulator is healthy, copy and install the APK using the container's internal ADB:
// turbo
```bash
podman cp app/build/outputs/apk/debug/app-debug.apk android-emulator:/tmp/app-debug.apk
podman exec android-emulator adb install /tmp/app-debug.apk
```

### 4. Launch the Application
Launch the Expense Tracker app on the emulator:
// turbo
```bash
podman exec android-emulator adb shell monkey -p com.example.expensetracker -c android.intent.category.LAUNCHER 1
```

### 5. Send a Test SMS
To simulate an incoming SMS and test the expense tracking logic:
// turbo
```bash
podman exec android-emulator adb emu sms send <SENDER_NUMBER> "<MESSAGE_CONTENT>"
```
Example:
```bash
podman exec android-emulator adb emu sms send 1234567890 "Rs 50 debited at Amazon"
```

### 6. Troubleshooting & Logs
To view the app's logs and verify if the `SmsReceiver` is working:
// turbo
```bash
podman exec android-emulator adb logcat -s SmsReceiver
```
To stop the emulator:
// turbo
```bash
podman compose down
```
