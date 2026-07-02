package com.example.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Self-rechaining daily reminder: a OneTimeWorkRequest aimed at the next
 * 18:30 local time. The worker re-arms itself; MainActivity re-arms on every
 * launch (unique name + REPLACE keeps this idempotent). PeriodicWorkRequest
 * is deliberately avoided — it can't target a time of day.
 */
object ReminderScheduler {
    private const val WORK_NAME = "streak_reminder"
    private const val REMINDER_HOUR = 18
    private const val REMINDER_MINUTE = 30

    fun scheduleNext(context: Context) {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMillis = next.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<StreakReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * After a quiz completes, re-aim at the next 18:30 — today's pending
     * reminder becomes tomorrow's check (the worker would stay silent today
     * anyway, but re-arming keeps the timing crisp).
     */
    fun onQuizCompleted(context: Context) = scheduleNext(context)

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
