package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.QuizAppUI
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: QuizViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    // Re-arm the daily streak reminder chain (idempotent: unique work + REPLACE)
    if (viewModel.preferences.reminderEnabled) {
      com.example.notifications.ReminderScheduler.scheduleNext(this)
    }
    setContent {
      MyApplicationTheme {
        QuizAppUI(viewModel = viewModel)
      }
    }
  }
}
