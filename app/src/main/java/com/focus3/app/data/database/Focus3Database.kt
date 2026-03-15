package com.focus3.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focus3.app.data.dao.CalendarNoteDao
import com.focus3.app.data.dao.ChallengeDao
import com.focus3.app.data.dao.NoteDao
import com.focus3.app.data.dao.TaskDao
import com.focus3.app.data.model.CalendarNote
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.Note
import com.focus3.app.data.model.StreakData

/**
 * 🗄️ Focus3 Production Database
 * Version 10: Added indexes for performance
 * 
 * Tables:
 * - daily_tasks: Daily 3-goal system
 * - streak_data: Streak tracking (single row)
 * - challenges: Long-term goals
 * - notes: Rich notes with labels
 * - calendar_notes: Date-based diary
 * 
 * Features:
 * - Full-text search indexes
 * - Analytics-ready queries
 * - Schema export for debugging
 */
@Database(
    entities = [
        DailyTask::class, 
        StreakData::class, 
        Challenge::class, 
        Note::class, 
        CalendarNote::class
    ],
    version = 10,
    exportSchema = true
)
abstract class Focus3Database : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun noteDao(): NoteDao
    abstract fun calendarNoteDao(): CalendarNoteDao
}

