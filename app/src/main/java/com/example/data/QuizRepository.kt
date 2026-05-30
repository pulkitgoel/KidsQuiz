package com.example.data

import kotlinx.coroutines.flow.Flow

class QuizRepository(private val db: AppDatabase) {
    private val questionDao = db.questionDao()
    private val quizAttemptDao = db.quizAttemptDao()

    val allQuestionsFlow: Flow<List<Question>> = questionDao.getAllQuestionsFlow()
    val allAttemptsFlow: Flow<List<QuizAttempt>> = quizAttemptDao.getAllAttemptsFlow()

    suspend fun ensureDefaultQuestionsExist() {
        val count = questionDao.getQuestionCount()
        if (count == 0) {
            val defaultQuestions = listOf(
                // Math
                Question(subject = "Math", questionText = "What is 5 + 4?", optionA = "7", optionB = "8", optionC = "9", optionD = "10", correctOptionIndex = 2),
                Question(subject = "Math", questionText = "What is 10 - 3?", optionA = "5", optionB = "6", optionC = "7", optionD = "8", correctOptionIndex = 2),
                Question(subject = "Math", questionText = "If you have 3 apples and find 4 more, how many do you have?", optionA = "5", optionB = "6", optionC = "7", optionD = "8", correctOptionIndex = 2),
                Question(subject = "Math", questionText = "What is 2 x 4?", optionA = "6", optionB = "8", optionC = "10", optionD = "12", correctOptionIndex = 1),
                Question(subject = "Math", questionText = "What is half of 12?", optionA = "4", optionB = "5", optionC = "6", optionD = "8", correctOptionIndex = 2),

                // English
                Question(subject = "English", questionText = "Which word is a naming word (noun)?", optionA = "Run", optionB = "Beautiful", optionC = "Apple", optionD = "Quickly", correctOptionIndex = 2),
                Question(subject = "English", questionText = "Choose the correct spelling:", optionA = "Recieve", optionB = "Receive", optionC = "Recive", optionD = "Receve", correctOptionIndex = 1),
                Question(subject = "English", questionText = "What is the opposite of 'Happy'?", optionA = "Sad", optionB = "Excited", optionC = "Angry", optionD = "Sleepy", correctOptionIndex = 0),
                Question(subject = "English", questionText = "Which letter is a vowel?", optionA = "B", optionB = "T", optionC = "E", optionD = "R", correctOptionIndex = 2),
                Question(subject = "English", questionText = "Complete the sentence: 'The cat ___ on the mat.'", optionA = "sat", optionB = "sit", optionC = "satting", optionD = "sitted", correctOptionIndex = 0),

                // General Knowledge
                Question(subject = "General Knowledge", questionText = "How many colors are in a rainbow?", optionA = "5", optionB = "6", optionC = "7", optionD = "8", correctOptionIndex = 2),
                Question(subject = "General Knowledge", questionText = "Which is the largest animal on Earth?", optionA = "Elephant", optionB = "Blue Whale", optionC = "Giraffe", optionD = "Dinosaur", correctOptionIndex = 1),
                Question(subject = "General Knowledge", questionText = "How many days are in a regular year?", optionA = "350", optionB = "360", optionC = "365", optionD = "366", correctOptionIndex = 2),
                Question(subject = "General Knowledge", questionText = "Which direction does the sun rise?", optionA = "North", optionB = "East", optionC = "South", optionD = "West", correctOptionIndex = 1),
                Question(subject = "General Knowledge", questionText = "How many legs does a spider have?", optionA = "6", optionB = "8", optionC = "10", optionD = "12", correctOptionIndex = 1),

                // Science
                Question(subject = "Science", questionText = "What planet do we live on?", optionA = "Mars", optionB = "Earth", optionC = "Venus", optionD = "Jupiter", correctOptionIndex = 1),
                Question(subject = "Science", questionText = "Which part of a plant grows under the ground?", optionA = "Leaves", optionB = "Stem", optionC = "Flowers", optionD = "Roots", correctOptionIndex = 3),
                Question(subject = "Science", questionText = "Water turns into ice when it is:", optionA = "Boiled", optionB = "Frozen", optionC = "Warmed", optionD = "Mixed", correctOptionIndex = 1),
                Question(subject = "Science", questionText = "What gives us light and warmth during the day?", optionA = "The Moon", optionB = "The Sun", optionC = "The Stars", optionD = "Clouds", correctOptionIndex = 1),
                Question(subject = "Science", questionText = "Which of these is a gas?", optionA = "Water", optionB = "Wood", optionC = "Air", optionD = "Rock", correctOptionIndex = 2)
            )
            for (q in defaultQuestions) {
                questionDao.insertQuestion(q)
            }
        }
    }

    suspend fun getQuestionsBySubject(subject: String): List<Question> {
        return questionDao.getQuestionsBySubject(subject)
    }

    suspend fun insertQuestion(question: Question) {
        questionDao.insertQuestion(question)
    }

    suspend fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteQuestion(question)
    }

    suspend fun insertAttempt(attempt: QuizAttempt) {
        quizAttemptDao.insertAttempt(attempt)
    }

    suspend fun getAttemptsCountSince(startOfDay: Long): Int {
        return quizAttemptDao.getAttemptsCountSince(startOfDay)
    }
}
