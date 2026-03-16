package com.focus3.app.data.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.focus3.app.data.dao.CalendarNoteDao
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.StreakData
import com.focus3.app.data.repository.TaskRepository
import org.json.JSONObject
import java.time.LocalDate
import java.util.Locale

/**
 * 📦 Data Import Helper
 * Extracted from MainViewModel to keep the ViewModel lean.
 * Handles JSON backup parsing with multi-format compatibility.
 */
class DataImportHelper(
    private val taskRepository: TaskRepository,
    private val calendarNoteDao: CalendarNoteDao
) {

    /**
     * Import data from a JSON backup file URI.
     * Supports multiple key naming conventions (snake_case, camelCase, etc.)
     */
    suspend fun importFromUri(context: Context, uri: Uri) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return

            val jsonObject = JSONObject(jsonString)

            importTasks(jsonObject)
            importChallenges(jsonObject)
            importStreak(jsonObject)

        } catch (e: Exception) {
            Log.e(TAG, "Error importing data: ${e.message}")
        }
    }

    // ─── Tasks ───────────────────────────────────────

    private suspend fun importTasks(jsonObject: JSONObject) {
        if (!jsonObject.has("tasks")) return

        val tasksArray = jsonObject.getJSONArray("tasks")
        for (i in 0 until tasksArray.length()) {
            val taskObj = tasksArray.getJSONObject(i)
            val rawTaskIndex = taskObj.optIntCompat("task_index", "taskIndex", "taskNumber", default = i)
            val taskIndex = if (
                taskObj.has("taskNumber")
                && !taskObj.has("task_index")
                && !taskObj.has("taskIndex")
            ) {
                (rawTaskIndex - 1).coerceAtLeast(0)
            } else {
                rawTaskIndex.coerceAtLeast(0)
            }

            val task = DailyTask(
                id = 0,
                date = taskObj.optStringCompat("date", default = LocalDate.now().toString()),
                taskIndex = taskIndex,
                content = taskObj.optStringCompat("content"),
                isCompleted = taskObj.optBooleanCompat("is_completed", "isCompleted", default = false)
            )

            if (task.content.isNotBlank()) {
                taskRepository.insertTask(task)
            }
        }
    }

    // ─── Challenges ──────────────────────────────────

    private suspend fun importChallenges(jsonObject: JSONObject) {
        if (!jsonObject.has("challenges")) return

        val challengesArray = jsonObject.getJSONArray("challenges")
        for (i in 0 until challengesArray.length()) {
            val challengeObj = challengesArray.getJSONObject(i)
            val completedDays = challengeObj.optIntCompat("completed_days", "completedDays", "currentProgress", default = 0)
            val currentStreak = when {
                challengeObj.has("current_streak") -> challengeObj.optInt("current_streak", 0)
                challengeObj.has("currentStreak") -> challengeObj.optInt("currentStreak", 0)
                challengeObj.has("currentProgress") -> completedDays
                else -> 0
            }

            val challenge = Challenge(
                id = 0,
                name = challengeObj.optStringCompat("name"),
                targetDays = challengeObj.optIntCompat("target_days", "targetDays", default = 30),
                icon = challengeObj.optStringCompat("icon", default = "\uD83C\uDFAF"),
                startDate = challengeObj.optStringCompat("start_date", "startDate", default = LocalDate.now().toString()),
                completedDays = completedDays,
                currentStreak = currentStreak,
                lastCompletedDate = challengeObj.optStringCompat("last_completed_date", "lastCompletedDate"),
                reminderTime = challengeObj.optNullableStringCompat("reminder_time", "reminderTime"),
                graceDaysUsed = challengeObj.optIntCompat("grace_days_used", "graceDaysUsed", default = 0)
            )

            if (challenge.name.isNotBlank()) {
                taskRepository.insertChallenge(challenge)
            }
        }
    }

    // ─── Streak ──────────────────────────────────────

    private suspend fun importStreak(jsonObject: JSONObject) {
        if (!jsonObject.has("streak")) return

        val streakObj = jsonObject.getJSONObject("streak")
        val lastCompletedDate = streakObj.optStringCompat(
            "last_completed_date",
            "lastCompletedDate",
            "lastCompletionDate"
        )

        val streakData = StreakData(
            currentStreak = streakObj.optIntCompat("current_streak", "currentStreak", default = 0),
            longestStreak = streakObj.optIntCompat("longest_streak", "longestStreak", default = 0),
            totalCompletedDays = streakObj.optIntCompat("total_completed_days", "totalCompletedDays", default = 0),
            lastCompletedDate = lastCompletedDate,
            graceDaysUsed = streakObj.optIntCompat("grace_days_used", "graceDaysUsed", default = 0),
            lastCheckedDate = streakObj.optStringCompat("last_checked_date", "lastCheckedDate", default = lastCompletedDate)
        )
        taskRepository.saveStreakData(streakData)
    }

    // ─── Multi-format JSON helpers ───────────────────

    private fun JSONObject.optIntCompat(vararg keys: String, default: Int = 0): Int {
        for (key in keys) {
            if (has(key)) return optInt(key, default)
        }
        return default
    }

    private fun JSONObject.optBooleanCompat(vararg keys: String, default: Boolean = false): Boolean {
        for (key in keys) {
            if (has(key)) return optBoolean(key, default)
        }
        return default
    }

    private fun JSONObject.optStringCompat(vararg keys: String, default: String = ""): String {
        for (key in keys) {
            if (!has(key)) continue
            val value = optString(key, "")
            if (value.isNotBlank() && value.lowercase(Locale.US) != "null") {
                return value
            }
        }
        return default
    }

    private fun JSONObject.optNullableStringCompat(vararg keys: String): String? {
        val value = optStringCompat(*keys, default = "")
        return value.ifBlank { null }
    }

    companion object {
        private const val TAG = "DataImportHelper"
    }
}
