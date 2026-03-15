package com.focus3.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focus3.app.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MidnightResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Check if yesterday's tasks were all completed and update streak
            taskRepository.checkAndUpdateStreak()
            
            // Cleanup old tasks (older than 90 days)
            taskRepository.cleanupOldTasks()
            
            // Initialize today's tasks if needed
            taskRepository.initializeTodayTasks()
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        const val WORK_NAME = "midnight_reset_worker"
    }
}
