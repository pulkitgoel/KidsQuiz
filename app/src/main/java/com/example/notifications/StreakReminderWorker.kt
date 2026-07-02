package com.example.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Daily check around the reminder hour: if today's quiz hasn't been done and
 * reminders are enabled, nudge with a notification. Always re-arms the next
 * run — the chain must survive a skipped or failed day.
 */
class StreakReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val prefs = PreferencesManager(applicationContext)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val notificationsAllowed =
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED) &&
                    NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()

            if (prefs.reminderEnabled && prefs.lastQuizDate != today && notificationsAllowed) {
                NotificationHelper.showStreakReminder(applicationContext, prefs.streakCount)
            }
        } finally {
            // Re-arm tomorrow's check no matter what happened above
            ReminderScheduler.scheduleNext(applicationContext)
        }
        return Result.success()
    }
}
