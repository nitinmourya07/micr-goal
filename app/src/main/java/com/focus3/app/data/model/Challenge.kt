package com.focus3.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 🏆 Challenge Entity
 * Long-term goals with progress tracking
 * Indexed for fast date-based queries
 */
@Entity(
    tableName = "challenges",
    indices = [
        Index(value = ["startDate"]),
        Index(value = ["lastCompletedDate"]),
        Index(value = ["completedDays"])
    ]
)
data class Challenge(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val targetDays: Int,
    val icon: String = "\uD83D\uDCDA",
    val startDate: String = LocalDate.now().toString(),
    val completedDays: Int = 0,
    val currentStreak: Int = 0,
    val lastCompletedDate: String = "", // Format: yyyy-MM-dd
    val reminderTime: String? = null, // Format: HH:mm
    val graceDaysUsed: Int = 0
)

