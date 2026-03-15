package com.focus3.app.data.dao

import androidx.room.*
import com.focus3.app.data.model.CalendarNote
import kotlinx.coroutines.flow.Flow

/**
 * 📅 Next-Level Calendar Note DAO
 * Diary analytics and mood tracking
 */
@Dao
interface CalendarNoteDao {
    
    // ==================== BASIC QUERIES ====================
    
    @Query("SELECT * FROM calendar_notes WHERE date = :date LIMIT 1")
    fun getNoteForDate(date: String): Flow<CalendarNote?>
    
    @Query("SELECT * FROM calendar_notes WHERE date = :date LIMIT 1")
    suspend fun getNoteForDateSync(date: String): CalendarNote?
    
    @Query("SELECT * FROM calendar_notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<CalendarNote>>
    
    @Query("SELECT date FROM calendar_notes")
    fun getDatesWithNotes(): Flow<List<String>>
    
    // ==================== CRUD ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CalendarNote)
    
    @Update
    suspend fun updateNote(note: CalendarNote)
    
    @Delete
    suspend fun deleteNote(note: CalendarNote)
    
    @Query("DELETE FROM calendar_notes WHERE date = :date")
    suspend fun deleteNoteForDate(date: String)
    
    @Query("DELETE FROM calendar_notes")
    suspend fun deleteAll()
    
    // ==================== ANALYTICS ====================
    
    // Get total diary entries count
    @Query("SELECT COUNT(*) FROM calendar_notes")
    suspend fun getDiaryCount(): Int
    
    // Get entries in date range
    @Query("SELECT * FROM calendar_notes WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getNotesInDateRange(startDate: String, endDate: String): Flow<List<CalendarNote>>
    
    // Get mood distribution (count per mood emoji)
    @Query("SELECT mood, COUNT(*) as count FROM calendar_notes GROUP BY mood ORDER BY count DESC")
    suspend fun getMoodStats(): List<MoodStat>
    
    // Get most used mood
    @Query("SELECT mood FROM calendar_notes GROUP BY mood ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostUsedMood(): String?
    
    // Get recent diary entries
    @Query("SELECT * FROM calendar_notes ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int): List<CalendarNote>
    
    // Get entries for specific month
    @Query("SELECT * FROM calendar_notes WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getEntriesForMonth(monthPrefix: String): Flow<List<CalendarNote>>
    
    // Get writing streak (consecutive days with entries)
    @Query("SELECT COUNT(DISTINCT date) FROM calendar_notes WHERE date >= :startDate")
    suspend fun getEntryCountSince(startDate: String): Int
}

// Data class for mood statistics
data class MoodStat(
    val mood: String,
    val count: Int
)
