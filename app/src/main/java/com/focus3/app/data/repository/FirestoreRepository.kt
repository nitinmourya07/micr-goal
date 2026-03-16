package com.focus3.app.data.repository

import android.content.Context
import android.util.Log
import com.focus3.app.data.dao.CalendarNoteDao
import com.focus3.app.data.dao.ChallengeDao
import com.focus3.app.data.dao.NoteDao
import com.focus3.app.data.dao.TaskDao
import com.focus3.app.data.model.CalendarNote
import com.focus3.app.data.model.Challenge
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.Note
import com.focus3.app.data.model.StreakData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ☁️ Firestore Cloud Sync Repository
 * Pushes and pulls all Room data to/from Cloud Firestore under
 * the path: users/{uid}/...
 *
 * Collections per user:
 *   users/{uid}/tasks       → DailyTask documents
 *   users/{uid}/challenges  → Challenge documents
 *   users/{uid}/notes       → Note documents
 *   users/{uid}/calendarNotes → CalendarNote documents
 *   users/{uid}/metadata/streak → StreakData document
 */
@Singleton
class FirestoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val challengeDao: ChallengeDao,
    private val noteDao: NoteDao,
    private val calendarNoteDao: CalendarNoteDao
) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ═══════════════════════════════════════════════════
    // PUSH — Room → Firestore
    // ═══════════════════════════════════════════════════

    /**
     * Push ALL local Room data to Firestore for the given user.
     * Overwrites remote data with local data (local wins).
     */
    suspend fun pushAllData(uid: String) {
        try {
            pushTasks(uid)
            pushChallenges(uid)
            pushStreak(uid)
            pushNotes(uid)
            pushCalendarNotes(uid)
            Log.d(TAG, "Successfully pushed all data for uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing data: ${e.message}")
            throw e
        }
    }

    private suspend fun pushTasks(uid: String) {
        val collection = firestore.collection("users").document(uid).collection("tasks")
        // Get all tasks (last 90 days should be in DB already)
        val tasks = taskDao.getTasksForDateSync("2000-01-01") // gets nothing if empty; use a broad range
        // Actually let's get all tasks from the DB directly
        val allTasks = mutableListOf<DailyTask>()
        val recentDates = taskDao.getRecentDatesWithTasks(365)
        for (date in recentDates) {
            allTasks.addAll(taskDao.getTasksForDateSync(date))
        }

        // Batch writes (max 500 per batch)
        val batches = allTasks.chunked(400)
        for (chunk in batches) {
            val batch = firestore.batch()
            for (task in chunk) {
                val docRef = collection.document("${task.date}_${task.taskIndex}")
                batch.set(docRef, mapOf(
                    "date" to task.date,
                    "taskIndex" to task.taskIndex,
                    "content" to task.content,
                    "isCompleted" to task.isCompleted
                ))
            }
            batch.commit().await()
        }
        Log.d(TAG, "Pushed ${allTasks.size} tasks")
    }

    private suspend fun pushChallenges(uid: String) {
        val collection = firestore.collection("users").document(uid).collection("challenges")
        val challenges = challengeDao.getAllChallengesSync()

        val batch = firestore.batch()
        for (challenge in challenges) {
            val docRef = collection.document(challenge.id.toString())
            batch.set(docRef, mapOf(
                "name" to challenge.name,
                "targetDays" to challenge.targetDays,
                "icon" to challenge.icon,
                "startDate" to challenge.startDate,
                "completedDays" to challenge.completedDays,
                "currentStreak" to challenge.currentStreak,
                "lastCompletedDate" to challenge.lastCompletedDate,
                "reminderTime" to (challenge.reminderTime ?: ""),
                "graceDaysUsed" to challenge.graceDaysUsed
            ))
        }
        batch.commit().await()
        Log.d(TAG, "Pushed ${challenges.size} challenges")
    }

    private suspend fun pushStreak(uid: String) {
        val streakData = taskDao.getStreakDataSync() ?: StreakData()
        val docRef = firestore.collection("users").document(uid)
            .collection("metadata").document("streak")

        docRef.set(mapOf(
            "currentStreak" to streakData.currentStreak,
            "longestStreak" to streakData.longestStreak,
            "totalCompletedDays" to streakData.totalCompletedDays,
            "lastCompletedDate" to streakData.lastCompletedDate,
            "graceDaysUsed" to streakData.graceDaysUsed,
            "lastCheckedDate" to streakData.lastCheckedDate
        )).await()
        Log.d(TAG, "Pushed streak data")
    }

    private suspend fun pushNotes(uid: String) {
        val collection = firestore.collection("users").document(uid).collection("notes")
        val notes = noteDao.getRecentNotes(500) // Reasonable limit

        val batches = notes.chunked(400)
        for (chunk in batches) {
            val batch = firestore.batch()
            for (note in chunk) {
                val docRef = collection.document(note.id.toString())
                batch.set(docRef, mapOf(
                    "title" to note.title,
                    "content" to note.content,
                    "emoji" to note.emoji,
                    "color" to note.color,
                    "category" to note.category,
                    "isPinned" to note.isPinned,
                    "labels" to note.labels,
                    "reminderTime" to (note.reminderTime ?: ""),
                    "isArchived" to note.isArchived,
                    "checklistItems" to note.checklistItems,
                    "imageUri" to (note.imageUri ?: ""),
                    "createdAt" to note.createdAt,
                    "updatedAt" to note.updatedAt
                ))
            }
            batch.commit().await()
        }
        Log.d(TAG, "Pushed ${notes.size} notes")
    }

    private suspend fun pushCalendarNotes(uid: String) {
        val collection = firestore.collection("users").document(uid).collection("calendarNotes")
        val entries = calendarNoteDao.getRecentEntries(365)

        val batches = entries.chunked(400)
        for (chunk in batches) {
            val batch = firestore.batch()
            for (entry in chunk) {
                val docRef = collection.document(entry.date)
                batch.set(docRef, mapOf(
                    "date" to entry.date,
                    "content" to entry.content,
                    "mood" to entry.mood,
                    "createdAt" to entry.createdAt,
                    "updatedAt" to entry.updatedAt
                ))
            }
            batch.commit().await()
        }
        Log.d(TAG, "Pushed ${entries.size} calendar notes")
    }

    // ═══════════════════════════════════════════════════
    // PULL — Firestore → Room
    // ═══════════════════════════════════════════════════

    /**
     * Pull ALL Firestore data for the given user and write it to Room.
     * Remote data merges with local (replace on conflict via INSERT OR REPLACE).
     */
    suspend fun pullAllData(uid: String) {
        try {
            pullTasks(uid)
            pullChallenges(uid)
            pullStreak(uid)
            pullNotes(uid)
            pullCalendarNotes(uid)
            Log.d(TAG, "Successfully pulled all data for uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling data: ${e.message}")
            throw e
        }
    }

    private suspend fun pullTasks(uid: String) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        // Check if today's tasks already exist locally — if so, DON'T overwrite them
        val localTodayTasks = taskDao.getTasksForDateSync(today)
        val hasTodayLocally = localTodayTasks.isNotEmpty()
        
        val snapshot = firestore.collection("users").document(uid)
            .collection("tasks").get().await()

        var count = 0
        for (doc in snapshot.documents) {
            try {
                val date = doc.getString("date") ?: continue
                
                // CRITICAL: Skip today's tasks if they exist locally
                // This preserves local completion state (isCompleted)
                if (date == today && hasTodayLocally) {
                    continue
                }
                
                val task = DailyTask(
                    id = 0, // Auto-generate
                    date = date,
                    taskIndex = (doc.getLong("taskIndex") ?: continue).toInt(),
                    content = doc.getString("content") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
                taskDao.insertTask(task) // REPLACE on conflict (date + taskIndex unique index)
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed task doc ${doc.id}: ${e.message}")
            }
        }
        Log.d(TAG, "Pulled $count tasks (skipped today=$hasTodayLocally)")
    }

    private suspend fun pullChallenges(uid: String) {
        val snapshot = firestore.collection("users").document(uid)
            .collection("challenges").get().await()

        // CRITICAL FIX: Clear ALL local challenges first, then insert cloud data
        // This prevents duplication — id=0 with autoGenerate always creates new rows
        val cloudChallenges = mutableListOf<Challenge>()
        for (doc in snapshot.documents) {
            try {
                val reminderTimeRaw = doc.getString("reminderTime") ?: ""
                val challenge = Challenge(
                    id = 0, // Will auto-generate on insert
                    name = doc.getString("name") ?: continue,
                    targetDays = (doc.getLong("targetDays") ?: 30).toInt(),
                    icon = doc.getString("icon") ?: "\uD83C\uDFAF",
                    startDate = doc.getString("startDate") ?: "",
                    completedDays = (doc.getLong("completedDays") ?: 0).toInt(),
                    currentStreak = (doc.getLong("currentStreak") ?: 0).toInt(),
                    lastCompletedDate = doc.getString("lastCompletedDate") ?: "",
                    reminderTime = reminderTimeRaw.ifBlank { null },
                    graceDaysUsed = (doc.getLong("graceDaysUsed") ?: 0).toInt()
                )
                cloudChallenges.add(challenge)
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed challenge doc ${doc.id}: ${e.message}")
            }
        }
        
        // Only clear-and-replace if cloud has data (don't wipe on network error)
        if (cloudChallenges.isNotEmpty()) {
            challengeDao.deleteAll()
            for (challenge in cloudChallenges) {
                challengeDao.insertChallenge(challenge)
            }
        }
        Log.d(TAG, "Pulled ${cloudChallenges.size} challenges (clear+insert)")
    }

    private suspend fun pullStreak(uid: String) {
        val doc = firestore.collection("users").document(uid)
            .collection("metadata").document("streak").get().await()

        if (doc.exists()) {
            val streakData = StreakData(
                currentStreak = (doc.getLong("currentStreak") ?: 0).toInt(),
                longestStreak = (doc.getLong("longestStreak") ?: 0).toInt(),
                totalCompletedDays = (doc.getLong("totalCompletedDays") ?: 0).toInt(),
                lastCompletedDate = doc.getString("lastCompletedDate") ?: "",
                graceDaysUsed = (doc.getLong("graceDaysUsed") ?: 0).toInt(),
                lastCheckedDate = doc.getString("lastCheckedDate") ?: ""
            )
            taskDao.insertOrUpdateStreak(streakData)
            Log.d(TAG, "Pulled streak data: streak=${streakData.currentStreak}")
        }
    }

    private suspend fun pullNotes(uid: String) {
        val snapshot = firestore.collection("users").document(uid)
            .collection("notes").get().await()

        var count = 0
        for (doc in snapshot.documents) {
            try {
                val reminderTimeRaw = doc.getString("reminderTime") ?: ""
                val imageUriRaw = doc.getString("imageUri") ?: ""
                val note = Note(
                    id = 0,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    emoji = doc.getString("emoji") ?: "📝",
                    color = doc.getString("color") ?: "#1E3A5F",
                    category = doc.getString("category") ?: "General",
                    isPinned = doc.getBoolean("isPinned") ?: false,
                    labels = doc.getString("labels") ?: "",
                    reminderTime = reminderTimeRaw.ifBlank { null },
                    isArchived = doc.getBoolean("isArchived") ?: false,
                    checklistItems = doc.getString("checklistItems") ?: "",
                    imageUri = imageUriRaw.ifBlank { null },
                    createdAt = doc.getString("createdAt") ?: "",
                    updatedAt = doc.getString("updatedAt") ?: ""
                )
                noteDao.insertNote(note)
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed note doc ${doc.id}: ${e.message}")
            }
        }
        Log.d(TAG, "Pulled $count notes")
    }

    private suspend fun pullCalendarNotes(uid: String) {
        val snapshot = firestore.collection("users").document(uid)
            .collection("calendarNotes").get().await()

        var count = 0
        for (doc in snapshot.documents) {
            try {
                val entry = CalendarNote(
                    id = 0,
                    date = doc.getString("date") ?: continue,
                    content = doc.getString("content") ?: "",
                    mood = doc.getString("mood") ?: "😊",
                    createdAt = (doc.getLong("createdAt") ?: System.currentTimeMillis()),
                    updatedAt = (doc.getLong("updatedAt") ?: System.currentTimeMillis())
                )
                calendarNoteDao.insertNote(entry)
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed calendar note doc ${doc.id}: ${e.message}")
            }
        }
        Log.d(TAG, "Pulled $count calendar notes")
    }

    // ═══════════════════════════════════════════════════
    // ONE-TIME CLEANUP — Nuclear reset for stale data
    // ═══════════════════════════════════════════════════

    /**
     * One-time cleanup: deletes ALL challenges from Firestore and Room.
     * Guarded by SharedPreferences flag so it only runs once ever.
     * This fixes the 127 "exam" mission duplication bug.
     */
    suspend fun nukeAndRebuildChallenges(userId: String) {
        val prefs = context.getSharedPreferences("focus3_prefs", Context.MODE_PRIVATE)
        val alreadyCleaned = prefs.getBoolean("challenges_cleaned_v2", false)
        if (alreadyCleaned) return  // Only run once ever

        try {
            // Step 1: Delete ALL from Firestore for this user
            val collectionRef = firestore
                .collection("users")
                .document(userId)
                .collection("challenges")

            val allDocs = collectionRef.get().await()
            // Batch deletes (max 500 per batch)
            val batches = allDocs.documents.chunked(400)
            for (chunk in batches) {
                val batch = firestore.batch()
                chunk.forEach { doc -> batch.delete(doc.reference) }
                batch.commit().await()
            }

            // Step 2: Delete ALL local challenges
            challengeDao.deleteAll()

            // Step 3: Mark as cleaned — never run again
            prefs.edit()
                .putBoolean("challenges_cleaned_v2", true)
                .apply()

            Log.d(TAG, "Nuclear cleanup complete: wiped all challenges for uid=$userId")
        } catch (e: Exception) {
            Log.e(TAG, "Nuclear cleanup failed: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════════════
    // SINGLE ITEM PUSH/DELETE — Immediate Firestore sync
    // ═══════════════════════════════════════════════════

    /**
     * Push a single challenge to Firestore immediately after local insert.
     */
    suspend fun pushSingleChallenge(uid: String, challenge: Challenge) {
        try {
            val docRef = firestore
                .collection("users")
                .document(uid)
                .collection("challenges")
                .document(challenge.id.toString())

            docRef.set(mapOf(
                "name" to challenge.name,
                "targetDays" to challenge.targetDays,
                "icon" to challenge.icon,
                "startDate" to challenge.startDate,
                "completedDays" to challenge.completedDays,
                "currentStreak" to challenge.currentStreak,
                "lastCompletedDate" to challenge.lastCompletedDate,
                "reminderTime" to (challenge.reminderTime ?: ""),
                "graceDaysUsed" to challenge.graceDaysUsed
            )).await()
            Log.d(TAG, "Pushed single challenge: ${challenge.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push challenge: ${e.message}")
        }
    }

    /**
     * Delete a single challenge from Firestore by its local ID.
     */
    suspend fun deleteSingleChallenge(uid: String, challengeId: Int) {
        try {
            if (challengeId > 0) {
                firestore
                    .collection("users")
                    .document(uid)
                    .collection("challenges")
                    .document(challengeId.toString())
                    .delete()
                    .await()
                Log.d(TAG, "Deleted challenge $challengeId from Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete challenge from Firestore: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════════════
    // SYNC ON LOGIN
    // ═══════════════════════════════════════════════════

    /**
     * Sync strategy on login:
     * 1. Run one-time nuclear cleanup (if not done yet)
     * 2. Push local data FIRST (preserves local work)
     * 3. Pull cloud challenges only if Firestore has data
     * 4. Pull other data normally
     */
    suspend fun syncOnLogin(uid: String) {
        try {
            // STEP 1: One-time nuclear cleanup of stale challenges
            nukeAndRebuildChallenges(uid)

            // STEP 2: Push all local data to Firestore first
            pushAllData(uid)

            // STEP 3: Smart challenge sync — check if cloud has data
            val challengeSnapshot = firestore
                .collection("users")
                .document(uid)
                .collection("challenges")
                .get()
                .await()

            if (challengeSnapshot.isEmpty) {
                // Cloud is empty — local data already pushed above, nothing to pull
                Log.d(TAG, "Firestore challenges empty — local data preserved")
            } else {
                // Cloud has data — pull challenges (replaces local)
                pullChallenges(uid)
            }

            // STEP 4: Pull other data normally (tasks, streak, notes, etc.)
            pullTasks(uid)
            pullStreak(uid)
            pullNotes(uid)
            pullCalendarNotes(uid)

            Log.d(TAG, "Full sync completed for uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "Sync on login failed: ${e.message}")
            // Don't throw — user can still work offline
        }
    }

    companion object {
        private const val TAG = "FirestoreRepo"
    }
}
