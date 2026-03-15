package com.focus3.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus3.app.data.model.CalendarNote
import com.focus3.app.data.model.DailyTask
import com.focus3.app.data.model.StreakData
import com.focus3.app.data.repository.TaskRepository
import com.focus3.app.ui.screens.CustomMilestone
import com.focus3.app.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import com.focus3.app.util.QuotesData
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

import com.focus3.app.data.model.Challenge
import com.focus3.app.data.dao.CalendarNoteDao

@androidx.compose.runtime.Immutable
data class MainUiState(
    val tasks: List<DailyTask> = emptyList(),
    val allTasksHistory: List<DailyTask> = emptyList(), // All historical tasks for analytics
    val streak: Int = 0,
    val formattedDate: String = "",
    val allCompleted: Boolean = false,
    val showConfetti: Boolean = false,
    val isLoading: Boolean = true,
    val completionProgress: Float = 0f,
    val completionHistory: Map<String, Int> = emptyMap(),
    val calendarNotes: Map<String, CalendarNote> = emptyMap(),
    val currentView: AppView = AppView.HOME,
    val showOnboarding: Boolean = false,
    val userAvatar: String = "\uD83D\uDE0E",
    val userName: String = "Focus Champion",
    val showTutorial: Boolean = false,
    val customMilestones: List<CustomMilestone> = emptyList(),
    val challenges: List<Challenge> = emptyList(),
    val selectedChallenge: Challenge? = null,
    val completionQuote: String? = null,
    val appOpenQuote: String? = null,
    val permanentQuote: String = "",
    val graceChancesRemaining: Int = 3,
    val showCompletionQuote: Boolean = false,
    val showAppOpenQuote: Boolean = false,
    val confettiMessage: String = "\uD83C\uDF89 Goal Complete!",
    // Epic Celebration System
    val showCelebration: Boolean = false,
    val celebrationType: String = "DAILY_COMPLETE",
    val celebrationChallengeName: String = "",
    // Settings
    val longestStreak: Int = 0,
    val totalCompletedDays: Int = 0,
    val graceDaysUsed: Int = 0,
    val notificationsEnabled: Boolean = true,
    val dailyReminderTime: String = "08:00"
)

enum class AppView {
    HOME, ANALYTICS, ONBOARDING, STREAK_JOURNEY, PROFILE, CUSTOM_MILESTONES, TUTORIAL, CHALLENGES, NOTES, NOTE_EDITOR, CALENDAR, EDIT_CHALLENGE, SETTINGS
}

// Intermediate data classes for combine grouping (file-level for Kotlin 2.0 compatibility)
internal data class CombinedGroup1(
    val tasks: List<DailyTask>,
    val streakData: StreakData?,
    val history: Map<String, Int>,
    val showConfetti: Boolean,
    val currentView: AppView
)

internal data class CombinedGroup2(
    val customMilestones: List<CustomMilestone>,
    val challenges: List<Challenge>,
    val selectedChallengeId: Int?,
    val completionQuote: String?,
    val showCompletionQuote: Boolean
)

