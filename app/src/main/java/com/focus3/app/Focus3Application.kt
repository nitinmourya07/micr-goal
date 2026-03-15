package com.focus3.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.focus3.app.notification.NotificationHelper
import com.focus3.app.worker.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Focus3Application : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize notification channels (Next-Level Notification System)
            NotificationHelper.createNotificationChannels(this)
            
            // Schedule midnight reset worker
            WorkManagerHelper.scheduleMidnightReset(this)
            // Schedule daily goal reminder at 8 PM
            WorkManagerHelper.scheduleDailyGoalReminder(this)
            // Schedule challenge reminder at 7 PM
            WorkManagerHelper.scheduleChallengeReminder(this)
            
            // 🚨 AGGRESSIVE STREAK PROTECTION 🚨
            // Multiple reminders throughout the day to ensure no day is missed
            WorkManagerHelper.scheduleAggressiveStreakProtection(this)
        } catch (e: Exception) {
            android.util.Log.e("Focus3App", "Error during app initialization: ${e.message}")
        }
    }
}
