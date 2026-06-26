package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VibeCheckDao {
    @Query("SELECT * FROM vibe_checks ORDER BY timestamp DESC")
    fun getAllVibeChecks(): Flow<List<VibeCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVibeCheck(vibe: VibeCheck)

    @Delete
    suspend fun deleteVibeCheck(vibe: VibeCheck)

    @Query("DELETE FROM vibe_checks")
    suspend fun clearAll()
}
