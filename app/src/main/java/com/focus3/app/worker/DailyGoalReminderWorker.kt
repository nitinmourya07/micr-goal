package com.focus3.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focus3.app.data.repository.TaskRepository
import com.focus3.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 🔔 PRO DAILY GOAL REMINDER WORKER
 * Sends smart, context-aware notifications with motivational quotes
 * at the exact time the user has set
 */
@HiltWorker
class DailyGoalReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            sendProNotification()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun sendProNotification() {
        // Get current progress, goals and streak
        val todayTasks = taskRepository.getTasksForDateSync(taskRepository.getTodayString()).take(3)
        val completedCount = todayTasks.count { it.isCompleted && it.content.isNotBlank() }
        val streakData = taskRepository.getStreakDataSync()
        val currentStreak = streakData.currentStreak
        
        // Get actual goal titles for rich notification
        val goalTitles = todayTasks.mapNotNull { task ->
            if (task.content.isNotBlank()) task.content else null
        }
        
        val hour = java.time.LocalTime.now().hour
        
        // Skip notification if all goals are already completed
        if (completedCount >= 3) {
            // Optionally show celebration/motivation for completed day
            if (currentStreak > 0 && currentStreak % 7 == 0) {
                NotificationHelper.showMilestoneCelebration(applicationContext, currentStreak)
            }
            return
        }
        
        // 🔥 SMART NOTIFICATION BASED ON TIME AND PROGRESS 🔥
        when {
            // Morning - Fresh start motivation
            hour < 12 -> {
                NotificationHelper.showDailyGoalReminder(
                    context = applicationContext,
                    completedCount = completedCount,
                    streak = currentStreak,
                    goals = goalTitles
                )
            }
            // Afternoon - Progress check
            hour < 18 -> {
                NotificationHelper.showDailyGoalReminder(
                    context = applicationContext,
                    completedCount = completedCount,
                    streak = currentStreak,
                    goals = goalTitles
                )
            }
            // Evening (6-9 PM) - Urgency building
            hour < 21 -> {
                NotificationHelper.showDailyGoalReminder(
                    context = applicationContext,
                    completedCount = completedCount,
                    streak = currentStreak,
                    goals = goalTitles
                )
            }
            // DANGER ZONE (9 PM onwards) - CRITICAL ALERTS
            else -> {
                // Show urgent daily reminder with goals
                NotificationHelper.showDailyGoalReminder(
                    context = applicationContext,
                    completedCount = completedCount,
                    streak = currentStreak,
                    goals = goalTitles
                )
                
                // ALSO show streak danger alert if they have a streak to protect
                if (currentStreak > 0) {
                    NotificationHelper.showStreakDangerAlert(
                        context = applicationContext,
                        currentStreak = currentStreak
                    )
                }
            }
        }
        
        // Log for debugging
        android.util.Log.d("DailyGoalReminder", 
            "Sent notification: $completedCount/3 complete, streak: $currentStreak, hour: $hour")
    }

    companion object {
        const val WORK_NAME = "daily_goal_reminder"
    }
}

