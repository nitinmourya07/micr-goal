package com.focus3.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager-based EXACT time scheduler for Daily Goal Reminders
 * This ensures notifications fire at the EXACT user-set time
 */
object DailyGoalAlarmScheduler {
    
    private const val TAG = "DailyGoalAlarmScheduler"
    private const val ACTION_DAILY_GOAL_REMINDER = "com.focus3.app.DAILY_GOAL_REMINDER"
    private const val REQUEST_CODE = 1001 // Fixed ID for daily goal reminder
    
    /**
     * Schedule a daily reminder at the given time
     * @param context Application context
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     */
    fun scheduleReminder(
        context: Context,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create intent for the alarm
        val intent = Intent(context, DailyGoalReminderReceiver::class.java).apply {
            action = ACTION_DAILY_GOAL_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate next trigger time
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+: Check if we can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled EXACT alarm at $hour:$minute")
                } else {
                    // Fall back to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled INEXACT alarm at $hour:$minute (no exact alarm permission)")
                }
            } else {
                // Android 11 and below
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled EXACT alarm at $hour:$minute")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm: ${e.message}")
        }
    }
    
    /**
     * Cancel the daily goal reminder
     */
    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, DailyGoalReminderReceiver::class.java).apply {
            action = ACTION_DAILY_GOAL_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled daily goal reminder")
    }
    
    /**
     * Parse time string (HH:mm) to hour and minute
     */
    fun parseTimeString(timeString: String?): Pair<Int, Int>? {
        if (timeString.isNullOrBlank()) return null
        
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute in 0..59) {
                    Pair(hour, minute)
                } else null
            } else null
        } catch (e: NumberFormatException) {
            null
        }
    }
}

/**
 * BroadcastReceiver that handles daily goal reminder alarms
 */
class DailyGoalReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DailyGoalReminderRcvr"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Daily goal reminder alarm received!")
        
        // Show notification using coroutine (since we need database access)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                // Get repository from Hilt
                val repository = getRepository(context)
                val today = repository.getTodayString()
                val todayTasks = repository.getTasksForDateSync(today).take(3)
                val completedCount = todayTasks.count { it.isCompleted && it.content.isNotBlank() }
                val streakData = repository.getStreakDataSync()
                val streak = streakData.currentStreak
                
                val goalTitles = todayTasks.mapNotNull { task ->
                    if (task.content.isNotBlank()) task.content else null
                }
                
                // Only show notification if goals are not completed
                if (completedCount < 3) {
                    NotificationHelper.showDailyGoalReminder(
                        context = context,
                        completedCount = completedCount,
                        streak = streak,
                        goals = goalTitles
                    )
                    Log.d(TAG, "Notification shown: $completedCount/3 complete, streak: $streak")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing notification: ${e.message}")
                // Fallback: show generic notification
                NotificationHelper.showDailyGoalReminder(
                    context = context,
                    completedCount = 0,
                    streak = 0,
                    goals = emptyList()
                )
            }
            
            // Reschedule for next day
            rescheduleForNextDay(context)
        }
    }
    
    private fun rescheduleForNextDay(context: Context) {
        val prefs = context.getSharedPreferences("focus3_preferences", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("daily_reminder_enabled", true)
        
        if (isEnabled) {
            val hour = prefs.getInt("daily_reminder_hour", 20)
            val minute = prefs.getInt("daily_reminder_minute", 0)
            
            DailyGoalAlarmScheduler.scheduleReminder(context, hour, minute)
            Log.d(TAG, "Rescheduled for next day at $hour:$minute")
        }
    }
    
    private fun getRepository(context: Context): com.focus3.app.data.repository.TaskRepository {
        // Access the database directly since we're in a BroadcastReceiver
        val database = androidx.room.Room.databaseBuilder(
            context.applicationContext,
            com.focus3.app.data.database.Focus3Database::class.java,
            "focus3_database"
        ).build()
        
        return com.focus3.app.data.repository.TaskRepository(
            database.taskDao(),
            database.challengeDao(),
            context.applicationContext
        )
    }
}
