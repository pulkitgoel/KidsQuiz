# Skill: Build and Deploy App
**Purpose**: Automatically builds the Debug APK and deploys it to a physically connected USB device or running Emulator.

## Usage
Run the following PowerShell script from the root of the project:
```powershell
.\.antigravity\skills\deploy_debug_app.ps1
```

## How It Works
The script handles the following edge cases:
1. **JDK Environment Mismatch**: If you are running the build from a terminal that is attached to a generic Java environment (like `redhat.java`), the build may fail with errors like `jlink executable does not exist`. To bypass this, the script correctly sets `JAVA_HOME` to Android Studio's embedded JDK (`jbr`) which contains all required Android compilation tools.
2. **Daemon Caching**: It runs `.\gradlew --stop` to kill any existing cached Gradle daemons that might be hanging onto an old incorrect JRE path.
3. **ADB Deployment**: It invokes `.\gradlew installDebug` which uses the Android SDK's bundled `adb` to securely push the `.apk` directly to your active testing device.

## Troubleshooting
- If it fails, ensure your device is unlocked and "USB Debugging" is enabled in Developer Options.
- If no devices are found, open Android Studio once to ensure the ADB server is initialized.
