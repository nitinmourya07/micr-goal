package com.focus3.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.repository.TaskRepository
import com.focus3.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ChallengeReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "challenge_daily_reminder"
    }

    override suspend fun doWork(): Result {
        return try {
            val challenges = taskRepository.getAllChallenges().first()
            
            // Send notifications for active (incomplete) challenges
            val activeChallenges = challenges.filter { it.completedDays < it.targetDays }
            
            if (activeChallenges.isNotEmpty()) {
                sendSmartChallengeNotifications(activeChallenges)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendSmartChallengeNotifications(challenges: List<Challenge>) {
        // Show notification for the most important challenge (lowest completion %)
        val priorityChallenge = challenges.minByOrNull { 
            it.completedDays.toFloat() / it.targetDays.toFloat() 
        } ?: return
        
        NotificationHelper.showChallengeReminder(
            context = applicationContext,
            challengeName = priorityChallenge.name,
            challengeIcon = priorityChallenge.icon,
            daysCompleted = priorityChallenge.completedDays,
            totalDays = priorityChallenge.targetDays,
            currentStreak = priorityChallenge.currentStreak
        )
        
        // Check for milestones
        challenges.forEach { challenge ->
            checkForMilestones(challenge)
        }
    }
    
    private fun checkForMilestones(challenge: Challenge) {
        val streak = challenge.currentStreak
        val milestones = listOf(7, 14, 21, 30, 50, 75, 100, 365)
        
        if (streak in milestones) {
            NotificationHelper.showMilestoneCelebration(
                context = applicationContext,
                days = streak
            )
        }
    }
}
