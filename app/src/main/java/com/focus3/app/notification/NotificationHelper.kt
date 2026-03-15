package com.focus3.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.Manifest
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.focus3.app.MainActivity
import java.time.LocalTime

/**
 * Notification helper used by reminder workers and schedulers.
 */
object NotificationHelper {

    // Channel IDs
    const val CHANNEL_DAILY_GOALS = "daily_goals_channel"
    const val CHANNEL_CHALLENGES = "challenges_channel"
    const val CHANNEL_ACHIEVEMENTS = "achievements_channel"
    const val CHANNEL_STREAK = "streak_channel"
    const val CHANNEL_MOTIVATION = "motivation_channel"

    // Notification IDs
    const val NOTIFICATION_DAILY = 1001
    const val NOTIFICATION_CHALLENGE = 2001
    const val NOTIFICATION_ACHIEVEMENT = 3001
    const val NOTIFICATION_STREAK = 4001
    const val NOTIFICATION_MOTIVATION = 5001
    const val NOTIFICATION_WEEKLY_RECAP = 6001

    private val VIBRATION_POWER = longArrayOf(0, 150, 80, 150)
    private val VIBRATION_URGENT = longArrayOf(0, 300, 150, 300, 150, 400)
    private val VIBRATION_EPIC = longArrayOf(0, 100, 50, 100, 50, 100, 50, 100, 50, 500)
    private val VIBRATION_SOFT = longArrayOf(0, 80, 50, 80)

    private val MORNING_POWER = listOf(
        "\uD83C\uDF05 Rise with intent. Your mission starts now.",
        "\u2600\uFE0F New day, same commitment. Execute.",
        "\uD83D\uDCAA You are up. Now move with purpose.",
        "\uD83D\uDD25 Build momentum early. Win the day.",
        "\u26A1 Start strong. Finish stronger."
    )

    private val AFTERNOON_GRIND = listOf(
        "\u23F0 Midday check: stay focused and keep moving.",
        "\uD83D\uDCBC Keep your standards high and your pace steady.",
        "\uD83C\uDFAF Three goals. One clear mission.",
        "\uD83D\uDD25 Progress beats perfection. Continue.",
        "\u26A1 Discipline now, pride later."
    )

    private val EVENING_WARRIOR = listOf(
        "\uD83C\uDF19 Final stretch. Close your loops.",
        "\u2694\uFE0F Strong finish mode is active.",
        "\uD83C\uDF1F End the day with proof of effort.",
        "\uD83D\uDD25 Give full effort while it still counts.",
        "\uD83D\uDC51 Finish what you started."
    )

    private val STREAK_DANGER_ELITE = listOf(
        "\uD83D\uDEA8 Alert: your %d-day streak is at risk tonight.",
        "\u26A0\uFE0F %d days of effort need protection now.",
        "\uD83D\uDC80 Do not lose your %d-day streak to one missed day.",
        "\uD83D\uDD25 Emergency: %d-day streak in critical condition.",
        "\u23F0 Final call: %d days of progress need action."
    )

    private val CELEBRATION_EPIC = listOf(
        "\uD83C\uDF8A Outstanding execution.",
        "\uD83D\uDD25 That is winner-level consistency.",
        "\uD83D\uDC51 Great work. Keep momentum.",
        "\uD83D\uDC8E Precision and discipline paid off.",
        "\uD83D\uDE80 Excellent progress."
    )

    private val MOTIVATIONAL_QUOTES = listOf(
        "The only bad workout is the one that did not happen. - Unknown",
        "Small daily improvements lead to major long-term results. - Unknown",
        "Discipline equals freedom. - Jocko Willink",
        "Your future is created by what you do today. - Robert Kiyosaki",
        "Success is rented, and rent is due every day. - J.J. Watt",
        "Do not count the days, make the days count. - Muhammad Ali",
        "The hard days are what make you stronger. - Aly Raisman",
        "The secret of getting ahead is getting started. - Mark Twain",
        "Be better than yesterday. - Unknown",
        "Champions train, complainers explain. - Unknown"
    )

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY_GOALS,
            "Daily Goals",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Your daily mission briefing"
            enableVibration(true)
            vibrationPattern = VIBRATION_POWER
            enableLights(true)
            lightColor = Color.CYAN
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val challengesChannel = NotificationChannel(
            CHANNEL_CHALLENGES,
            "Challenges",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Challenge progress and reminders"
            enableVibration(true)
            vibrationPattern = VIBRATION_POWER
            enableLights(true)
            lightColor = Color.MAGENTA
            setShowBadge(true)
        }

        val achievementsChannel = NotificationChannel(
            CHANNEL_ACHIEVEMENTS,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Celebrate your wins"
            enableVibration(true)
            vibrationPattern = VIBRATION_EPIC
            enableLights(true)
            lightColor = Color.YELLOW
            setShowBadge(true)
        }

