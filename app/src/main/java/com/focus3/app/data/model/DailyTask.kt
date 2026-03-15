package com.focus3.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎯 Daily Task Entity
 * Represents a single daily goal (3 per day)
 * Index on date for fast queries
 */
@Entity(
    tableName = "daily_tasks",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "taskIndex"], unique = true)
    ]
)
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // Format: yyyy-MM-dd
    val taskIndex: Int, // 0, 1, or 2
    val content: String = "",
    val isCompleted: Boolean = false
)
