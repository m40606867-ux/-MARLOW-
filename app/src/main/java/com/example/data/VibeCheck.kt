package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vibe_checks")
data class VibeCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val inputPrompt: String,
    val moodSlider: Float,
    val energySlider: Float,
    val positivityScore: Int,
    val dominantEmotion: String,
    val aiInsight: String,
    val activityIdea: String,
    val colorTheme: String
)
