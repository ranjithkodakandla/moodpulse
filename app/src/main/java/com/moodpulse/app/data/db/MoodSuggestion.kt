package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lifecycle states for Smart Context proactive check-in suggestions.
 */
enum class SuggestionStatus {
    NEW,
    SHOWN,
    ACKNOWLEDGED,
    CONFIRMED,
    CORRECTED,
    DISMISSED,
    SNOOZED,
    EXPIRED
}

@Entity(tableName = "mood_suggestion")
data class MoodSuggestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val candidateMood: Int,           // 1-5 using existing Mood enum values
    val candidateFeelings: String,    // JSON array of FeelingTag names
    val confidenceBand: String,       // "LOW","MEDIUM","HIGH"
    val observationsJson: String,     // JSON array of strings
    val promptText: String,           // what to show the user
    val createdAt: Long = System.currentTimeMillis(),
    val userDecision: String? = null, // legacy decision field preserved for backward compatibility
    val correctedMood: Int? = null,
    val correctedFeelings: String? = null,
    val status: String = SuggestionStatus.NEW.name,
    val feedbackComment: String? = null,
    val lastInteractedAt: Long? = null,
    val snoozedUntil: Long? = null,
    val liked: Boolean = false
)
