package com.example.data

import kotlinx.coroutines.flow.Flow

class VibeRepository(private val vibeCheckDao: VibeCheckDao) {
    val allVibeChecks: Flow<List<VibeCheck>> = vibeCheckDao.getAllVibeChecks()

    suspend fun insert(vibe: VibeCheck) {
        vibeCheckDao.insertVibeCheck(vibe)
    }

    suspend fun delete(vibe: VibeCheck) {
        vibeCheckDao.deleteVibeCheck(vibe)
    }

    suspend fun clearAll() {
        vibeCheckDao.clearAll()
    }
}
