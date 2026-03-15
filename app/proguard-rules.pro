# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ==================== Room ====================
# Keep Room entities (annotated with @Entity)
-keep class com.focus3.app.data.model.** { *; }

# Keep Room DAOs (annotated with @Dao)
-keep interface com.focus3.app.data.dao.** { *; }

# Keep Room Database
-keep class com.focus3.app.data.database.** { *; }

# Room generated code
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}

# ==================== Hilt / Dagger ====================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ==================== WorkManager ====================
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class androidx.hilt.work.** { *; }

# Keep HiltWorker annotated classes
-keep @androidx.hilt.work.HiltWorker class * { *; }

# ==================== Compose ====================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep @Immutable and @Stable annotated classes
-keep @androidx.compose.runtime.Immutable class * { *; }
-keep @androidx.compose.runtime.Stable class * { *; }

# ==================== Kotlin ====================
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep data classes
-keepclassmembers class * {
    public <init>(...);
}

# ==================== JSON (org.json) ====================
-keep class org.json.** { *; }

# ==================== BroadcastReceivers ====================
-keep class com.focus3.app.notification.** { *; }

# ==================== Misc ====================
# Keep BuildConfig
-keep class com.focus3.app.BuildConfig { *; }

# Keep R8 from stripping debugging info in release
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