internal data class CombinedGroup3(
    val appOpenQuote: String?,
    val showAppOpenQuote: Boolean,
    val permanentQuote: String,
    val confettiMessage: String,
    val showCelebration: Boolean,
    val celebrationType: String,
    val celebrationChallengeName: String,
    val notificationsEnabled: Boolean,
    val dailyReminderTime: String
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val calendarNoteDao: CalendarNoteDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context
) : ViewModel() {
    
    // SharedPreferences for notification settings - MUST match alarm scheduler!
    private val prefs = appContext.getSharedPreferences("focus3_preferences", android.content.Context.MODE_PRIVATE)
    
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    private val _dailyReminderTime = MutableStateFlow(prefs.getString("reminder_time", "08:00") ?: "08:00")
    
    // User Profile
    private val _userAvatar = MutableStateFlow(prefs.getString("user_avatar", "\uD83D\uDE0E") ?: "\uD83D\uDE0E")
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Focus Champion") ?: "Focus Champion")
    
    private val _showConfetti = MutableStateFlow(false)
    private val _hasShownConfettiToday = MutableStateFlow(false)
    // Track challenges that have shown quotes today to prevent repeated quotes
    private val _challengesWithQuoteShownToday = MutableStateFlow<Set<Int>>(emptySet())
    
    private val _currentView = MutableStateFlow(AppView.HOME)
    private val _navigationBackStack = mutableListOf<AppView>() // Back stack for navigation
    private val _customMilestones = MutableStateFlow<List<CustomMilestone>>(emptyList())
    private val _selectedChallengeId = MutableStateFlow<Int?>(null)
    
    private val _completionQuote = MutableStateFlow<String?>(null)
    private val _showCompletionQuote = MutableStateFlow(false)
    private val _appOpenQuote = MutableStateFlow<String?>(null)
    private val _showAppOpenQuote = MutableStateFlow(false)
    private val _permanentQuote = MutableStateFlow(QuotesData.getHomeQuote())
    private val _confettiMessage = MutableStateFlow("\uD83C\uDF89 Goal Complete!")
    
    // Calendar Notes - convert list to map for easy lookup
    private val _calendarNotes = calendarNoteDao.getAllNotes().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    
    // Celebration System States
    private val _showCelebration = MutableStateFlow(false)
    private val _celebrationType = MutableStateFlow("DAILY_COMPLETE")
    private val _celebrationChallengeName = MutableStateFlow("")
    
    init {
        // Refresh quote on app open (ViewModel creation)
        _permanentQuote.value = QuotesData.getHomeQuote()
        
        // Initialize today's tasks
        viewModelScope.launch {
            taskRepository.initializeTodayTasks()
            taskRepository.checkAndUpdateStreak()
        }
        
        // Ensure daily reminder is scheduled on app start
        ensureReminderScheduled()
    }
    
    private fun ensureReminderScheduled() {
        val isEnabled = prefs.getBoolean("notifications_enabled", true)
        if (isEnabled) {
            val timeParts = _dailyReminderTime.value.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            
            // Save the keys for AlarmScheduler
            prefs.edit()
                .putInt("daily_reminder_hour", hour)
                .putInt("daily_reminder_minute", minute)
                .putBoolean("daily_reminder_enabled", true)
                .apply()
            
            // Schedule the reminder
            com.focus3.app.notification.DailyGoalAlarmScheduler.scheduleReminder(appContext, hour, minute)
            if (BuildConfig.DEBUG) android.util.Log.d("MainViewModel", "Init: Scheduled reminder at $hour:$minute")
        }
    }

    
    private val group1Flow: Flow<CombinedGroup1> = combine(
        taskRepository.getTasksForToday(),
        taskRepository.getStreakData(),
        taskRepository.getCompletionHistory(7),
        _showConfetti,
        _currentView
    ) { tasks, streakData, history, showConfetti, currentView ->
        CombinedGroup1(tasks, streakData, history, showConfetti, currentView)
    }
    
    private val group2Flow: Flow<CombinedGroup2> = combine(
        _customMilestones,
        taskRepository.getAllChallenges(),
        _selectedChallengeId,
        _completionQuote,
        _showCompletionQuote
    ) { customMilestones, challenges, selectedId, completionQuote, showCompletionQuote ->
        CombinedGroup2(customMilestones, challenges, selectedId, completionQuote, showCompletionQuote)
    }
    
    private val group3Flow: Flow<CombinedGroup3> = combine(
        combine(
            _appOpenQuote,
            _showAppOpenQuote,
            _permanentQuote,
            _confettiMessage
        ) { a, b, c, d -> listOf(a, b, c, d) },
        combine(
            _showCelebration,
            _celebrationType,
            _celebrationChallengeName
        ) { a, b, c -> Triple(a, b, c) },
        _notificationsEnabled,
        _dailyReminderTime
    ) { list, triple, notifEnabled, reminderTime ->
        CombinedGroup3(
            appOpenQuote = list[0] as String?,
            showAppOpenQuote = list[1] as Boolean,
            permanentQuote = list[2] as String,
            confettiMessage = list[3] as String,
            showCelebration = triple.first,
            celebrationType = triple.second,
            celebrationChallengeName = triple.third,
            notificationsEnabled = notifEnabled,
            dailyReminderTime = reminderTime
        )
    }
    
    private val allHistoricalTasksFlow: Flow<List<DailyTask>> = taskRepository.getAllHistoricalTasks(30)
    
    val uiState: StateFlow<MainUiState> = combine(
        group1Flow,
        group2Flow,
        group3Flow,
        _calendarNotes,
        allHistoricalTasksFlow
    ) { g1, g2, g3, calendarNotesList, allHistoricalTasks ->
        // Extract from group 1
        val tasks = g1.tasks
        val streakData = g1.streakData
        val history = g1.history
        val showConfetti = g1.showConfetti
        val currentView = g1.currentView
        
        // Extract from group 2
        val customMilestones = g2.customMilestones
        val challenges = g2.challenges
        val selectedId = g2.selectedChallengeId
        val completionQuote = g2.completionQuote
        val showCompQuote = g2.showCompletionQuote
        
        // Extract from group 3
        val appOpenQuote = g3.appOpenQuote
        val showAppQuote = g3.showAppOpenQuote
        val permanentQuote = g3.permanentQuote
        val confettiMsg = g3.confettiMessage
        val showCelebration = g3.showCelebration
        val celebrationType = g3.celebrationType
        val celebrationChallengeName = g3.celebrationChallengeName
        
        // Convert calendar notes list to map by date
        val calendarNotesMap = calendarNotesList.associateBy { it.date }

        // CRITICAL: Only count first 3 tasks for progress (database may have more)
        val first3Tasks = tasks.take(3)
        val completedCount = first3Tasks.count { it.isCompleted }
        val progress = if (first3Tasks.isNotEmpty()) completedCount.toFloat() / 3f else 0f
        val allCompleted = first3Tasks.size == 3 && first3Tasks.all { it.isCompleted }
        
        MainUiState(
            tasks = tasks,
            allTasksHistory = allHistoricalTasks,
            streak = streakData?.currentStreak ?: 0,
            formattedDate = getFormattedDate(),
            allCompleted = allCompleted,
            showConfetti = showConfetti,
            isLoading = false,
            completionProgress = progress,
            completionHistory = history,
            calendarNotes = calendarNotesMap,
            currentView = currentView,
            customMilestones = customMilestones,
            challenges = challenges,
            selectedChallenge = challenges.find { it.id == selectedId },
            completionQuote = completionQuote,
            showCompletionQuote = showCompQuote,
            appOpenQuote = appOpenQuote,
            showAppOpenQuote = showAppQuote,
            permanentQuote = permanentQuote,
            graceChancesRemaining = 3 - (streakData?.graceDaysUsed ?: 0),
            confettiMessage = confettiMsg,
            showCelebration = showCelebration,
            celebrationType = celebrationType,
            celebrationChallengeName = celebrationChallengeName,
            longestStreak = streakData?.longestStreak ?: 0,
            totalCompletedDays = streakData?.totalCompletedDays ?: 0,
            graceDaysUsed = streakData?.graceDaysUsed ?: 0,
            notificationsEnabled = g3.notificationsEnabled,
            dailyReminderTime = g3.dailyReminderTime,
            userAvatar = _userAvatar.value,
            userName = _userName.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun addChallenge(name: String, targetDays: Int, icon: String, reminderTime: String?) {
        viewModelScope.launch {
            val challenge = Challenge(
                name = name, 
                targetDays = targetDays, 
                icon = icon,
                reminderTime = reminderTime
            )
            taskRepository.insertChallenge(challenge)
            
            // Schedule reminder if time is set
            reminderTime?.let { time ->
                val parsed = com.focus3.app.notification.ChallengeAlarmScheduler.parseTimeString(time)
                parsed?.let { (hour, minute) ->
                    // Note: We need the actual ID after insert, so we get latest challenges
                    val challenges = taskRepository.getAllChallenges().first()
                    val newChallenge = challenges.find { it.name == name }
                    newChallenge?.let {
                        com.focus3.app.notification.ChallengeAlarmScheduler.scheduleReminder(
                            context = appContext,
                            challengeId = it.id,
                            challengeName = name,
                            challengeIcon = icon,
                            hour = hour,
                            minute = minute
                        )
                    }
                }
            }
        }
    }

    fun deleteChallenge(challengeId: Int) {
        viewModelScope.launch {
            val challenge = uiState.value.challenges.find { it.id == challengeId }
            challenge?.let { 
                taskRepository.deleteChallenge(it)
                // Cancel any scheduled reminder
                com.focus3.app.notification.ChallengeAlarmScheduler.cancelReminder(appContext, challengeId)
            }
            // Navigate back after deletion
            _currentView.value = AppView.CHALLENGES
        }
    }
    
    fun updateChallenge(challenge: Challenge) {
        viewModelScope.launch {
            taskRepository.updateChallenge(challenge)
            
            // Update reminder schedule if time is set
            challenge.reminderTime?.let { time ->
                val parsed = com.focus3.app.notification.ChallengeAlarmScheduler.parseTimeString(time)
                parsed?.let { (hour, minute) ->
                    com.focus3.app.notification.ChallengeAlarmScheduler.scheduleReminder(
                        context = appContext,
                        challengeId = challenge.id,
                        challengeName = challenge.name,
                        challengeIcon = challenge.icon,
                        hour = hour,
                        minute = minute
                    )
                }
            } ?: run {
                // If no reminder time, cancel any existing reminder
                com.focus3.app.notification.ChallengeAlarmScheduler.cancelReminder(appContext, challenge.id)
            }
            
            // Navigate back to challenges after saving
            _currentView.value = AppView.CHALLENGES
        }
    }

    fun updateChallengeProgress(challengeId: Int) {
        viewModelScope.launch {
            // Repository returns true if progress was actually updated
            val wasUpdated = taskRepository.updateChallengeProgress(challengeId)
            
            if (wasUpdated) {
                // Check if challenge is now FULLY COMPLETE
                val challenges = taskRepository.getAllChallenges().first()
                val challengeAfter = challenges.find { c -> c.id == challengeId }
                
                if (challengeAfter != null && challengeAfter.completedDays >= challengeAfter.targetDays) {
                    // Challenge fully complete - trigger epic celebration.
                    triggerChallengeCompleteCelebration(challengeAfter.name)
                } else {
                    // Just progress - show simple confetti
                    _confettiMessage.value = "\uD83D\uDD25 Challenge Progress!"
                    _showConfetti.value = true
                }
                
                // Check for streak milestones
                val newStreak = challengeAfter?.currentStreak ?: 0
                val milestones = listOf(7, 14, 21, 30, 50, 75, 100, 365)
                if (newStreak in milestones) {
                    viewModelScope.launch {
                        delay(3000) // Wait for current celebration
                        triggerStreakMilestoneCelebration(newStreak)
                    }
                }
            }
        }
    }

    fun selectChallenge(id: Int?) {
        _selectedChallengeId.value = id
    }

    fun navigateTo(view: AppView) {
        // Don't add to back stack if navigating to same view
        if (_currentView.value != view) {
            // Add current view to back stack before navigating (cap at 20 to prevent memory leak)
            _navigationBackStack.add(_currentView.value)
            if (_navigationBackStack.size > 20) {
                _navigationBackStack.removeAt(0)
            }
        }
        _currentView.value = view
    }
    
    // Check if can go back (not on HOME or empty stack)
    fun canGoBack(): Boolean {
        return _navigationBackStack.isNotEmpty()
    }
    
    // Handle back button press - returns true if handled, false if should exit app
    fun onBackPressed(): Boolean {
        if (_navigationBackStack.isNotEmpty()) {
            val previousView = _navigationBackStack.removeLast()
            _currentView.value = previousView
            return true
        }
        // No back stack - should exit app
        return false
    }
    
    fun updateCustomMilestones(milestones: List<CustomMilestone>) {
        _customMilestones.value = milestones
    }
    
    fun toggleView() {
        _currentView.value = if (_currentView.value == AppView.HOME) AppView.ANALYTICS else AppView.HOME
    }
    
    private fun getFormattedDate(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        return today.format(formatter)
    }
    
    fun updateTaskContent(taskId: Int, content: String) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskContent(taskId, content)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error updating task: ${e.message}")
            }
        }
    }
    
    fun toggleTaskCompletion(task: DailyTask) {
        viewModelScope.launch {
            val wasCompleted = task.isCompleted
            taskRepository.toggleTaskCompletion(task.id)
            
            // Trigger celebration on completion
            if (!wasCompleted) {
                _confettiMessage.value = "\u2705 Task Done!"
                _showConfetti.value = true
            } else {
                // Task was uncompleted - handle streak revert if needed
                taskRepository.handleTaskUncompletion()
            }
            
            // Check if all tasks are now completed - ONLY check first 3 tasks
            val tasks = taskRepository.getTasksForDateSync(taskRepository.getTodayString()).take(3)
            val completedCount = tasks.count { it.isCompleted }
            val allCompleted = completedCount == 3
            
            if (allCompleted && !_hasShownConfettiToday.value) {
                _confettiMessage.value = "\uD83C\uDFC6 All Goals Complete!"
                _showConfetti.value = true
                _hasShownConfettiToday.value = true
                
                // Update streak on completion
                taskRepository.updateStreakOnCompletion()
                
                // TRIGGER EPIC DOPAMINE CELEBRATION!
                triggerDailyCompleteCelebration()
            }
        }
    }

    fun completeSelectedChallenge() {
        val challengeId = _selectedChallengeId.value ?: return
        
        viewModelScope.launch {
            // Smart tracking: Repository returns true only if progress was actually updated
            // (i.e., not already completed today). This is database-backed and persists.
            val wasUpdated = taskRepository.updateChallengeProgress(challengeId)
            
            // Only show celebration if progress was actually updated (first time today)
            if (wasUpdated) {
                // Check if challenge is now complete
                val challenges = taskRepository.getAllChallenges().first()
                val challengeAfter = challenges.find { c -> c.id == challengeId }
                
                if (challengeAfter != null && challengeAfter.completedDays >= challengeAfter.targetDays) {
                    // CHALLENGE COMPLETE - MEGA CELEBRATION!
                    triggerChallengeCompleteCelebration(challengeAfter.name)
                } else {
                    // Progress celebration
                    triggerChallengeProgressCelebration()
                }
                
                // Check for streak milestones
                val newStreak = challengeAfter?.currentStreak ?: 0
                val milestones = listOf(7, 14, 21, 30, 50, 75, 100, 365)
                if (newStreak in milestones) {
                    viewModelScope.launch {
                        delay(2000) // Wait for progress celebration to finish
                        triggerStreakMilestoneCelebration(newStreak)
                    }
                }
            }
        }
    }
    
    // ==================== CELEBRATION SYSTEM ====================
    
    fun triggerDailyCompleteCelebration() {
        _celebrationType.value = "DAILY_COMPLETE"
        _celebrationChallengeName.value = ""
        _showCelebration.value = true
    }
    
    fun triggerChallengeProgressCelebration() {
        _celebrationType.value = "CHALLENGE_PROGRESS"
        _celebrationChallengeName.value = ""
        _showCelebration.value = true
    }
    
    fun triggerChallengeCompleteCelebration(challengeName: String) {
        _celebrationType.value = "CHALLENGE_COMPLETE"
        _celebrationChallengeName.value = challengeName
        _showCelebration.value = true
    }
    
    fun triggerStreakMilestoneCelebration(days: Int) {
        _celebrationType.value = "STREAK_MILESTONE"
        _celebrationChallengeName.value = days.toString()
        _showCelebration.value = true
    }
    
    fun dismissCelebration() {
        _showCelebration.value = false
    }
    
    fun dismissConfetti() {
        _showConfetti.value = false
    }
    
    fun dismissCompletionQuote() {
        _showCompletionQuote.value = false
    }
    
    fun getTask(index: Int): DailyTask? {
        return uiState.value.tasks.getOrNull(index)
    }
    
    // Calendar Notes Functions with error handling
    fun saveCalendarNote(note: CalendarNote) {
        viewModelScope.launch {
            try {
                calendarNoteDao.insertNote(note)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error saving calendar note: ${e.message}")
            }
        }
    }
    
    fun deleteCalendarNote(date: String) {
        viewModelScope.launch {
            try {
                calendarNoteDao.deleteNoteForDate(date)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error deleting calendar note: ${e.message}")
            }
        }
    }
    
    // Settings Functions
    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit()
            .putBoolean("notifications_enabled", enabled)
            .putBoolean("daily_reminder_enabled", enabled) // Key used by AlarmScheduler
            .apply()
        
        if (enabled) {
            // Schedule reminder with current saved time using AlarmManager for EXACT time
            val timeParts = _dailyReminderTime.value.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            
            // Also save hour/minute for AlarmScheduler
            prefs.edit()
                .putInt("daily_reminder_hour", hour)
                .putInt("daily_reminder_minute", minute)
                .apply()
            
            com.focus3.app.notification.DailyGoalAlarmScheduler.scheduleReminder(appContext, hour, minute)
            if (BuildConfig.DEBUG) android.util.Log.d("MainViewModel", "Notifications enabled, scheduled at $hour:$minute")
        } else {
            // Cancel reminder
            com.focus3.app.notification.DailyGoalAlarmScheduler.cancelReminder(appContext)
            if (BuildConfig.DEBUG) android.util.Log.d("MainViewModel", "Notifications disabled, cancelled reminder")
        }
    }
    
    fun setReminderTime(time: String) {
        _dailyReminderTime.value = time
        prefs.edit().putString("reminder_time", time).apply()
        
        // Parse and save hour/minute separately for AlarmManager
        val timeParts = time.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        
        // Save to keys that AlarmScheduler reads
        prefs.edit()
            .putInt("daily_reminder_hour", hour)
            .putInt("daily_reminder_minute", minute)
            .putBoolean("daily_reminder_enabled", _notificationsEnabled.value)
            .apply()
        
        // Reschedule if notifications are enabled using AlarmManager for EXACT time
        if (_notificationsEnabled.value) {
            // Use AlarmManager for exact time instead of WorkManager
            com.focus3.app.notification.DailyGoalAlarmScheduler.scheduleReminder(appContext, hour, minute)
            if (BuildConfig.DEBUG) android.util.Log.d("MainViewModel", "Scheduled reminder at $hour:$minute")
        }
    }
    
    // Profile Functions
    fun saveUserAvatar(avatar: String) {
        _userAvatar.value = avatar
        prefs.edit().putString("user_avatar", avatar).apply()
    }
    
    fun saveUserName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
    }
    
    // Clear All Data
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear all tasks
                taskRepository.deleteAllTasks()
                
                // Reset streak
                taskRepository.saveStreakData(StreakData())
                
                // Delete all challenges
                taskRepository.deleteAllChallenges()
                
                // Delete all calendar notes
                calendarNoteDao.deleteAll()
                
                // Reset preferences
                prefs.edit().clear().apply()
                
                // Reset in-memory states
                _customMilestones.value = emptyList()
                _userAvatar.value = "\uD83D\uDE0E"
                _userName.value = "Focus Champion"
                _notificationsEnabled.value = true
                _dailyReminderTime.value = "08:00"
                _hasShownConfettiToday.value = false
                _challengesWithQuoteShownToday.value = emptySet()
                _navigationBackStack.clear()
                _currentView.value = AppView.HOME
                
                // Re-initialize today's tasks so HomeScreen isn't empty
                taskRepository.initializeTodayTasks()
                
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error clearing data: ${e.message}")
            }
        }
    }
    // Import Data from JSON backup
    fun importDataFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: return@launch

                // Parse JSON and import data
                val jsonObject = org.json.JSONObject(jsonString)

                fun org.json.JSONObject.optIntCompat(vararg keys: String, default: Int = 0): Int {
                    for (key in keys) {
                        if (has(key)) return optInt(key, default)
                    }
                    return default
                }

                fun org.json.JSONObject.optBooleanCompat(vararg keys: String, default: Boolean = false): Boolean {
                    for (key in keys) {
                        if (has(key)) return optBoolean(key, default)
                    }
                    return default
                }

                fun org.json.JSONObject.optStringCompat(vararg keys: String, default: String = ""): String {
                    for (key in keys) {
                        if (!has(key)) continue
                        val value = optString(key, "")
                        if (value.isNotBlank() && value.lowercase(Locale.US) != "null") {
                            return value
                        }
                    }
                    return default
                }

                fun org.json.JSONObject.optNullableStringCompat(vararg keys: String): String? {
                    val value = optStringCompat(*keys, default = "")
                    return value.ifBlank { null }
                }

                // Import tasks if present
                if (jsonObject.has("tasks")) {
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
                            id = 0, // Let Room auto-generate
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

                // Import challenges if present
                if (jsonObject.has("challenges")) {
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
                            id = 0, // Let Room auto-generate
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

                // Import streak if present
                if (jsonObject.has("streak")) {
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

            } catch (e: Exception) {
                // Handle error silently - could add error feedback here
                android.util.Log.e("MainViewModel", "Error importing data: ${e.message}")
            }
        }
    }
}
