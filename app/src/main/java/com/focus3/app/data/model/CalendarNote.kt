package com.focus3.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 📅 Calendar Note Entity
 * Diary entries and reflections for specific dates
 * Unique date index ensures one entry per day
 */
@Entity(
    tableName = "calendar_notes",
    indices = [
        Index(value = ["date"], unique = true)
    ]
)
data class CalendarNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // Format: yyyy-MM-dd
    val content: String,
    val mood: String = "😊", // Emoji mood indicator
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
