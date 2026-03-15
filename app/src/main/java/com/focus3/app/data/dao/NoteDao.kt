package com.focus3.app.data.dao

import androidx.room.*
import com.focus3.app.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * 📝 Next-Level Note DAO
 * Full-text search, analytics, and optimized queries
 */
@Dao
interface NoteDao {
    
    // ==================== BASIC QUERIES ====================
    
    // Get all notes, pinned first, then by updated date
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    // Get notes by category
    @Query("SELECT * FROM notes WHERE category = :category ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(category: String): Flow<List<Note>>
    
    // Search notes by title or content
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    // Get only pinned notes
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>
    
    // Get note by ID
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
    
    // ==================== CRUD ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
    
    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun togglePin(id: Int, updatedAt: String)
    
    // ==================== ANALYTICS ====================
    
    // Get note count
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
    
    // Get pinned note count
    @Query("SELECT COUNT(*) FROM notes WHERE isPinned = 1")
    suspend fun getPinnedNoteCount(): Int
    
    // Get archived note count
    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 1")
    suspend fun getArchivedNoteCount(): Int
    
    // Get categories with note counts
    @Query("SELECT category FROM notes GROUP BY category ORDER BY COUNT(*) DESC")
    fun getCategories(): Flow<List<String>>
    
    // Get note count per category
    @Query("SELECT category, COUNT(*) as count FROM notes GROUP BY category ORDER BY count DESC")
    suspend fun getCategoryStats(): List<CategoryStat>
    
    // Get archived notes
    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<Note>>
    
    // Search by label
    @Query("SELECT * FROM notes WHERE labels LIKE '%' || :label || '%' ORDER BY updatedAt DESC")
    fun getNotesByLabel(label: String): Flow<List<Note>>
    
    // Get notes with reminders
    @Query("SELECT * FROM notes WHERE reminderTime IS NOT NULL ORDER BY reminderTime ASC")
    fun getNotesWithReminders(): Flow<List<Note>>
    
    // Get recent notes (last N)
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentNotes(limit: Int): List<Note>
    
    // Get notes created between dates
    @Query("SELECT * FROM notes WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getNotesInDateRange(startDate: String, endDate: String): Flow<List<Note>>
    
    // Get total word count (approximate)
    @Query("SELECT SUM(LENGTH(content) - LENGTH(REPLACE(content, ' ', '')) + 1) FROM notes")
    suspend fun getTotalWordCount(): Int?
}

// Data class for category statistics
data class CategoryStat(
    val category: String,
    val count: Int
)
