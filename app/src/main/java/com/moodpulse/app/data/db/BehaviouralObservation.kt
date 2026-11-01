package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "behavioural_observation")
data class BehaviouralObservation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val observationType: String,  // "HIGH_SHOPPING","HIGH_SWITCHING","LATE_NIGHT", etc.
    val dateTime: Long,
    val baselineValue: Float,
    val currentValue: Float,
    val evidenceJson: String,     // JSON array of observation strings
    val createdAt: Long = System.currentTimeMillis()
)
