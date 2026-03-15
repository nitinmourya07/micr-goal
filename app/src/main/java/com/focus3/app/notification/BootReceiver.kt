package com.focus3.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.focus3.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules alarms after device reboot
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (BuildConfig.DEBUG) Log.d("BootReceiver", "Boot completed, rescheduling alarms")

            // 1. Reschedule Daily Goal Reminder
            rescheduleDailyGoal(context)

            // 2. Reschedule Challenge Reminders
            rescheduleChallenges(context)
        }
    }

    private fun rescheduleDailyGoal(context: Context) {
        val prefs = context.getSharedPreferences("focus3_preferences", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("daily_reminder_enabled", true)
        
        if (isEnabled) {
            val hour = prefs.getInt("daily_reminder_hour", 20)
            val minute = prefs.getInt("daily_reminder_minute", 0)
            
            DailyGoalAlarmScheduler.scheduleReminder(context, hour, minute)
            if (BuildConfig.DEBUG) Log.d("BootReceiver", "Rescheduled Daily Goal Reminder for $hour:$minute")
        }
    }

    private fun rescheduleChallenges(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            var database: com.focus3.app.data.database.Focus3Database? = null
            try {
                database = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    com.focus3.app.data.database.Focus3Database::class.java,
                    "focus3_database"
                )
                .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8)
                .build()
                
                val challenges = database.challengeDao().getAllChallengesSync()
                var count = 0
                
                challenges.forEach { challenge ->
                    if (!challenge.reminderTime.isNullOrBlank()) {
                        val time = ChallengeAlarmScheduler.parseTimeString(challenge.reminderTime)
                        time?.let { (hour, minute) ->
                            ChallengeAlarmScheduler.scheduleReminder(
                                context = context,
                                challengeId = challenge.id,
                                challengeName = challenge.name,
                                challengeIcon = challenge.icon,
                                hour = hour,
                                minute = minute
                            )
                            count++
                        }
                    }
                }
                if (BuildConfig.DEBUG) Log.d("BootReceiver", "Rescheduled $count challenge reminders")
                
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error rescheduling challenges: ${e.message}")
            } finally {
                // Always close the database to prevent resource leaks
                try { database?.close() } catch (_: Exception) {}
            }
        }
    }
}
