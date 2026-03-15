package com.focus3.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    
    private const val DAILY_REMINDER_WORK_NAME = "daily_goal_reminder"
    
    fun scheduleMidnightReset(context: Context) {
        // Calculate delay until midnight
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT)
        val delayMinutes = Duration.between(now, midnight).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<MidnightResetWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MidnightResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    fun scheduleDailyGoalReminder(context: Context) {
        // Schedule reminder at 8 PM (20:00)
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(20, 0) // 8 PM
        
        var targetTime = now.toLocalDate().atTime(reminderTime)
        if (now.isAfter(targetTime)) {
            // If 8 PM has passed today, schedule for tomorrow
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMinutes = Duration.between(now, targetTime).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<DailyGoalReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    fun scheduleChallengeReminder(context: Context) {
        // Schedule challenge reminder at 7 PM (19:00) - 1 hour before daily goals
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(19, 0) // 7 PM
        
        var targetTime = now.toLocalDate().atTime(reminderTime)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMinutes = Duration.between(now, targetTime).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<ChallengeReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ChallengeReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    // Custom time scheduling functions
    fun scheduleDailyGoalReminderWithTime(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(hour, minute)
        
        var targetTime = now.toLocalDate().atTime(reminderTime)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMinutes = Duration.between(now, targetTime).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<DailyGoalReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Replace to update with new time
            workRequest
        )
    }
    
    fun scheduleChallengeReminderWithTime(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(hour, minute)
        
        var targetTime = now.toLocalDate().atTime(reminderTime)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMinutes = Duration.between(now, targetTime).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<ChallengeReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ChallengeReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Replace to update with new time
            workRequest
        )
    }
    
    /**
     * 🚨 AGGRESSIVE STREAK PROTECTION SYSTEM 🚨
     * Schedules multiple reminders to ensure no day is missed
     */
    fun scheduleAggressiveStreakProtection(context: Context) {
        // Morning reminder at 9 AM
        scheduleReminderAtTime(context, "morning_reminder", 9, 0)
        
        // Afternoon reminder at 2 PM
        scheduleReminderAtTime(context, "afternoon_reminder", 14, 0)
        
        // Evening reminder at 6 PM
        scheduleReminderAtTime(context, "evening_reminder", 18, 0)
        
        // Night reminder at 8 PM
        scheduleReminderAtTime(context, "night_reminder", 20, 0)
        
        // DANGER ZONE - 9 PM Critical Alert
        scheduleReminderAtTime(context, "danger_alert_1", 21, 0)
        
        // FINAL WARNING - 10 PM Urgent Alert
        scheduleReminderAtTime(context, "danger_alert_2", 22, 0)
        
        // LAST CHANCE - 11 PM Ultra Urgent
        scheduleReminderAtTime(context, "last_chance_alert", 23, 0)
    }
    
    private fun scheduleReminderAtTime(context: Context, workName: String, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(hour, minute)
        
        var targetTime = now.toLocalDate().atTime(reminderTime)
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMinutes = Duration.between(now, targetTime).toMinutes()
        
        val workRequest = PeriodicWorkRequestBuilder<DailyGoalReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    // Cancel functions
    fun cancelDailyGoalReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }
    
    fun cancelChallengeReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ChallengeReminderWorker.WORK_NAME)
    }
    
    fun cancelAllAggressiveReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("morning_reminder")
        workManager.cancelUniqueWork("afternoon_reminder")
        workManager.cancelUniqueWork("evening_reminder")
        workManager.cancelUniqueWork("night_reminder")
        workManager.cancelUniqueWork("danger_alert_1")
        workManager.cancelUniqueWork("danger_alert_2")
        workManager.cancelUniqueWork("last_chance_alert")
    }
}