        val streakChannel = NotificationChannel(
            CHANNEL_STREAK,
            "Streak Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical streak protection"
            enableVibration(true)
            vibrationPattern = VIBRATION_URGENT
            enableLights(true)
            lightColor = Color.RED
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val motivationChannel = NotificationChannel(
            CHANNEL_MOTIVATION,
            "Daily Motivation",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Inspirational quotes and reminders"
            enableVibration(true)
            vibrationPattern = VIBRATION_SOFT
            enableLights(true)
            lightColor = Color.WHITE
        }

        notificationManager.createNotificationChannels(
            listOf(dailyChannel, challengesChannel, achievementsChannel, streakChannel, motivationChannel)
        )
    }

    fun showDailyGoalReminder(
        context: Context,
        completedCount: Int = 0,
        streak: Int = 0,
        goals: List<String> = emptyList()
    ) {
        val hour = LocalTime.now().hour
        val powerMessage = when {
            hour in 5..11 -> MORNING_POWER.random()
            hour in 12..17 -> AFTERNOON_GRIND.random()
            else -> EVENING_WARRIOR.random()
        }

        val progressEmoji = when (completedCount) {
            0 -> "\u2B1C\u2B1C\u2B1C"
            1 -> "\u2705\u2B1C\u2B1C"
            2 -> "\u2705\u2705\u2B1C"
            else -> "\u2705\u2705\u2705"
        }

        val urgencyText = when {
            hour >= 21 && completedCount < 3 -> "\uD83D\uDEA8 Final hours"
            hour >= 18 && completedCount < 3 -> "\u26A1 Evening push"
            completedCount >= 3 -> "\uD83C\uDFC6 All complete"
            else -> "\uD83D\uDCAA Keep going"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("Mission status: $urgencyText")
            .setSummaryText("$completedCount/3 complete - ${if (streak > 0) "\uD83D\uDD25 $streak day streak" else "Start your streak"}")

        inboxStyle.addLine(powerMessage)
        inboxStyle.addLine("")
        inboxStyle.addLine("Today's goals")

        if (goals.isNotEmpty()) {
            goals.take(3).forEachIndexed { index, goal ->
                val status = if (index < completedCount) "\u2705" else "\u2B1C"
                val goalText = if (goal.length > 40) goal.take(40) + "..." else goal
                inboxStyle.addLine("$status $goalText")
            }
        } else {
            inboxStyle.addLine("\u2B1C Goal 1: Tap to define")
            inboxStyle.addLine("\u2B1C Goal 2: Tap to define")
            inboxStyle.addLine("\u2B1C Goal 3: Tap to define")
        }

        if (streak > 0) {
            inboxStyle.addLine("")
            inboxStyle.addLine("\uD83D\uDD25 Streak: $streak days")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "HOME")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_DAILY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_GOALS)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("$progressEmoji $urgencyText")
            .setContentText(powerMessage)
            .setSubText(if (streak > 0) "\uD83D\uDD25 $streak day streak" else null)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(if (hour >= 20 && completedCount < 3) VIBRATION_URGENT else VIBRATION_POWER)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(Color.CYAN)
            .setColorized(true)
            .setNumber((3 - completedCount).coerceAtLeast(0))
            .addAction(android.R.drawable.ic_media_play, "Open goals", pendingIntent)
            .build()

        notifySafely(context, NOTIFICATION_DAILY, notification)
    }

    fun showStreakDangerAlert(context: Context, currentStreak: Int) {
        if (currentStreak <= 0) return

        val rawMessage = STREAK_DANGER_ELITE.random()
        val message = rawMessage.replace("%d", currentStreak.toString())
        val hoursLeft = (24 - LocalTime.now().hour).coerceAtLeast(1)

        val bigText = """
            $message

            \u23F0 Approx. $hoursLeft hour(s) left today.
            \uD83D\uDCCA $currentStreak days of progress are on the line.
            \uD83D\uDC49 Tap now to protect your streak.
        """.trimIndent()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "HOME")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_STREAK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("\uD83D\uDD25 $currentStreak day streak at risk")
            .setContentText("Complete your goals now to keep momentum.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(VIBRATION_URGENT)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.RED)
            .setColorized(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_save, "Protect streak", pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open goals", pendingIntent)
            .build()

        notifySafely(context, NOTIFICATION_STREAK, notification)
    }

    fun showChallengeReminder(
        context: Context,
        challengeName: String,
        challengeIcon: String,
        daysCompleted: Int,
        totalDays: Int,
        currentStreak: Int
    ) {
        val safeTotal = totalDays.coerceAtLeast(1)
        val safeCompleted = daysCompleted.coerceIn(0, safeTotal)
        val progressPercent = (safeCompleted * 100 / safeTotal)
        val daysLeft = (safeTotal - safeCompleted).coerceAtLeast(0)

        val progressBar = buildString {
            val filled = (progressPercent / 10).coerceIn(0, 10)
            repeat(filled) { append('#') }
            repeat(10 - filled) { append('-') }
        }

        val statusMessage = when {
            progressPercent >= 95 -> "Finish line: $daysLeft day(s) left."
            progressPercent >= 75 -> "Strong pace: $daysLeft day(s) left."
            progressPercent >= 50 -> "Halfway done. Keep pressure on."
            progressPercent >= 25 -> "Good momentum. Continue daily."
            else -> "Early phase. Build consistency now."
        }

        val bigText = """
            $challengeIcon $challengeName

            $statusMessage

            Progress: [$progressBar] $progressPercent%
            Completed: $safeCompleted / $safeTotal day(s)
            Streak: $currentStreak day(s)
            Remaining: $daysLeft day(s)

            Tap to update today's progress.
        """.trimIndent()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "CHALLENGES")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_CHALLENGE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CHALLENGES)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("$challengeIcon $challengeName")
            .setContentText("$progressPercent% complete - $daysLeft day(s) left")
            .setSubText("\uD83D\uDD25 $currentStreak day streak")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setProgress(safeTotal, safeCompleted, false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(VIBRATION_POWER)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(Color.MAGENTA)
            .setColorized(true)
            .addAction(android.R.drawable.ic_menu_rotate, "Mark complete", pendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "View challenge", pendingIntent)
            .build()

        notifySafely(context, NOTIFICATION_CHALLENGE, notification)
    }

