package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kidquiz_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN = "parent_pin"
        private const val KEY_STREAK = "quiz_streak"
        private const val KEY_LAST_DATE = "last_quiz_date"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_DAILY_GOAL = "daily_quiz_goal"
        private const val KEY_CHILD_NAME = "child_name"
        private const val KEY_CHILD_PHOTO_URI = "child_photo_uri"
        private const val KEY_CHILD_AGE = "child_age"
        private const val KEY_CHILD_CLASS = "child_class"
        private const val KEY_AI_API_KEY = "ai_api_key"
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_QUESTIONS_PER_QUIZ = "questions_per_quiz"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
        private const val KEY_CUSTOM_SUBJECTS = "custom_subjects"
        private const val KEY_DEFAULT_QUESTIONS_CLEARED = "default_questions_cleared"
        private const val KEY_SUBJECT_TIMERS = "subject_timers"
        private const val KEY_LAST_CLAIMED_LEVEL = "last_claimed_level"
        private const val KEY_QUIZ_DATES_HISTORY = "quiz_dates_history"
        private const val KEY_SOUND_MUTED = "sound_muted"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_CELEBRATED_MILESTONES = "celebrated_milestones"
        private const val HISTORY_CAP = 60
    }

    var defaultQuestionsCleared: Boolean
        get() = prefs.getBoolean(KEY_DEFAULT_QUESTIONS_CLEARED, false)
        set(value) = prefs.edit().putBoolean(KEY_DEFAULT_QUESTIONS_CLEARED, value).apply()

    var subjectTimers: Map<String, Int> // SubjectName to minutes
        get() {
            val jsonStr = prefs.getString(KEY_SUBJECT_TIMERS, "{}") ?: "{}"
            return try {
                val json = org.json.JSONObject(jsonStr)
                val map = mutableMapOf<String, Int>()
                json.keys().forEach { key ->
                    map[key] = json.getInt(key)
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }
        }
        set(value) {
            val json = org.json.JSONObject()
            value.forEach { (k, v) -> json.put(k, v) }
            prefs.edit().putString(KEY_SUBJECT_TIMERS, json.toString()).apply()
        }

    var parentPin: String
        get() = prefs.getString(KEY_PIN, "1234") ?: "1234"
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var streakCount: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    var lastQuizDate: String
        get() = prefs.getString(KEY_LAST_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_DATE, value).apply()

    var totalStars: Int
        get() = prefs.getInt(KEY_TOTAL_STARS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_STARS, value).apply()

    var dailyQuizGoal: Int
        get() = prefs.getInt(KEY_DAILY_GOAL, 1) // default 1 quiz per day
        set(value) = prefs.edit().putInt(KEY_DAILY_GOAL, value).apply()

    var questionsPerQuiz: Int
        get() = prefs.getInt(KEY_QUESTIONS_PER_QUIZ, 5) // default 5 questions per quiz
        set(value) = prefs.edit().putInt(KEY_QUESTIONS_PER_QUIZ, value).apply()

    var childName: String
        get() = prefs.getString(KEY_CHILD_NAME, "Alex") ?: "Alex"
        set(value) = prefs.edit().putString(KEY_CHILD_NAME, value).apply()

    var childAge: String
        get() = prefs.getString(KEY_CHILD_AGE, "8") ?: "8"
        set(value) = prefs.edit().putString(KEY_CHILD_AGE, value).apply()

    var childClass: String
        get() = prefs.getString(KEY_CHILD_CLASS, "Grade 3") ?: "Grade 3"
        set(value) = prefs.edit().putString(KEY_CHILD_CLASS, value).apply()

    var childPhotoUri: String
        get() = prefs.getString(KEY_CHILD_PHOTO_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CHILD_PHOTO_URI, value).apply()

    var aiApiKey: String
        get() = prefs.getString(KEY_AI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AI_API_KEY, value).apply()

    var aiProvider: String
        get() = prefs.getString(KEY_AI_PROVIDER, "openai") ?: "openai"
        set(value) = prefs.edit().putString(KEY_AI_PROVIDER, value).apply()

    var customCategories: Set<String>
        get() = prefs.getStringSet(KEY_CUSTOM_CATEGORIES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_CUSTOM_CATEGORIES, value).apply()

    var customSubjects: Set<String>
        get() = prefs.getStringSet(KEY_CUSTOM_SUBJECTS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_CUSTOM_SUBJECTS, value).apply()

    fun updateStreak() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val yesterday = sdf.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        val lastDate = lastQuizDate
        if (lastDate == today) {
            // Already did a quiz today, don't increment streak again
            return
        } else if (lastDate == yesterday) {
            // Completed yesterday, streak continues!
            streakCount = streakCount + 1
        } else if (lastDate.isEmpty()) {
            // First time completing a quiz
            streakCount = 1
            celebratedMilestones = emptySet()
        } else {
            // More than a day gap, start new streak from 1
            streakCount = 1
            // New streak run: earlier milestones can be celebrated again
            celebratedMilestones = emptySet()
        }
        lastQuizDate = today
    }

    var soundMuted: Boolean
        get() = prefs.getBoolean(KEY_SOUND_MUTED, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_MUTED, value).apply()

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var celebratedMilestones: Set<String>
        get() = prefs.getStringSet(KEY_CELEBRATED_MILESTONES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_CELEBRATED_MILESTONES, value).apply()

    fun hasCelebratedMilestone(milestone: Int): Boolean =
        milestone.toString() in celebratedMilestones

    fun markMilestoneCelebrated(milestone: Int) {
        celebratedMilestones = celebratedMilestones + milestone.toString()
    }

    /** Days (yyyy-MM-dd) on which at least one quiz was completed; drives the streak calendar. */
    fun getQuizDates(): Set<String> {
        val raw = prefs.getString(KEY_QUIZ_DATES_HISTORY, null)
        if (raw == null) {
            // Migration: seed history from the legacy single lastQuizDate
            return if (lastQuizDate.isNotEmpty()) setOf(lastQuizDate) else emptySet()
        }
        return try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun recordQuizDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val dates = (getQuizDates() + today).sorted().takeLast(HISTORY_CAP)
        val arr = org.json.JSONArray()
        dates.forEach { arr.put(it) }
        prefs.edit().putString(KEY_QUIZ_DATES_HISTORY, arr.toString()).apply()
    }

    fun addStars(count: Int) {
        totalStars = totalStars + count
    }

    var lastClaimedLevel: Int
        get() = prefs.getInt(KEY_LAST_CLAIMED_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_CLAIMED_LEVEL, value).apply()
}
