package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_context_prefs")
data class SmartContextPreferences(
    @PrimaryKey val id: Int = 1,
    val monitoringEnabled: Boolean = false,
    val excludedPackagesJson: String = "[]",
    val quietHoursStart: Int = 22,   // hour 0-23
    val quietHoursEnd: Int = 8,
    val suggestionNotificationsEnabled: Boolean = false,
    val spendingAlertsEnabled: Boolean = false,
    val spendingThreshold: Double = 2000.0,
    val lastSuggestionAt: Long = 0L,
    val suggestionCooldownMinutes: Int = 120
)
