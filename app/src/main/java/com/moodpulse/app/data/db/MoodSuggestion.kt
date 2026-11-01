package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_suggestion")
data class MoodSuggestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val candidateMood: Int,           // 1-5 using existing Mood enum values
    val candidateFeelings: String,    // JSON array of FeelingTag names
    val confidenceBand: String,       // "LOW","MEDIUM","HIGH"
    val observationsJson: String,     // JSON array of strings
    val promptText: String,           // what to show the user
    val createdAt: Long = System.currentTimeMillis(),
    val userDecision: String? = null, // "CONFIRMED","CORRECTED","DISMISSED",null
    val correctedMood: Int? = null,
    val correctedFeelings: String? = null
)