    fun showAchievementUnlocked(
        context: Context,
        achievementTitle: String,
        achievementEmoji: String,
        description: String
    ) {
        val celebration = CELEBRATION_EPIC.random()

        val bigText = """
            Achievement unlocked

            $achievementEmoji $achievementTitle
            $description

            $celebration
        """.trimIndent()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "PROFILE")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ACHIEVEMENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("$achievementEmoji $achievementTitle")
            .setContentText(celebration)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(VIBRATION_EPIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(Color.parseColor("#FFD700"))
            .setColorized(true)
            .addAction(android.R.drawable.ic_menu_share, "Share", pendingIntent)
            .build()

        notifySafely(context, NOTIFICATION_ACHIEVEMENT, notification)
    }

    fun showMotivationalQuote(context: Context) {
        val quote = MOTIVATIONAL_QUOTES.random()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_MOTIVATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Daily motivation")
            .setContentText(quote.take(56) + if (quote.length > 56) "..." else "")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$quote\n\nMake today count."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(VIBRATION_SOFT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setColor(Color.WHITE)
            .build()

        notifySafely(context, NOTIFICATION_MOTIVATION, notification)
    }

    fun showWeeklyRecap(
        context: Context,
        totalGoalsCompleted: Int,
        bestStreak: Int,
        challengesActive: Int,
        daysWithAllComplete: Int
    ) {
        val rating = when {
            daysWithAllComplete >= 7 -> "\uD83C\uDFC6 Legendary week"
            daysWithAllComplete >= 5 -> "\u2B50 Excellent week"
            daysWithAllComplete >= 3 -> "\uD83D\uDCAA Good week"
            else -> "\uD83D\uDCC8 Room to grow"
        }

        val bigText = """
            Weekly recap

            $rating

            Goals completed: $totalGoalsCompleted / 21
            Best streak: $bestStreak day(s)
            Active challenges: $challengesActive
            Perfect days: $daysWithAllComplete / 7
        """.trimIndent()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "ANALYTICS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_WEEKLY_RECAP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Weekly recap: $rating")
            .setContentText("$totalGoalsCompleted goals - $daysWithAllComplete perfect days")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(VIBRATION_POWER)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(Color.CYAN)
            .addAction(android.R.drawable.ic_menu_view, "Open analytics", pendingIntent)
            .build()

        notifySafely(context, NOTIFICATION_WEEKLY_RECAP, notification)
    }

    fun showMilestoneCelebration(context: Context, days: Int) {
        val (emoji, title, description) = when (days) {
            3 -> Triple("\uD83C\uDF31", "3-day spark", "You have started strong. Keep going.")
            7 -> Triple("\uD83D\uDDD3\uFE0F", "1-week streak", "Seven days of consistent execution.")
            14 -> Triple("\uD83C\uDF1F", "2-week streak", "Your discipline is compounding.")
            21 -> Triple("\u26A1", "21-day habit", "You built a meaningful habit cycle.")
            30 -> Triple("\uD83D\uDC51", "1-month champion", "Thirty days of reliable action.")
            50 -> Triple("\uD83D\uDD25", "50-day run", "Sustained consistency unlocked.")
            75 -> Triple("\uD83D\uDC8E", "75-day diamond", "Pressure turned into progress.")
            100 -> Triple("\uD83C\uDFC6", "100-day centurion", "Elite consistency milestone achieved.")
            200 -> Triple("\uD83E\uDD81", "200-day lion", "Long-term execution at a high level.")
            365 -> Triple("\uD83C\uDF8A", "1-year streak", "A full year of discipline and follow-through.")
            else -> Triple("\u2B50", "$days-day milestone", "Every day counted. Keep building.")
        }

        showAchievementUnlocked(
            context = context,
            achievementTitle = title,
            achievementEmoji = emoji,
            description = description
        )
    }

    fun cancelStreakDanger(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_STREAK)
    }

    private fun notifySafely(
        context: Context,
        notificationId: Int,
        notification: android.app.Notification
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
