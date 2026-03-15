package com.focus3.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.Note
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data Export Helper
 * Exports app data to JSON and CSV formats for backup/sharing
 */
object DataExportHelper {
    
    private const val EXPORT_FOLDER = "exports"
    
    /**
     * Export all data to JSON format
     */
    fun exportToJson(
        context: Context,
        tasks: List<DailyTask>,
        challenges: List<Challenge>,
        notes: List<Note>,
        streak: Int
    ): Uri? {
        try {
            val json = JSONObject().apply {
                put("app_name", "Focus3")
                put("export_date", getCurrentDateTime())
                put("version", "1.0")
                
                // User Stats
                put("stats", JSONObject().apply {
                    put("current_streak", streak)
                    put("total_tasks", tasks.size)
                    put("total_challenges", challenges.size)
                    put("total_notes", notes.size)
                })
                
                // Tasks
                put("tasks", JSONArray().apply {
                    tasks.forEach { task ->
                        put(JSONObject().apply {
                            put("id", task.id)
                            put("content", task.content)
                            put("is_completed", task.isCompleted)
                            put("task_index", task.taskIndex)
                            put("date", task.date)
                        })
                    }
                })
                
                // Challenges
                put("challenges", JSONArray().apply {
                    challenges.forEach { challenge ->
                        put(JSONObject().apply {
                            put("id", challenge.id)
                            put("name", challenge.name)
                            put("icon", challenge.icon)
                            put("target_days", challenge.targetDays)
                            put("completed_days", challenge.completedDays)
                            put("current_streak", challenge.currentStreak)
                            put("start_date", challenge.startDate)
                            put("last_completed_date", challenge.lastCompletedDate)
                            put("reminder_time", challenge.reminderTime)
                        })
                    }
                })
                
                // Notes
                put("notes", JSONArray().apply {
                    notes.forEach { note ->
                        put(JSONObject().apply {
                            put("id", note.id)
                            put("title", note.title)
                            put("content", note.content)
                            put("category", note.category)
                            put("is_pinned", note.isPinned)
                            put("created_at", note.createdAt)
                            put("updated_at", note.updatedAt)
                        })
                    }
                })
            }
            
            val fileName = "focus3_backup_${getFileDateTime()}.json"
            return saveAndGetUri(context, fileName, json.toString(2))
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Export tasks and challenges to CSV format
     */
    fun exportToCsv(
        context: Context,
        tasks: List<DailyTask>,
        challenges: List<Challenge>
    ): Uri? {
        try {
            val csv = StringBuilder()
            
            // Header
            csv.appendLine("=== FOCUS3 DATA EXPORT ===")
            csv.appendLine("Export Date: ${getCurrentDateTime()}")
            csv.appendLine()
            
            // Tasks Section
            csv.appendLine("=== DAILY TASKS ===")
            csv.appendLine("Date,TaskIndex,Content,Completed")
            tasks.forEach { task ->
                csv.appendLine("${task.date},${task.taskIndex},\"${task.content.replace("\"", "\"\"")}\",${task.isCompleted}")
            }
            csv.appendLine()
            
            // Challenges Section
            csv.appendLine("=== CHALLENGES ===")
            csv.appendLine("Name,Icon,Target Days,Completed Days,Current Streak,Start Date,Reminder Time")
            challenges.forEach { challenge ->
                csv.appendLine("\"${challenge.name}\",${challenge.icon},${challenge.targetDays},${challenge.completedDays},${challenge.currentStreak},${challenge.startDate},${challenge.reminderTime ?: "N/A"}")
            }
            
            val fileName = "focus3_export_${getFileDateTime()}.csv"
            return saveAndGetUri(context, fileName, csv.toString())
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Share exported file via Intent
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String = "application/json") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Focus3 Data Export")
            putExtra(Intent.EXTRA_TEXT, "Here's my Focus3 backup data!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share backup via"))
    }
    
    /**
     * Save file and return content URI
     */
    private fun saveAndGetUri(context: Context, fileName: String, content: String): Uri? {
        try {
            // Create exports directory
            val exportDir = File(context.filesDir, EXPORT_FOLDER)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            // Delete old exports (keep last 5)
            cleanOldExports(exportDir)
            
            // Save file
            val file = File(exportDir, fileName)
            file.writeText(content)
            
            // Get content URI via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Clean old export files (keep last 5)
     */
    private fun cleanOldExports(directory: File) {
        try {
            val files = directory.listFiles()?.sortedByDescending { it.lastModified() }
            files?.drop(5)?.forEach { it.delete() }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    
    private fun getFileDateTime(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}
