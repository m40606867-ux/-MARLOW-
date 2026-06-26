package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marlow_chats")
data class MarlowChat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "user" or "marlow"
    val text: String,
    val isVoice: Boolean = false
)

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // YYYY-MM-DD
    val prayerName: String, // Fajr, Dhuhr, Asr, Maghrib, Isha
    val isCompleted: Boolean,
    val completedTime: Long? = null
)

@Entity(tableName = "habit_trackers")
data class HabitTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitName: String,
    val description: String,
    val startTimestamp: Long = System.currentTimeMillis(),
    val isOpponentActive: Boolean = true,
    val lastCravingTimestamp: Long? = null,
    val cravingCount: Int = 0,
    val levelOfAddiction: Int = 3 // 1 (low) to 5 (high)
)
