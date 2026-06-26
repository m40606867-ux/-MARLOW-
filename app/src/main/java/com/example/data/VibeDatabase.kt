package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VibeCheck::class,
        MarlowChat::class,
        PrayerLog::class,
        HabitTracker::class,
        User::class
    ],
    version = 3,
    exportSchema = false
)
abstract class VibeDatabase : RoomDatabase() {
    abstract fun vibeCheckDao(): VibeCheckDao
    abstract fun marlowDao(): MarlowDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: VibeDatabase? = null

        fun getDatabase(context: Context): VibeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeDatabase::class.java,
                    "vibe_companion_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
