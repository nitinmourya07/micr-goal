package com.focus3.app.data.util

import com.focus3.app.data.dao.ChallengeDao
import com.focus3.app.data.dao.NoteDao
import com.focus3.app.data.dao.TaskDao
import com.focus3.app.data.dao.CalendarNoteDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📊 Database Statistics Helper
 * Centralized access to all database analytics
 */
@Singleton
class DatabaseStats @Inject constructor(
    private val taskDao: TaskDao,
    private val challengeDao: ChallengeDao,
    private val noteDao: NoteDao,
    private val calendarNoteDao: CalendarNoteDao
) {
    
    // ==================== TASK STATS ====================
    
    suspend fun getTotalTasks(): Int = taskDao.getTotalTaskCount()
    
    suspend fun getCompletedTasks(): Int = taskDao.getTotalCompletedTasks()
    
    suspend fun getCompletionRate(startDate: String, endDate: String): Float = 
        taskDao.getCompletionRateForRange(startDate, endDate) ?: 0f
    
    suspend fun getFullyCompletedDays(): Int = taskDao.getFullyCompletedDaysCount()
    
    // ==================== CHALLENGE STATS ====================
    
    suspend fun getTotalChallenges(): Int = challengeDao.getChallengeCount()
    
    suspend fun getActiveChallenges(): Int = challengeDao.getActiveChallengeCount()
    
    suspend fun getCompletedChallenges(): Int = challengeDao.getCompletedChallengeCount()
    
    suspend fun getAverageChallengeProgress(): Float = 
        challengeDao.getAverageCompletionRate() ?: 0f
    
    suspend fun getLongestActiveStreak(): Int = 
        challengeDao.getLongestActiveStreak() ?: 0
    
    // ==================== NOTE STATS ====================
    
    suspend fun getTotalNotes(): Int = noteDao.getNoteCount()
    
    suspend fun getPinnedNotes(): Int = noteDao.getPinnedNoteCount()
    
    suspend fun getArchivedNotes(): Int = noteDao.getArchivedNoteCount()
    
    suspend fun getTotalWordCount(): Int = noteDao.getTotalWordCount() ?: 0
    
    // ==================== AGGREGATED STATS ====================
    
    data class AppStats(
        val totalTasks: Int,
        val completedTasks: Int,
        val taskCompletionPercent: Float,
        val fullyCompletedDays: Int,
        val totalChallenges: Int,
        val activeChallenges: Int,
        val completedChallenges: Int,
        val totalNotes: Int,
        val totalWords: Int
    )
    
    suspend fun getAppStats(startDate: String, endDate: String): AppStats {
        return AppStats(
            totalTasks = getTotalTasks(),
            completedTasks = getCompletedTasks(),
            taskCompletionPercent = getCompletionRate(startDate, endDate),
            fullyCompletedDays = getFullyCompletedDays(),
            totalChallenges = getTotalChallenges(),
            activeChallenges = getActiveChallenges(),
            completedChallenges = getCompletedChallenges(),
            totalNotes = getTotalNotes(),
            totalWords = getTotalWordCount()
        )
    }
}
