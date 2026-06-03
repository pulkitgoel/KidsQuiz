# Google Play Console Publishing Instructions

This guide covers everything you need to know about publishing your Android App Bundle to the Google Play Console for internal testing.

## 1. The App Bundle (.aab)
Your app bundle has been successfully generated and is located at:
`app\build\outputs\bundle\release\app-release.aab`

The package name has been updated to **`com.pulkitgoel.smartkidsquiz`**, which uniquely identifies your app on Google Play.

## 2. Uploading to Google Play Console
1. Go to your [Google Play Console](https://play.google.com/console).
2. Select your app from the Dashboard.
3. In the left menu, navigate to **Testing > Internal testing**.
4. Click on the **Create new release** button.
5. In the **App bundles** section, you will see a box that says "Drop app bundles here to upload".
6. Drag and drop the `app-release.aab` file from your computer into that box.

## 3. Release Details
* **Release Name**: `v1.0 - Gamification & Animations Update`
* **Release Notes**:
```text
<en-US>
- Added a fun, new interactive animated background for kids!
- Meet our new animated characters: a happy sun for correct answers and a rainy cloud for oopsies!
- Introduced Magic Scratch Cards! Scratch to reveal bonus stars.
- Added a new Level-up Magic Chest on the home screen to unlock surprise rewards!
- Earn new Learning Badges (Math Wiz, Word Star, Explorer, and more) as you play.
- Smooth bouncy animations and colorful UI enhancements throughout the app.
- Various bug fixes and stability improvements.
</en-US>
```

## 4. Finalizing the Release
1. Once the upload finishes, scroll down and click **Next** or **Save**.
2. If there are no errors, click **Start rollout to Internal testing**.
3. Now, anyone in your designated internal testers list can download and test the app using the invite link provided on the Internal Testing page!

## Keystore Details
Your app is signed with an upload key (`my-upload-key.jks`), which is located in the root of the project directory. Please keep this file secure! If you lose it, you will need to request a key reset from Google Play support for future updates.
