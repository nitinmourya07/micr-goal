# 🎯 Focus3 — Micro Goal Tracker

<p align="center">
  <strong>A premium Android app that helps you achieve your goals — 3 at a time.</strong>
  <br/>
  <em>Built with Jetpack Compose • Material 3 • Room • Hilt • Kotlin</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" alt="Android" />
  <img src="https://img.shields.io/badge/Min%20SDK-26-blue?style=for-the-badge" alt="Min SDK 26" />
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=for-the-badge" alt="Target SDK 35" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.0-purple?style=for-the-badge&logo=kotlin" alt="Kotlin 2.0" />
  <img src="https://img.shields.io/badge/Compose-Material%203-teal?style=for-the-badge" alt="Material 3" />
</p>

---

## 📖 About

**Focus3** is a micro-goal tracking Android application designed around the philosophy that *less is more*. Instead of overwhelming to-do lists, Focus3 limits you to **3 daily goals** — forcing you to prioritize what truly matters.

The app features a beautifully crafted dark-themed UI with glassmorphism effects, confetti celebrations, streak tracking, challenge management, and a full-featured notes system — all wrapped in a premium, modern experience.

---

## ✨ Key Features

### 🏠 Daily Goals
- **3 focused goals per day** — write, track, and complete your most important tasks
- **Smart greetings** based on time of day (Morning, Afternoon, Evening, Night)
- **Animated progress ring** and visual completion indicators
- **Celebration animations** with confetti when all goals are completed
- **Share your daily wins** with friends

### 🔥 Streak System
- **Daily streak tracking** to build consistency
- **Longest streak record** to push personal bests
- **Grace days** to forgive occasional misses
- **Custom milestones** — set your own streak goals
- **Streak journey visualization** with progress map

### 🏆 Challenges
- **Create custom challenges** with target durations
- **Track progress** with visual indicators
- **Edit & manage** active challenges
- **Challenge reminders** via push notifications
- **Journey view** for each challenge

### 📝 Notes
- **Full-featured notes system** with categories
- **Pin important notes** to the top
- **Search functionality** across all notes
- **Rich note editor** with category support

### 📅 Calendar
- **Monthly calendar view** of your completion history
- **Add notes to specific dates** for journaling
- **Visual heatmap** showing productive days
- **Streak statistics** at a glance

### 📊 Analytics
- **Completion history** with visual charts
- **Task history** tracking over time
- **Performance insights** and trends

### ⚙️ Additional Features
- **Onboarding tutorial** for new users
- **Profile customization** (avatar & name)
- **Data export/import** (JSON format)
- **Backup & restore** support
- **Daily goal reminders** via AlarmManager
- **Boot-persistent alarms** — reminders survive device restarts
- **Haptic feedback** for tactile interaction
- **Lottie animations** for premium celebrations
- **Glassmorphism UI components** for modern aesthetics

---

## 🏗️ Architecture

