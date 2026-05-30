package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // "Math", "English", "General Knowledge", "Science"
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int, // 0 to 3
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)
