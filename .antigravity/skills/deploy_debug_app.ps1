# Skill: Deploy Debug App to Connected Device
# This skill bypasses IDE JDK limitations and deploys the app to the user's USB-connected device.

Write-Host "Stopping any stuck Gradle daemons to clear environment caches..."
.\gradlew --stop

Write-Host "Building and deploying Debug APK to connected Android device..."
# The gradle.properties already contains org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr
# But we can also force it here just in case:
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

.\gradlew installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Deployment successful! Check the connected device."
} else {
    Write-Host "❌ Deployment failed. Please ensure a device is connected (check Android Studio) and ADB is working."
}
