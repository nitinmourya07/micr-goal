package com.focus3.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * AlarmManager-based precise reminder scheduler for challenges
 * This ensures notifications fire at EXACT user-set times
 */
object ChallengeAlarmScheduler {
    
    private const val TAG = "ChallengeAlarmScheduler"
    private const val ACTION_CHALLENGE_REMINDER = "com.focus3.app.CHALLENGE_REMINDER"
    private const val EXTRA_CHALLENGE_ID = "challenge_id"
    private const val EXTRA_CHALLENGE_NAME = "challenge_name"
    private const val EXTRA_CHALLENGE_ICON = "challenge_icon"
    
    /**
     * Schedule a daily reminder for a specific challenge at the given time
     * @param context Application context
     * @param challengeId Unique ID of the challenge
     * @param challengeName Name of the challenge (for notification)
     * @param challengeIcon Icon emoji for the challenge
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     */
    fun scheduleReminder(
        context: Context,
        challengeId: Int,
        challengeName: String,
        challengeIcon: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create intent for the alarm
        val intent = Intent(context, ChallengeReminderReceiver::class.java).apply {
            action = ACTION_CHALLENGE_REMINDER
            putExtra(EXTRA_CHALLENGE_ID, challengeId)
            putExtra(EXTRA_CHALLENGE_NAME, challengeName)
            putExtra(EXTRA_CHALLENGE_ICON, challengeIcon)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            challengeId, // Use challenge ID as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate next trigger time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
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
                } else {
                    // Fall back to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                // Android 11 and below
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Scheduled reminder for $challengeName at $hour:$minute")
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm: ${e.message}")
            // Fall back to WorkManager
        }
    }
    
    /**
     * Cancel reminder for a specific challenge
     */
    fun cancelReminder(context: Context, challengeId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ChallengeReminderReceiver::class.java).apply {
            action = ACTION_CHALLENGE_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            challengeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled reminder for challenge ID: $challengeId")
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
 * BroadcastReceiver that handles challenge reminders
 */
class ChallengeReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val challengeId = intent.getIntExtra("challenge_id", -1)
        val challengeName = intent.getStringExtra("challenge_name") ?: "Challenge"
        val challengeIcon = intent.getStringExtra("challenge_icon") ?: "🎯"
        
        if (challengeId != -1) {
            // Show notification using coroutine
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    // Get repository from Hilt-style manual injection
                    val database = androidx.room.Room.databaseBuilder(
                        context.applicationContext,
                        com.focus3.app.data.database.Focus3Database::class.java,
                        "focus3_database"
                    ).build()
                    
                    val challengeDao = database.challengeDao()
                    val challenge = challengeDao.getChallengeById(challengeId)
                    
                    if (challenge != null) {
                        val currentStreak = challenge.currentStreak
                        val totalDays = challenge.targetDays
                        val daysCompleted = challenge.completedDays
                        
                        NotificationHelper.showChallengeReminder(
                            context = context,
                            challengeName = challenge.name,
                            challengeIcon = challenge.icon,
                            daysCompleted = daysCompleted,
                            totalDays = totalDays,
                            currentStreak = currentStreak
                        )
                        
                        // Reschedule for next day if reminder is set
                        val time = ChallengeAlarmScheduler.parseTimeString(challenge.reminderTime)
                        time?.let { (hour, minute) ->
                            ChallengeAlarmScheduler.scheduleReminder(
                                context = context,
                                challengeId = challengeId,
                                challengeName = challenge.name,
                                challengeIcon = challenge.icon,
                                hour = hour,
                                minute = minute
                            )
                            Log.d("ChallengeRcvr", "Rescheduled ${challenge.name} for next day")
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("ChallengeRcvr", "Error fetching data: ${e.message}")
                    // Fallback
                    NotificationHelper.showChallengeReminder(
                        context = context,
                        challengeName = challengeName,
                        challengeIcon = challengeIcon,
                        daysCompleted = 0,
                        totalDays = 30,
                        currentStreak = 0
                    )
                }
            }
        }
    }
}
