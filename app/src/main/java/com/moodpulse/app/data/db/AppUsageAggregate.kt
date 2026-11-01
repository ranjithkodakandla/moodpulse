package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_aggregate")
data class AppUsageAggregate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // ISO date "2026-11-01"
    val packageName: String,
    val appCategory: String,    // "SOCIAL", "SHOPPING", "ENTERTAINMENT", etc.
    val totalMinutes: Int,
    val sessionCount: Int,
    val timePeriod: String      // "MORNING","AFTERNOON","EVENING","NIGHT"
)
