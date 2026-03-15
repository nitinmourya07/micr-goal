package com.focus3.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.focus3.app.MainActivity

/**
 * Legacy per-challenge reminder worker.
 * Kept for compatibility with existing schedules.
 */
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "challenge_reminders"
        const val KEY_CHALLENGE_NAME = "challengeName"
        const val KEY_CHALLENGE_ICON = "challengeIcon"
        const val KEY_CHALLENGE_ID = "challengeId"
        const val KEY_DAYS_COMPLETED = "daysCompleted"
        const val KEY_TARGET_DAYS = "targetDays"
        const val KEY_CURRENT_STREAK = "currentStreak"

        private val VIBRATION_POWER = longArrayOf(0, 200, 100, 200, 100, 300)
        private val VIBRATION_EPIC = longArrayOf(0, 100, 50, 100, 50, 100, 50, 400)
    }

    override fun doWork(): Result {
        val challengeName = inputData.getString(KEY_CHALLENGE_NAME) ?: "Challenge"
        val challengeIcon = inputData.getString(KEY_CHALLENGE_ICON) ?: "\uD83C\uDFAF"
        val challengeId = inputData.getInt(KEY_CHALLENGE_ID, 0)
        val daysCompleted = inputData.getInt(KEY_DAYS_COMPLETED, 0)
        val targetDays = inputData.getInt(KEY_TARGET_DAYS, 0)
        val currentStreak = inputData.getInt(KEY_CURRENT_STREAK, 0)

        sendProNotification(
            challengeName = challengeName,
            challengeIcon = challengeIcon,
            challengeId = challengeId,
            daysCompleted = daysCompleted,
            targetDays = targetDays,
            currentStreak = currentStreak
        )

        return Result.success()
    }

    private fun sendProNotification(
        challengeName: String,
        challengeIcon: String,
        challengeId: Int,
        daysCompleted: Int,
        targetDays: Int,
        currentStreak: Int
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Challenge Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Personal challenge coach"
                enableVibration(true)
                vibrationPattern = VIBRATION_POWER
                enableLights(true)
                lightColor = Color.MAGENTA
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "CHALLENGES")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            challengeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val safeTotal = targetDays.coerceAtLeast(1)
        val safeCompleted = daysCompleted.coerceIn(0, safeTotal)
        val progress = (safeCompleted * 100 / safeTotal)
        val daysLeft = (safeTotal - safeCompleted).coerceAtLeast(0)

        val progressBar = buildString {
            val filled = (progress / 10).coerceIn(0, 10)
            repeat(filled) { append('#') }
            repeat(10 - filled) { append('-') }
        }

        val (title, body, urgency) = getEliteMessage(
            challengeName = challengeName,
            challengeIcon = challengeIcon,
            progress = progress,
            currentStreak = currentStreak,
            daysCompleted = safeCompleted,
            targetDays = safeTotal,
            daysLeft = daysLeft
        )

        val bigText = """
            $body

            Progress: [$progressBar] $progress%
            Day ${safeCompleted + 1} of $safeTotal
            Streak: $currentStreak day(s)
            Remaining: $daysLeft day(s)

            Tap to complete today's challenge step.
        """.trimIndent()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText("\uD83D\uDD25 $currentStreak day streak")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setProgress(safeTotal, safeCompleted, false)
            .setPriority(if (urgency) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(if (urgency) VIBRATION_EPIC else VIBRATION_POWER)
            .setColor(Color.MAGENTA)
            .setColorized(true)
            .addAction(android.R.drawable.ic_menu_rotate, "Mark complete", pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open challenge", pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        runCatching {
            notificationManager.notify(2000 + challengeId, notification)
        }
    }

    private fun getEliteMessage(
        challengeName: String,
        challengeIcon: String,
        progress: Int,
        currentStreak: Int,
        daysCompleted: Int,
        targetDays: Int,
        daysLeft: Int
    ): Triple<String, String, Boolean> {
        return when {
            daysCompleted == 0 -> Triple(
                "\uD83D\uDE80 $challengeIcon $challengeName - Day 1",
                "The first step starts now. Build momentum.",
                false
            )
            daysCompleted in 1..6 -> Triple(
                "$challengeIcon Day ${daysCompleted + 1} - $challengeName",
                "$daysCompleted done, $daysLeft to go. Keep the chain alive.",
                false
            )
            daysCompleted == 7 -> Triple(
                "\uD83C\uDF89 1-week milestone - $challengeName",
                "A full week complete. Keep the standard high.",
                false
            )
            progress >= 95 -> Triple(
                "\uD83C\uDFC1 Final stretch - $challengeName",
                "Only $daysLeft day(s) left. Finish strong.",
                true
            )
            progress in 75..94 -> Triple(
                "\uD83D\uDD25 Strong run - $challengeName",
                "You are above 75%. Stay locked in.",
                false
            )
            progress in 50..74 -> Triple(
                "\u26A1 Halfway point - $challengeName",
                "Past the midpoint. Keep pressure on.",
                false
            )
            progress in 25..49 -> Triple(
                "$challengeIcon Building momentum - $challengeName",
                "Progress is compounding. Continue daily.",
                false
            )
            currentStreak >= 14 -> Triple(
                "\uD83D\uDD25 $currentStreak-day streak - $challengeName",
                "Excellent consistency. Protect this streak.",
                true
            )
            currentStreak >= 7 -> Triple(
                "\uD83D\uDD25 Weekly streak - $challengeName",
                "Good weekly momentum. Keep going.",
                false
            )
            else -> Triple(
                "$challengeIcon $challengeName - Day ${daysCompleted + 1}",
                "Show up, execute, and move the needle today.",
                false
            )
        }
    }
}
