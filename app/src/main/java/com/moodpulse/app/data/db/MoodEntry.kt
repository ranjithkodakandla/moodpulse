package com.moodpulse.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "mood_entries")
@TypeConverters(FeelingTagConverter::class)
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mood: Int,                   // 1=Very Low, 2=Low, 3=Okay, 4=Good, 5=Great
    val timestampMs: Long,           // epoch millis in UTC
    val timezoneId: String,          // e.g. "America/New_York"
    val feelingTags: List<String> = emptyList(),
    val note: String? = null,
    val source: String = "app",      // "app" or "widget"
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

class FeelingTagConverter {
    @TypeConverter
    fun fromList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split(",").map { it.trim() }
}
