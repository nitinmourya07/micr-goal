package com.focus3.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_data")
data class StreakData(
    @PrimaryKey
    val id: Int = 1, // Always 1, single row
    val currentStreak: Int = 0,
    val longestStreak: Int = 0, // All-time best streak
    val totalCompletedDays: Int = 0, // Total days with all goals completed
    val lastCompletedDate: String = "", // Format: yyyy-MM-dd
    val graceDaysUsed: Int = 0,
    val lastCheckedDate: String = "" // Format: yyyy-MM-dd, to prevent multiple checks per day
)

