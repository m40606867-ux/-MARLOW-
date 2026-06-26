package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarlowDao {
    // Chat Operations
    @Query("SELECT * FROM marlow_chats ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<MarlowChat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MarlowChat)

    @Query("DELETE FROM marlow_chats")
    suspend fun clearChatHistory()

    // Prayer Operations
    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString")
    fun getPrayersForDate(dateString: String): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerLog(log: PrayerLog)

    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString AND prayerName = :prayerName LIMIT 1")
    suspend fun getPrayerLog(dateString: String, prayerName: String): PrayerLog?

    // Habit Operations
    @Query("SELECT * FROM habit_trackers ORDER BY startTimestamp DESC")
    fun getAllHabits(): Flow<List<HabitTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitTracker)

    @Update
    suspend fun updateHabit(habit: HabitTracker)

    @Delete
    suspend fun deleteHabit(habit: HabitTracker)
}
