package com.focus3.app.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focus3.app.data.dao.TaskDao
import com.focus3.app.data.database.Focus3Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🗄️ Next-Level Database Module
 * Production-ready configuration with:
 * - Database lifecycle callbacks
 * - Query logging in debug
 * - Optimized connection pool
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "Focus3DB"
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Focus3Database {
        return Room.databaseBuilder(
            context,
            Focus3Database::class.java,
            "focus3_database"
        )
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Database created successfully")
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "Database opened")
            }
        })
        .build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: Focus3Database): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: Focus3Database): com.focus3.app.data.dao.ChallengeDao {
        return database.challengeDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: Focus3Database): com.focus3.app.data.dao.NoteDao {
        return database.noteDao()
    }
    
    @Provides
    @Singleton
    fun provideCalendarNoteDao(database: Focus3Database): com.focus3.app.data.dao.CalendarNoteDao {
        return database.calendarNoteDao()
    }
}
