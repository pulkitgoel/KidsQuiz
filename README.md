# KidQuiz 🎒

KidQuiz is a smart, interactive educational app built with **Jetpack Compose**. It helps children learn through fun quizzes while providing parents with powerful administrative tools to manage content and track progress.

## ✨ Features

### 👦 Kid Mode
*   **Multiple Subjects:** Math, English, Science, and General Knowledge.
*   **Gamification:** Earn stars for correct answers and maintain a daily learning streak.
*   **Daily Goals:** A visual progress bar on the home screen helps kids reach their daily learning targets.
*   **Timed Challenges:** Optional per-subject timers with alarming sound effects in the final seconds to add excitement.
*   **Smart Feedback:** Instant 5-star ratings and motivational messages based on performance.

### 🛡️ Parent Dashboard (PIN Protected)
*   **Score History:** Detailed reports of every quiz attempt, including specific questions missed.
*   **AI Question Generator:** Instantly generate brand-new, age-appropriate questions using OpenAI or DeepSeek.
*   **Custom Bank Management:** Add your own questions manually or import them via CSV/JSON files.
*   **Archive System:** Questions are automatically archived after a test to keep the bank fresh. Parents can reactivate them anytime from the dedicated Archive tab.
*   **Profile Personalization:** Customize the child's name, age, grade, and profile photo.

## 🛠️ Tech Stack
*   **UI:** Jetpack Compose
*   *Database:** Room (Offline local persistence)
*   **Network:** OkHttp
*   **AI:** OpenAI & DeepSeek API integrations
*   **Images:** Coil & Android-Image-Cropper

## 🚀 Getting Started

### Prerequisites
*   [Android Studio Ladybug](https://developer.android.com/studio) or newer.
*   Android device running **Android 7.0 (API 24)** or higher.

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/pulkitgoel/KidsQuiz.git
    ```
2.  **Setup Environment Variables:**
    Create a file named `.env` in the root directory and add your API keys:
    ```env
    OPENAI_API_KEY=your_openai_key_here
    DEEPSEEK_API_KEY=your_deepseek_key_here
    ```
3.  **Open in Android Studio:**
    *   Select **Open** and choose the `kidquiz` directory.
    *   Let the Gradle sync complete.
4.  **Run the App:**
    *   Connect your device via USB or start an emulator.
    *   Press **Shift + F10** or the **Run** button.

## 📝 Default Credentials
The Parent Dashboard is protected by a default PIN: **`1234`**. 
You can change this anytime in the **Settings & Access** tab inside the dashboard.

---
Developed by [Pulkit Goel](https://github.com/pulkitgoel)
