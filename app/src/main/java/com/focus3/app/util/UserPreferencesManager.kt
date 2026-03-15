package com.focus3.app.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "focus3_preferences"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        private const val KEY_CHALLENGE_REMINDER_ENABLED = "challenge_reminder_enabled"
        private const val KEY_CHALLENGE_REMINDER_HOUR = "challenge_reminder_hour"
        private const val KEY_CHALLENGE_REMINDER_MINUTE = "challenge_reminder_minute"
        
        // Default times
        const val DEFAULT_DAILY_HOUR = 20 // 8 PM
        const val DEFAULT_DAILY_MINUTE = 0
        const val DEFAULT_CHALLENGE_HOUR = 19 // 7 PM
        const val DEFAULT_CHALLENGE_MINUTE = 0
    }

    // Daily Reminder Settings
    var isDailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, value).apply()

    var dailyReminderHour: Int
        get() = prefs.getInt(KEY_DAILY_REMINDER_HOUR, DEFAULT_DAILY_HOUR)
        set(value) = prefs.edit().putInt(KEY_DAILY_REMINDER_HOUR, value).apply()

    var dailyReminderMinute: Int
        get() = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, DEFAULT_DAILY_MINUTE)
        set(value) = prefs.edit().putInt(KEY_DAILY_REMINDER_MINUTE, value).apply()

    // Challenge Reminder Settings
    var isChallengeReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGE_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CHALLENGE_REMINDER_ENABLED, value).apply()

    var challengeReminderHour: Int
        get() = prefs.getInt(KEY_CHALLENGE_REMINDER_HOUR, DEFAULT_CHALLENGE_HOUR)
        set(value) = prefs.edit().putInt(KEY_CHALLENGE_REMINDER_HOUR, value).apply()

    var challengeReminderMinute: Int
        get() = prefs.getInt(KEY_CHALLENGE_REMINDER_MINUTE, DEFAULT_CHALLENGE_MINUTE)
        set(value) = prefs.edit().putInt(KEY_CHALLENGE_REMINDER_MINUTE, value).apply()

    // Helper to get formatted time string
    fun getDailyReminderTimeFormatted(): String {
        val hour = dailyReminderHour
        val minute = dailyReminderMinute
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }

    fun getChallengeReminderTimeFormatted(): String {
        val hour = challengeReminderHour
        val minute = challengeReminderMinute
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }
}
