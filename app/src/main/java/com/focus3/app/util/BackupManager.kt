package com.focus3.app.util

import com.focus3.app.data.dao.CalendarNoteDao
import com.focus3.app.data.dao.ChallengeDao
import com.focus3.app.data.dao.NoteDao
import com.focus3.app.data.dao.TaskDao
import javax.inject.Inject
import javax.inject.Singleton

// Temporary stub to fix Hilt generated code issue
@Singleton
class BackupManager @Inject constructor(
    private val taskDao: TaskDao,
    private val challengeDao: ChallengeDao,
    private val noteDao: NoteDao,
    private val calendarNoteDao: CalendarNoteDao
) {
    // Stub - can be implemented later if needed
}
