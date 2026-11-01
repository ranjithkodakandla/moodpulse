package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_event")
data class SpendingEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String = "INR",
    val merchant: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String,       // "MANUAL","NOTIFICATION_DETECTED"
    val isPlanned: Boolean = false,
    val moodAtTime: Int? = null,
    val userDecision: String? = null  // "PAUSED","CONTINUED","SAVED_FOR_LATER"
)
