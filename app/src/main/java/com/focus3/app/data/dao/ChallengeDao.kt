package com.focus3.app.data.dao

import androidx.room.*
import com.focus3.app.data.model.Challenge
import kotlinx.coroutines.flow.Flow

/**
 * 🏆 Next-Level Challenge DAO
 * Analytics, statistics, and optimized queries
 */
@Dao
interface ChallengeDao {
    
    // ==================== BASIC CRUD ====================
    
    @Query("SELECT * FROM challenges ORDER BY id DESC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    @Query("SELECT * FROM challenges ORDER BY id DESC")
    suspend fun getAllChallengesSync(): List<Challenge>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: Int): Challenge?

    @Query("UPDATE challenges SET completedDays = completedDays + 1, currentStreak = :newStreak, lastCompletedDate = :today, graceDaysUsed = :graceUsed WHERE id = :id")
    suspend fun updateProgress(id: Int, newStreak: Int, today: String, graceUsed: Int)
    
    @Query("DELETE FROM challenges")
    suspend fun deleteAll()
    
    // ==================== ADVANCED ANALYTICS ====================
    
    // Get active challenges (not yet completed)
    @Query("SELECT * FROM challenges WHERE completedDays < targetDays ORDER BY completedDays DESC")
    fun getActiveChallenges(): Flow<List<Challenge>>
    
    // Get completed challenges
    @Query("SELECT * FROM challenges WHERE completedDays >= targetDays ORDER BY completedDays DESC")
    fun getCompletedChallenges(): Flow<List<Challenge>>
    
    // Get challenge count
    @Query("SELECT COUNT(*) FROM challenges")
    suspend fun getChallengeCount(): Int
    
    // Get active challenge count
    @Query("SELECT COUNT(*) FROM challenges WHERE completedDays < targetDays")
    suspend fun getActiveChallengeCount(): Int
    
    // Get completed challenge count
    @Query("SELECT COUNT(*) FROM challenges WHERE completedDays >= targetDays")
    suspend fun getCompletedChallengeCount(): Int
    
    // Get average completion rate across all challenges
    @Query("""
        SELECT AVG(CAST(completedDays AS FLOAT) / CAST(targetDays AS FLOAT) * 100) 
        FROM challenges 
        WHERE targetDays > 0
    """)
    suspend fun getAverageCompletionRate(): Float?
    
    // Get total completed days across all challenges
    @Query("SELECT SUM(completedDays) FROM challenges")
    suspend fun getTotalCompletedDays(): Int?
    
    // Get longest current streak among all challenges
    @Query("SELECT MAX(currentStreak) FROM challenges")
    suspend fun getLongestActiveStreak(): Int?
    
    // Get challenges due today (with pending reminder)
    @Query("SELECT * FROM challenges WHERE lastCompletedDate != :today AND completedDays < targetDays ORDER BY currentStreak DESC")
    suspend fun getChallengesDueToday(today: String): List<Challenge>
    
    // Get challenges by completion percentage range
    @Query("""
        SELECT * FROM challenges 
        WHERE (CAST(completedDays AS FLOAT) / CAST(targetDays AS FLOAT) * 100) >= :minPercent 
        AND (CAST(completedDays AS FLOAT) / CAST(targetDays AS FLOAT) * 100) <= :maxPercent
        ORDER BY completedDays DESC
    """)
    fun getChallengesByCompletionRange(minPercent: Float, maxPercent: Float): Flow<List<Challenge>>
}
