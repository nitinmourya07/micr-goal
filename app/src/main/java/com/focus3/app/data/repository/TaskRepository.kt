package com.focus3.app.data.repository

import com.focus3.app.data.dao.ChallengeDao
import com.focus3.app.data.dao.TaskDao
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.StreakData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import androidx.work.*
import com.focus3.app.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.util.concurrent.TimeUnit
import java.time.LocalTime
import java.time.Duration
import android.util.Log

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val challengeDao: ChallengeDao,
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    fun getTodayString(): String = LocalDate.now().format(dateFormatter)
    
    fun getTasksForToday(): Flow<List<DailyTask>> {
        return taskDao.getTasksForDate(getTodayString())
    }
    
    suspend fun getTasksForDateSync(date: String): List<DailyTask> {
        return taskDao.getTasksForDateSync(date)
    }
    
    suspend fun initializeTodayTasks() {
        val today = getTodayString()
        val existingTasks = taskDao.getTasksForDateSync(today)
        
        if (existingTasks.isEmpty()) {
            // Create 3 empty tasks for today
            repeat(3) { index ->
                taskDao.insertTask(
                    DailyTask(
                        date = today,
                        taskIndex = index,
                        content = "",
                        isCompleted = false
                    )
                )
            }
        }
    }
    
    suspend fun updateTask(task: DailyTask) {
        taskDao.updateTask(task)
    }

    suspend fun insertTask(task: DailyTask) {
        taskDao.insertTask(task)
    }
    
    suspend fun updateTaskContent(taskId: Int, content: String) {
        val tasks = taskDao.getTasksForDateSync(getTodayString())
        tasks.find { it.id == taskId }?.let { task ->
            taskDao.updateTask(task.copy(content = content))
        }
    }
    
    suspend fun toggleTaskCompletion(taskId: Int) {
        val tasks = taskDao.getTasksForDateSync(getTodayString())
        tasks.find { it.id == taskId }?.let { task ->
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }
    
    // Streak Management
    fun getStreakData(): Flow<StreakData?> {
        return taskDao.getStreakData()
    }
    
    suspend fun getStreakDataSync(): StreakData {
        return taskDao.getStreakDataSync() ?: StreakData()
    }
    
    suspend fun checkAndUpdateStreak() {
        val today = getTodayString()
        val currentStreakData = taskDao.getStreakDataSync() ?: StreakData()
        
        // Exit early if already checked today
        if (currentStreakData.lastCheckedDate == today) return
        
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        val yesterdayTasks = taskDao.getTasksForDateSync(yesterday)
        
        // If no tasks exist for yesterday (fresh install, first day, or never used that day),
        // just mark as checked without penalizing — user wasn't active yet
        if (yesterdayTasks.isEmpty()) {
            taskDao.insertOrUpdateStreak(currentStreakData.copy(lastCheckedDate = today))
            return
        }
        
        // Check if all 3 tasks were completed yesterday
        val allCompleted = yesterdayTasks.size >= 3 && yesterdayTasks.take(3).all { it.isCompleted }
        
        val newStreak = if (allCompleted) {
            // Yesterday was completed, update lastCheckedDate
            currentStreakData.copy(lastCheckedDate = today)
        } else {
            // Check for grace chances
            if (currentStreakData.graceDaysUsed < 3 && currentStreakData.currentStreak > 0) {
                // Use a grace day
                currentStreakData.copy(
                    graceDaysUsed = currentStreakData.graceDaysUsed + 1,
                    lastCheckedDate = today
                )
            } else {
                // Reset streak if out of grace chances or streak was already 0
                currentStreakData.copy(
                    currentStreak = 0,
                    lastCompletedDate = "",
                    graceDaysUsed = 0,
                    lastCheckedDate = today
                )
            }
        }
        
        taskDao.insertOrUpdateStreak(newStreak)
    }
    
    suspend fun updateStreakOnCompletion() {
        val today = getTodayString()
        val todayTasks = taskDao.getTasksForDateSync(today)
        val currentStreakData = taskDao.getStreakDataSync() ?: StreakData()
        
        // Check if all 3 tasks are completed today (count completed, not row count)
        val completedCount = todayTasks.take(3).count { it.isCompleted }
        

        if (completedCount == 3 && currentStreakData.lastCompletedDate != today) {
            // Increment streak when all tasks completed for the first time today
            val newStreak = currentStreakData.currentStreak + 1
            val newLongestStreak = maxOf(currentStreakData.longestStreak, newStreak)
            val newTotalDays = currentStreakData.totalCompletedDays + 1
            
            taskDao.insertOrUpdateStreak(
                currentStreakData.copy(
                    currentStreak = newStreak,
                    longestStreak = newLongestStreak,
                    totalCompletedDays = newTotalDays,
                    lastCompletedDate = today,
                    lastCheckedDate = today, // Mark as checked today
                    graceDaysUsed = 0 // Reset grace days on successful completion
                )
            )
        }
    }
    
    // NEW: Handle task uncompletion - decrement streak if needed
    suspend fun handleTaskUncompletion() {
        val today = getTodayString()
        val todayTasks = taskDao.getTasksForDateSync(today)
        val currentStreakData = taskDao.getStreakDataSync() ?: StreakData()
        
        // If today was the last completed date and now not all tasks are complete
        val completedCount = todayTasks.take(3).count { it.isCompleted }
        

        if (completedCount < 3 && currentStreakData.lastCompletedDate == today) {
            // Revert today's streak increment
            val revertedStreak = (currentStreakData.currentStreak - 1).coerceAtLeast(0)
            // Recalculate longestStreak: it may have been inflated by today's now-reverted streak
            val correctedLongest = maxOf(currentStreakData.longestStreak, revertedStreak)
            
            taskDao.insertOrUpdateStreak(
                currentStreakData.copy(
                    currentStreak = revertedStreak,
                    longestStreak = correctedLongest,
                    totalCompletedDays = (currentStreakData.totalCompletedDays - 1).coerceAtLeast(0),
                    lastCompletedDate = "", // Clear so it can be re-earned
                    lastCheckedDate = today // Still checked today
                )
            )
        }
    }
    
    suspend fun cleanupOldTasks() {
        val ninetyDaysAgo = LocalDate.now().minusDays(90).format(dateFormatter)
        taskDao.deleteTasksBeforeDate(ninetyDaysAgo)
    }
    
    fun getCompletionHistory(days: Int = 7): Flow<Map<String, Int>> {
        val endDate = LocalDate.now().format(dateFormatter)
        val startDate = LocalDate.now().minusDays(days.toLong()).format(dateFormatter)
        
        return taskDao.getTasksInDateRange(startDate, endDate).map { tasks ->
            tasks.groupBy { it.date }
                .mapValues { (_, dayTasks) -> 
                    // Only count first 3 tasks, cap at 3 max
                    dayTasks.take(3).count { it.isCompleted }.coerceAtMost(3)
                }
        }
    }
    
    // Get all historical tasks for detailed view
    fun getAllHistoricalTasks(days: Int = 30): Flow<List<DailyTask>> {
        val endDate = LocalDate.now().format(dateFormatter)
        val startDate = LocalDate.now().minusDays(days.toLong()).format(dateFormatter)
        return taskDao.getTasksInDateRange(startDate, endDate)
    }

    // Challenge Management
    fun getAllChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllChallenges()
    }

    suspend fun insertChallenge(challenge: Challenge) {
        challengeDao.insertChallenge(challenge)
        if (!challenge.reminderTime.isNullOrBlank()) {
            scheduleChallengeReminder(challenge)
        }
    }

    /**
     * 🔔 Schedule per-challenge reminder at user-specified time
     * Fires exactly at the time user set - PERFECT timing
     */
    private fun scheduleChallengeReminder(challenge: Challenge) {
        val reminderTime = challenge.reminderTime ?: return
        
        try {
            val timeParts = reminderTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            
            com.focus3.app.notification.ChallengeAlarmScheduler.scheduleReminder(
                context = context,
                challengeId = challenge.id,
                challengeName = challenge.name,
                challengeIcon = challenge.icon,
                hour = hour,
                minute = minute
            )
            
            android.util.Log.d("ChallengeReminder", "Scheduled Alarm for '${challenge.name}' at $reminderTime")
        } catch (e: Exception) {
            android.util.Log.e("ChallengeReminder", "Failed to schedule alarm: ${e.message}")
        }
    }
    
    /**
     * Cancel reminder for a specific challenge
     */
    private fun cancelChallengeReminder(challengeId: Int) {
        com.focus3.app.notification.ChallengeAlarmScheduler.cancelReminder(context, challengeId)
        android.util.Log.d("ChallengeReminder", "Cancelled alarm for challenge $challengeId")
    }

    suspend fun deleteChallenge(challenge: Challenge) {
        // Cancel the reminder first
        cancelChallengeReminder(challenge.id)
        // Then delete from database
        challengeDao.deleteChallenge(challenge)
    }
    
    suspend fun updateChallenge(challenge: Challenge) {
        challengeDao.updateChallenge(challenge)
        // Reschedule reminder with updated data
        if (!challenge.reminderTime.isNullOrBlank()) {
            scheduleChallengeReminder(challenge)
        } else {
            // If reminder time cleared, cancel the reminder
            cancelChallengeReminder(challenge.id)
        }
    }

    /**
     * Updates challenge progress for today.
     * Returns true if progress was actually updated (not already completed today).
     * Returns false if already completed today - prevents showing quote repeatedly.
     */
    suspend fun updateChallengeProgress(challengeId: Int): Boolean {
        val challenge = challengeDao.getChallengeById(challengeId) ?: return false
        val today = getTodayString()

        // Smart check: If already completed today, return false
        if (challenge.lastCompletedDate == today) {
            return false // Already completed today - don't show quote again
        }
        
        val lastDate = if (challenge.lastCompletedDate.isBlank()) null else LocalDate.parse(challenge.lastCompletedDate)
        val daysBetween = if (lastDate != null) ChronoUnit.DAYS.between(lastDate, LocalDate.now()) else 0
        
        val (newStreak, newGraceUsed) = when {
            challenge.lastCompletedDate.isBlank() -> 1 to 0
            daysBetween <= 1 -> (challenge.currentStreak + 1) to challenge.graceDaysUsed
            daysBetween <= (3 - challenge.graceDaysUsed + 1) -> {
                // Gap detected but within grace limits
                val missedDays = (daysBetween - 1).toInt()
                challenge.currentStreak + 1 to (challenge.graceDaysUsed + missedDays)
            }
            else -> 1 to 0 // Reset
        }

        challengeDao.updateProgress(challengeId, newStreak, today, newGraceUsed)
        return true // Progress was updated
    }
    
    // Delete all tasks (for clearing data)
    suspend fun deleteAllTasks() {
        taskDao.deleteAll()
    }
    
    // Delete all challenges
    suspend fun deleteAllChallenges() {
        challengeDao.deleteAll()
    }
    
    // Save streak data
    suspend fun saveStreakData(streakData: StreakData) {
        taskDao.insertOrUpdateStreak(streakData)
    }
}
