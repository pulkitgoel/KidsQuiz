package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val starsEarned: Int,
    val answersJson: String = ""
)
