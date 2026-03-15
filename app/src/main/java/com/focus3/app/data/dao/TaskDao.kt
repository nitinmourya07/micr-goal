package com.focus3.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.StreakData
import kotlinx.coroutines.flow.Flow

/**
 * 🎯 Next-Level Task DAO
 * High-performance queries with indexes
 * Includes analytics and statistics
 */
@Dao
interface TaskDao {
    
    // ==================== DAILY TASKS ====================
    
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY taskIndex ASC")
    fun getTasksForDate(date: String): Flow<List<DailyTask>>
    
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY taskIndex ASC")
    suspend fun getTasksForDateSync(date: String): List<DailyTask>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask)
    
    @Update
    suspend fun updateTask(task: DailyTask)
    
    @Query("SELECT * FROM daily_tasks WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, taskIndex ASC")
    fun getTasksInDateRange(startDate: String, endDate: String): Flow<List<DailyTask>>
    
    @Query("DELETE FROM daily_tasks WHERE date < :date")
    suspend fun deleteTasksBeforeDate(date: String)
    
    @Query("DELETE FROM daily_tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)
    
    // ==================== ADVANCED ANALYTICS ====================
    
    // Get total completed tasks count
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE isCompleted = 1")
    suspend fun getTotalCompletedTasks(): Int
    
    // Get completion rate for date range (returns completed/total ratio)
    @Query("""
        SELECT CAST(SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) AS FLOAT) / 
               CAST(COUNT(*) AS FLOAT) * 100 
        FROM daily_tasks 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getCompletionRateForRange(startDate: String, endDate: String): Float?
    
    // Get fully completed days count (all 3 tasks done)
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT date FROM daily_tasks 
            WHERE isCompleted = 1 
            GROUP BY date 
            HAVING COUNT(*) = 3
        )
    """)
    suspend fun getFullyCompletedDaysCount(): Int
    
    // Get productivity by day of week (0=Sunday, 6=Saturday)
    @Query("""
        SELECT CAST(SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) AS FLOAT) / 
               CAST(COUNT(*) AS FLOAT) * 100 
        FROM daily_tasks 
        WHERE date >= :startDate AND date <= :endDate
        AND strftime('%w', date) = :dayOfWeek
    """)
    suspend fun getCompletionRateByDayOfWeek(startDate: String, endDate: String, dayOfWeek: String): Float?
    
    // Get most productive hour (requires timestamp, placeholder for future)
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE date >= :startDate")
    suspend fun getTaskCountSince(startDate: String): Int
    
    // Get distinct dates with tasks
    @Query("SELECT DISTINCT date FROM daily_tasks ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentDatesWithTasks(limit: Int): List<String>
    
    // ==================== STREAK DATA ====================
    
    @Query("SELECT * FROM streak_data WHERE id = 1")
    fun getStreakData(): Flow<StreakData?>
    
    @Query("SELECT * FROM streak_data WHERE id = 1")
    suspend fun getStreakDataSync(): StreakData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streakData: StreakData)
    
    // ==================== CLEANUP ====================
    
    @Query("DELETE FROM daily_tasks")
    suspend fun deleteAll()
    
    // Get database statistics
    @Query("SELECT COUNT(*) FROM daily_tasks")
    suspend fun getTotalTaskCount(): Int
}