The project follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
app/src/main/java/com/focus3/app/
├── 📁 data/
│   ├── dao/              # Room DAOs (Task, Challenge, Note, CalendarNote)
│   ├── database/         # Room Database configuration
│   ├── model/            # Data classes (DailyTask, Challenge, Note, etc.)
│   ├── repository/       # Repository pattern for data access
│   └── util/             # Database utilities & stats
├── 📁 di/
│   └── DatabaseModule.kt # Hilt dependency injection module
├── 📁 notification/
│   ├── BootReceiver.kt           # Reschedules alarms after reboot
│   ├── ChallengeAlarmScheduler.kt # Challenge reminder scheduling
│   ├── DailyGoalAlarmScheduler.kt # Daily goal reminder scheduling
│   └── NotificationHelper.kt     # Notification creation utilities
├── 📁 ui/
│   ├── components/       # Reusable UI components
│   │   ├── CelebrationSystem.kt  # Milestone celebrations
│   │   ├── ConfettiOverlay.kt    # Confetti particle effects
│   │   ├── Glassmorphism.kt      # Glass-effect UI boxes
│   │   ├── GoalCard.kt           # Goal input/display cards
│   │   ├── ProgressRing.kt       # Circular progress indicator
│   │   ├── ShimmerEffect.kt      # Loading shimmer animation
│   │   └── StreakBadge.kt         # Streak display badge
│   ├── screens/          # App screens
│   │   ├── MainScreen.kt         # Main container + navigation
│   │   ├── AnalyticsScreen.kt    # Analytics dashboard
│   │   ├── CalendarScreen.kt     # Calendar view
│   │   ├── ChallengeScreen.kt    # Challenge management
│   │   ├── EditChallengeScreen.kt # Challenge editor
│   │   ├── CustomMilestoneScreen.kt # Custom milestone setup
│   │   ├── GuidedTutorial.kt     # Onboarding tutorial
│   │   ├── NotesScreen.kt        # Notes listing
│   │   ├── NoteEditorScreen.kt   # Note editor
│   │   ├── OnboardingScreen.kt   # First-time onboarding
│   │   ├── ProfileScreen.kt      # User profile
│   │   ├── SettingsScreen.kt     # App settings
│   │   └── StreakJourneyScreen.kt # Streak visualization
│   ├── theme/            # Material 3 theming
│   │   ├── Color.kt              # Color definitions
│   │   ├── Theme.kt              # App theme configuration
│   │   └── Type.kt               # Typography system
│   └── viewmodel/        # ViewModels
│       ├── MainViewModel.kt      # Primary app ViewModel
│       └── NotesViewModel.kt     # Notes feature ViewModel
├── 📁 util/
│   ├── BackupManager.kt          # Data backup utilities
│   ├── DataExportHelper.kt       # JSON export/import
│   ├── HapticUtils.kt            # Haptic feedback helpers
│   ├── QuotesData.kt             # Motivational quotes
│   └── ShareUtils.kt             # Social sharing utilities
├── Focus3Application.kt          # Application class (Hilt entry)
└── MainActivity.kt               # Single Activity entry point
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.0.0 |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Architecture** | MVVM + Repository Pattern |
| **Database** | Room (SQLite) |
| **Dependency Injection** | Hilt (Dagger) |
| **Background Tasks** | WorkManager + AlarmManager |
| **Animations** | Lottie + Compose Animations |
| **Build System** | Gradle (Kotlin DSL) |
| **Symbol Processing** | KSP (Kotlin Symbol Processing) |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** or higher
- **Android SDK 35** (compileSdk)
- **Minimum device**: Android 8.0 (API 26)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/nitinmourya07/micr-goal.git
   cd micr-goal
   ```

2. **Open in Android Studio**
   - File → Open → Select the `micr-goal` folder
   - Wait for Gradle sync to complete

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or simply press ▶️ **Run** in Android Studio with an emulator or connected device.

---

## 📱 Screens Overview

| Screen | Description |
|--------|------------|
| **Home** | Daily 3-goal tracker with progress ring, streak badge, greeting |
| **Challenges** | Create & manage long-term challenges with progress tracking |
| **Analytics** | Completion history charts and performance insights |
| **Notes** | Categorized note-taking with search and pin support |
| **Calendar** | Monthly completion heatmap with date-specific notes |
| **Streak Journey** | Visual streak progress with milestone markers |
| **Profile** | Avatar & name customization |
| **Settings** | Data export/import, backup, and app preferences |

---

## 🔔 Notifications

Focus3 uses **AlarmManager** for precise, time-based reminders:

- **Daily Goal Reminders** — configurable time to remind you to set/complete goals
- **Challenge Reminders** — per-challenge notification scheduling
- **Boot Persistence** — alarms are automatically rescheduled after device restart via `BootReceiver`

---

## 📦 Build Variants

| Variant | Minification | Description |
|---------|-------------|-------------|
| `debug` | ❌ Off | Fast builds for development |
| `release` | ✅ R8 + ProGuard | Optimized, minified, resource-shrunk |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is developed as a **Final Year Project**.

---

## 👤 Author

**Nitin Mourya**

- GitHub: [@nitinmourya07](https://github.com/nitinmourya07)

---

<p align="center">
  Made with ❤️ and Kotlin
  <br/>
  <strong>Focus on 3. Achieve everything.</strong>
</p>
