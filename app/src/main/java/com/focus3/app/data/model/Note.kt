package com.focus3.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 📝 Note Entity
 * Rich notes with categories, labels, and checklists
 * Multiple indexes for fast filtering/sorting
 */
@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isPinned"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isArchived"]),
        Index(value = ["isPinned", "updatedAt"])  // Composite for sorted pinned queries
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val emoji: String = "📝",
    val color: String = "#1E3A5F",
    val category: String = "General",
    val isPinned: Boolean = false,
    
    // NEW: Google Notes-like features
    val labels: String = "", // Comma-separated labels
    val reminderTime: String? = null, // ISO DateTime format
    val isArchived: Boolean = false,
    val checklistItems: String = "", // JSON string of checklist items
    val imageUri: String? = null,
    
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val updatedAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
) {
    // Helper functions for labels
    fun getLabelsList(): List<String> = 
        if (labels.isBlank()) emptyList() else labels.split(",").map { it.trim() }
    
    fun hasLabel(label: String): Boolean = 
        getLabelsList().contains(label)
    
    // Helper for checklists
    fun getChecklistItemsList(): List<ChecklistItem> {
        if (checklistItems.isBlank()) return emptyList()
        return try {
            checklistItems.split("|||").mapNotNull { item ->
                val parts = item.split(":::")
                if (parts.size == 2) {
                    ChecklistItem(text = parts[0], isChecked = parts[1] == "true")
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun setChecklistItems(items: List<ChecklistItem>): String {
        return items.joinToString("|||") { "${it.text}:::${it.isChecked}" }
    }
    
    companion object {
        // Premium color palette
        val colorOptions = listOf(
            "#1E3A5F" to "Ocean Blue",
            "#2D4A3E" to "Forest Green",
            "#4A2D4A" to "Royal Purple",
            "#4A3D2D" to "Warm Brown",
            "#3D2D4A" to "Mystic Violet",
            "#2D3D4A" to "Steel Gray",
            "#4A2D3D" to "Rose Berry",
            "#3D4A2D" to "Olive Green",
            "#4A3D3D" to "Mocha",
            "#2D4A4A" to "Teal"
        )
        
        // Emoji options for notes
        val emojiOptions = listOf(
            "📝", "💡", "🎯", "⭐", "💎", "🚀", 
            "📚", "💪", "🔥", "✨", "💼", "🎨",
            "💻", "📱", "🏆", "❤️", "⚡", "🌟"
        )
        
        // Category options
        val categoryOptions = listOf(
            "General", "Personal", "Work", "Ideas", 
            "Goals", "Journal", "Learning", "Health"
        )
        
        // Default labels
        val defaultLabels = listOf(
            "Important", "ToDo", "Later", "Reference", "Project"
        )
    }
}

// Simple checklist item
data class ChecklistItem(
    val text: String,
    val isChecked: Boolean = false
)
