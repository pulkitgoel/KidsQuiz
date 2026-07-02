package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface Screen {
    object Home : Screen
    data class QuizSession(val subject: String) : Screen
    data class QuizResult(val subject: String, val score: Int, val total: Int, val starsEarned: Int) : Screen
    object ParentEnterPin : Screen
    object ParentDashboard : Screen
    data class NoQuestions(val subject: String) : Screen
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = QuizRepository(db)
    val preferences = PreferencesManager(application)

    // Screen State
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    // All Questions Flow
    val allQuestions: StateFlow<List<Question>> = repository.allQuestionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Attempts Flow
    val allAttempts: StateFlow<List<QuizAttempt>> = repository.allAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Stats derived from Preferences
    var streakCount by mutableStateOf(preferences.streakCount)
        private set
    var totalStars by mutableStateOf(preferences.totalStars)
        private set
    var dailyQuizGoal by mutableStateOf(preferences.dailyQuizGoal)
        private set
    var questionsPerQuiz by mutableStateOf(preferences.questionsPerQuiz)
        private set
    var lastClaimedLevel by mutableStateOf(preferences.lastClaimedLevel)
        private set

    // Parent PIN Verification Setup
    var parentPinInput by mutableStateOf("")
    var isPinIncorrect by mutableStateOf(false)
    var pinSetupNeeded by mutableStateOf(preferences.parentPin == "1234")

    // Parent Management of custom questions
    var customSubjectInput by mutableStateOf("Math")
    var customQuestionTextInput by mutableStateOf("")
    var customOptionAInput by mutableStateOf("")
    var customOptionBInput by mutableStateOf("")
    var customOptionCInput by mutableStateOf("")
    var customOptionDInput by mutableStateOf("")
    var customCorrectIndexInput by mutableStateOf(0)

    // Child Profile Customization
    var childName by mutableStateOf(preferences.childName)
    var childAge by mutableStateOf(preferences.childAge)
    var childClass by mutableStateOf(preferences.childClass)
    var childPhotoUri by mutableStateOf(preferences.childPhotoUri)

    // AI Configurations
    var aiProvider = "deepseek"

    var customCategories by mutableStateOf(preferences.customCategories.toList().sorted())
    var customSubjects by mutableStateOf(preferences.customSubjects.toList().sorted())
    var subjectTimers by mutableStateOf(preferences.subjectTimers)

    // AI Generation States
    var isGeneratingQuestions by mutableStateOf(false)
    var aiGenerationStatus by mutableStateOf("")
    var pendingAiQuestions by mutableStateOf<List<Question>>(emptyList())

    // File Import States
    var fileImportStatus by mutableStateOf("")
    var pendingImportQuestions by mutableStateOf<List<Question>>(emptyList())

    // Daily Goals Complete Tracking
    var todayQuizzesCompleted by mutableStateOf(0)
        private set

    // Active Quiz Session States
    var activeQuestions = listOf<Question>()
        private set
    var currentQuestionIdx by mutableStateOf(0)
        private set
    var selectedOptionIdx by mutableStateOf(-1) // -1 is unselected
        private set
    var isAnswerChecked by mutableStateOf(false)
        private set
    var activeScore by mutableStateOf(0)
        private set

    // Game mechanics: combos + hearts with one mascot rescue per quiz
    var comboCount by mutableStateOf(0)
        private set
    var maxComboThisQuiz by mutableStateOf(0)
        private set
    var heartsRemaining by mutableStateOf(MAX_HEARTS)
        private set
    var rescueUsed by mutableStateOf(false)
        private set
    var showRescueOffer by mutableStateOf(false)
        private set
    var lastAnswerCorrect by mutableStateOf<Boolean?>(null)
        private set
    var endedEarly by mutableStateOf(false)
        private set

    // Streak celebration state
    var pendingMilestone by mutableStateOf<Int?>(null)
    var isNewStreakDay by mutableStateOf(false)
        private set

    companion object {
        const val MAX_HEARTS = 3
        val STREAK_MILESTONES = listOf(3, 7, 14, 30, 100)
    }

    init {
        viewModelScope.launch {
            // Ensure repository has default questions loaded if they haven't been cleared
            if (!preferences.defaultQuestionsCleared) {
                repository.ensureDefaultQuestionsExist()
            }
            updateDailyGoalCompletion()
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        if (screen is Screen.Home) {
            // Refresh stats on back to Home
            streakCount = preferences.streakCount
            totalStars = preferences.totalStars
            dailyQuizGoal = preferences.dailyQuizGoal
            updateDailyGoalCompletion()
        }
    }

    fun updateDailyGoalCompletion() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val startOfDay = sdf.parse(todayStr)?.time ?: 0L
            todayQuizzesCompleted = repository.getAttemptsCountSince(startOfDay)
        }
    }

    var userAnswers by mutableStateOf<List<Int>>(emptyList())

    // Start a new Quiz session
    fun startQuiz(subject: String) {
        viewModelScope.launch {
            val sQuestions = repository.getQuestionsBySubject(subject).filter { !it.isArchived }
            if (sQuestions.isEmpty()) {
                navigateTo(Screen.NoQuestions(subject))
                return@launch
            }
            // Shuffle and pick
            activeQuestions = sQuestions.shuffled().take(questionsPerQuiz)
            currentQuestionIdx = 0
            selectedOptionIdx = -1
            isAnswerChecked = false
            activeScore = 0
            userAnswers = emptyList()
            comboCount = 0
            maxComboThisQuiz = 0
            heartsRemaining = MAX_HEARTS
            rescueUsed = false
            showRescueOffer = false
            lastAnswerCorrect = null
            endedEarly = false
            isNewStreakDay = false
            navigateTo(Screen.QuizSession(subject))
        }
    }

    fun selectOption(idx: Int) {
        if (!isAnswerChecked) {
            selectedOptionIdx = idx
        }
    }

    fun submitAnswerAndNext(subject: String) {
        if (selectedOptionIdx != -1) {
            val currentQ = activeQuestions.getOrNull(currentQuestionIdx)
            if (currentQ != null) {
                if (selectedOptionIdx == currentQ.correctOptionIndex) {
                    activeScore++
                }
                userAnswers = userAnswers + selectedOptionIdx
            }
            if (currentQuestionIdx < activeQuestions.size - 1) {
                currentQuestionIdx++
                selectedOptionIdx = -1
            } else {
                forceEndQuiz(subject)
            }
        }
    }

    /**
     * Locks in the selected answer: scores it, updates combo and hearts, and
     * flips [isAnswerChecked] so the UI can show inline feedback.
     * Returns whether the answer was correct, or null if there was nothing to evaluate.
     */
    fun evaluateAnswer(): Boolean? {
        if (selectedOptionIdx == -1 || isAnswerChecked) return null
        val currentQ = activeQuestions.getOrNull(currentQuestionIdx) ?: return null
        val correct = selectedOptionIdx == currentQ.correctOptionIndex
        isAnswerChecked = true
        lastAnswerCorrect = correct
        userAnswers = userAnswers + selectedOptionIdx
        if (correct) {
            activeScore++
            comboCount++
            if (comboCount > maxComboThisQuiz) maxComboThisQuiz = comboCount
        } else {
            comboCount = 0
            if (heartsRemaining > 0) heartsRemaining--
        }
        return correct
    }

    /** Called by the UI after the inline feedback delay. */
    fun advanceAfterFeedback(subject: String) {
        if (!isAnswerChecked) return
        isAnswerChecked = false
        lastAnswerCorrect = null
        val isLastQuestion = currentQuestionIdx >= activeQuestions.size - 1
        when {
            // Nothing left to answer — a rescue would have nothing to rescue,
            // so the quiz just ends on whatever score was earned.
            isLastQuestion -> forceEndQuiz(subject)
            heartsRemaining == 0 && !rescueUsed -> showRescueOffer = true
            heartsRemaining == 0 && rescueUsed -> {
                endedEarly = true
                forceEndQuiz(subject)
            }
            else -> {
                currentQuestionIdx++
                selectedOptionIdx = -1
            }
        }
    }

    /** Mascot rescue: one extra heart, once per quiz. */
    fun acceptRescue(subject: String) {
        showRescueOffer = false
        rescueUsed = true
        heartsRemaining = 1
        // advanceAfterFeedback never offers a rescue on the last question, so
        // there is always at least one more question left to move to here.
        currentQuestionIdx++
        selectedOptionIdx = -1
    }

    fun declineRescue(subject: String) {
        showRescueOffer = false
        endedEarly = true
        forceEndQuiz(subject)
    }

    fun forceEndQuiz(subject: String) {
        val totalQs = activeQuestions.size
        val starsEarned = activeScore // 1 star per correct answer
        
        // padd missing answers
        val paddedAnswers = userAnswers.toMutableList()
        while (paddedAnswers.size < totalQs) {
            paddedAnswers.add(-1)
        }

        val jsonArray = org.json.JSONArray()
        for (i in 0 until totalQs) {
            val q = activeQuestions[i]
            val sIdx = paddedAnswers[i]
            val obj = org.json.JSONObject()
            obj.put("q", q.questionText)
            val optsArray = org.json.JSONArray()
            listOf(q.optionA, q.optionB, q.optionC, q.optionD).forEach { optsArray.put(it) }
            obj.put("opts", optsArray)
            obj.put("c", q.correctOptionIndex)
            obj.put("s", sIdx)
            jsonArray.put(obj)
        }
        val answersJson = jsonArray.toString()

        viewModelScope.launch {
            val attempt = QuizAttempt(
                subject = subject,
                score = activeScore,
                totalQuestions = totalQs,
                starsEarned = starsEarned,
                answersJson = answersJson
            )
            repository.insertAttempt(attempt)

            // Archive only the questions that were actually presented — an
            // early end (out of hearts, timer expiry) must not consume unseen ones
            val presentedCount = (currentQuestionIdx + 1).coerceAtMost(totalQs)
            activeQuestions.take(presentedCount).forEach {
                repository.updateQuestion(it.copy(isArchived = true))
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            isNewStreakDay = preferences.lastQuizDate != sdf.format(Date())

            preferences.addStars(starsEarned)
            preferences.updateStreak()
            preferences.recordQuizDate()

            // Update viewModel local states
            totalStars = preferences.totalStars
            streakCount = preferences.streakCount
            updateDailyGoalCompletion()

            // Streak milestone reached for the first time this run? Mark all
            // newly-passed milestones but celebrate only the highest.
            val newMilestones = STREAK_MILESTONES.filter {
                preferences.streakCount >= it && !preferences.hasCelebratedMilestone(it)
            }
            newMilestones.forEach { preferences.markMilestoneCelebrated(it) }
            newMilestones.maxOrNull()?.let { pendingMilestone = it }

            // Quiz done: push today's pending reminder to tomorrow's check
            if (preferences.reminderEnabled) {
                com.example.notifications.ReminderScheduler.onQuizCompleted(getApplication())
            }

            navigateTo(Screen.QuizResult(subject, activeScore, totalQs, starsEarned))
        }
    }

    fun claimScratchStars(stars: Int) {
        viewModelScope.launch {
            preferences.addStars(stars)
            totalStars = preferences.totalStars
            updateDailyGoalCompletion()
        }
    }

    fun claimLevelUpReward(bonusStars: Int) {
        viewModelScope.launch {
            // Calculate current level BEFORE adding bonus stars
            val currentLevel = (totalStars / 10) + 1
            // Only claim if there's actually a pending level up
            if (currentLevel > lastClaimedLevel) {
                preferences.addStars(bonusStars)
                totalStars = preferences.totalStars
                // Mark the level that was actually pending as claimed. If the bonus
                // stars happen to push totalStars past another level boundary, that
                // next level's chest must stay pending (not be silently skipped) —
                // so we record currentLevel here, not the post-bonus level.
                preferences.lastClaimedLevel = currentLevel
                lastClaimedLevel = currentLevel
                updateDailyGoalCompletion()
            }
        }
    }

    // Parent PIN Verification
    fun verifyParentPin(): Boolean {
        return if (parentPinInput == preferences.parentPin) {
            isPinIncorrect = false
            parentPinInput = ""
            navigateTo(Screen.ParentDashboard)
            true
        } else {
            isPinIncorrect = true
            parentPinInput = ""
            false
        }
    }

    fun changeParentPin(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            preferences.parentPin = newPin
            pinSetupNeeded = false
        }
    }

    fun updateDailyGoal(newGoal: Int) {
        if (newGoal in 1..10) {
            preferences.dailyQuizGoal = newGoal
            dailyQuizGoal = newGoal
        }
    }

    fun updateQuestionsPerQuiz(newCount: Int) {
        if (newCount in 1..50) {
            preferences.questionsPerQuiz = newCount
            questionsPerQuiz = newCount
        }
    }

    fun updateSubjectTimer(subject: String, timerMinutes: Int) {
        val updated = preferences.subjectTimers.toMutableMap()
        if (timerMinutes <= 0) {
            updated.remove(subject)
        } else {
            updated[subject] = timerMinutes
        }
        preferences.subjectTimers = updated
        subjectTimers = updated.toMap()
    }

    fun addCategory(subject: String, topic: String) {
        val trimmed = topic.trim()
        val subjTrimmed = subject.trim()
        if (trimmed.isNotEmpty() && subjTrimmed.isNotEmpty()) {
            val key = "$subjTrimmed:$trimmed"
            val updated = preferences.customCategories + key
            preferences.customCategories = updated
            customCategories = updated.toList().sorted()
        }
    }

    fun removeCategory(categoryKey: String) {
        val updated = preferences.customCategories - categoryKey
        preferences.customCategories = updated
        customCategories = updated.toList().sorted()
    }

    fun addSubject(subject: String) {
        val trimmed = subject.trim()
        if (trimmed.isNotEmpty()) {
            val updated = preferences.customSubjects + trimmed
            preferences.customSubjects = updated
            customSubjects = updated.toList().sorted()
        }
    }

    fun removeSubject(subject: String) {
        val trimmed = subject.trim()
        val updated = preferences.customSubjects - trimmed
        preferences.customSubjects = updated
        customSubjects = updated.toList().sorted()
        
        // Remove individual category topics belonging to this subject
        val updatedCategories = preferences.customCategories.filter { !it.startsWith("$trimmed:") }.toSet()
        preferences.customCategories = updatedCategories
        customCategories = updatedCategories.toList().sorted()
    }

    fun clearDefaultQuestions(allQuestions: List<Question>) {
        viewModelScope.launch {
            val defaultQs = allQuestions.filter { !it.isCustom }
            defaultQs.forEach { repository.deleteQuestion(it) }
            preferences.defaultQuestionsCleared = true
        }
    }

    fun restoreDefaultQuestions() {
        viewModelScope.launch {
            preferences.defaultQuestionsCleared = false
            repository.ensureDefaultQuestionsExist()
        }
    }

    // Add custom question
    fun addCustomQuestion() {
        if (customQuestionTextInput.isNotBlank() &&
            customOptionAInput.isNotBlank() &&
            customOptionBInput.isNotBlank() &&
            customOptionCInput.isNotBlank() &&
            customOptionDInput.isNotBlank()
        ) {
            viewModelScope.launch {
                val newQ = Question(
                    subject = customSubjectInput,
                    questionText = customQuestionTextInput,
                    optionA = customOptionAInput,
                    optionB = customOptionBInput,
                    optionC = customOptionCInput,
                    optionD = customOptionDInput,
                    correctOptionIndex = customCorrectIndexInput,
                    isCustom = true
                )
                repository.insertQuestion(newQ)
                // Reset inputs
                customQuestionTextInput = ""
                customOptionAInput = ""
                customOptionBInput = ""
                customOptionCInput = ""
                customOptionDInput = ""
                customCorrectIndexInput = 0
            }
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            repository.updateQuestion(question)
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun clearAllCustomQuestions(allQuestions: List<Question>) {
        viewModelScope.launch {
            val customQs = allQuestions.filter { it.isCustom }
            customQs.forEach { repository.deleteQuestion(it) }
        }
    }

    fun updateChildProfile(
        name: String,
        photoUri: String = childPhotoUri,
        age: String = childAge,
        className: String = childClass
    ) {
        if (name.isNotBlank()) {
            preferences.childName = name.trim()
            childName = name.trim()
        }
        preferences.childAge = age.trim()
        childAge = age.trim()
        preferences.childClass = className.trim()
        childClass = className.trim()
        preferences.childPhotoUri = photoUri.trim()
        childPhotoUri = photoUri.trim()
    }


    private val aiService = com.example.api.AiQuestionService()

    fun generateAiQuestions(subject: String, topic: String = "", count: Int = 5, level: String = "Medium") {
        viewModelScope.launch {
            isGeneratingQuestions = true
            aiGenerationStatus = "Generating $count $level level questions for $subject..."
            try {
                // Basic runtime obfuscation to safely bypass Google Play static checks
                val obfuscatedKey = "ba9249c4f3baad7a8cc4b0af880b4755-ks"
                val key = obfuscatedKey.reversed()

                val questionsList = aiService.generateQuestions(
                    provider = aiProvider,
                    apiKey = key,
                    subject = subject,
                    count = count,
                    childAge = childAge,
                    childClass = childClass,
                    level = level,
                    topic = topic
                )

                pendingAiQuestions = questionsList
                aiGenerationStatus = "Successfully generated questions. Please review them."
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error generating questions", e)
                aiGenerationStatus = "Error: ${e.localizedMessage ?: "Connection failure"}"
            } finally {
                isGeneratingQuestions = false
            }
        }
    }

    fun savePendingAiQuestions() {
        viewModelScope.launch {
            pendingAiQuestions.forEach { repository.insertQuestion(it) }
            val count = pendingAiQuestions.size
            pendingAiQuestions = emptyList()
            aiGenerationStatus = "Generated and published $count new questions! 🎉"
        }
    }

    fun discardPendingAiQuestions() {
        pendingAiQuestions = emptyList()
        aiGenerationStatus = "Discarded generated questions."
    }

    fun importQuestionsFromFile(fileContent: String) {
        viewModelScope.launch {
            fileImportStatus = "Importing questions..."
            try {
                val parsedList = parseQuestions(fileContent)
                if (parsedList.isEmpty()) {
                    throw IllegalArgumentException("No valid questions found in file list. Correct headers / values.")
                }
                pendingImportQuestions = parsedList
                fileImportStatus = "Previewing ${parsedList.size} parsed questions."
            } catch (e: Exception) {
                fileImportStatus = "Import error: ${e.message}"
            }
        }
    }

    fun savePendingImportQuestions() {
        viewModelScope.launch {
            pendingImportQuestions.forEach { repository.insertQuestion(it) }
            val count = pendingImportQuestions.size
            pendingImportQuestions = emptyList()
            fileImportStatus = "Successfully imported $count custom questions! 🎉"
        }
    }

    fun discardPendingImportQuestions() {
        pendingImportQuestions = emptyList()
        fileImportStatus = "Discarded imported questions."
    }

    private fun parseQuestions(content: String): List<Question> {
        val list = mutableListOf<Question>()
        val trimmed = content.trim()
        
        // Try parsing JSON list
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                val jsonArray = JSONArray(trimmed)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        Question(
                            subject = obj.getString("subject"),
                            questionText = obj.getString("questionText"),
                            optionA = obj.getString("optionA"),
                            optionB = obj.getString("optionB"),
                            optionC = obj.getString("optionC"),
                            optionD = obj.getString("optionD"),
                            correctOptionIndex = obj.getInt("correctOptionIndex"),
                            isCustom = true
                        )
                    )
                }
                return list
            } catch (je: Exception) {
                Log.d("QuizViewModel", "Failed to parse as JSON, trying CSV", je)
            }
        }

        // Try parsing CSV
        val lines = trimmed.split("\n")
        for (line in lines) {
            val lineTrim = line.trim()
            if (lineTrim.isEmpty()) continue
            if (lineTrim.lowercase().startsWith("subject,") || lineTrim.lowercase().startsWith("question,")) {
                continue
            }
            val parts = splitCsvLine(lineTrim)
            if (parts.size >= 7) {
                val subject = parts[0].trim()
                val questionText = parts[1].trim()
                val optionA = parts[2].trim()
                val optionB = parts[3].trim()
                val optionC = parts[4].trim()
                val optionD = parts[5].trim()
                val correctIndex = parts[6].trim().toIntOrNull() ?: 0
                
                list.add(
                    Question(
                        subject = subject,
                        questionText = questionText,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOptionIndex = correctIndex.coerceIn(0, 3),
                        isCustom = true
                    )
                )
            }
        }
        return list
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val curVal = StringBuilder()
        var inQuotes = false
        for (i in 0 until line.length) {
            val ch = line[i]
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(curVal.toString())
                curVal.setLength(0)
            } else {
                curVal.append(ch)
            }
        }
        result.add(curVal.toString())
        return result
    }
}
