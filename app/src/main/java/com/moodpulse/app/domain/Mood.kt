package com.moodpulse.app.domain

enum class Mood(
    val value: Int,
    val label: String,
    val shortLabel: String,
    val emoji: String,
    val description: String
) {
    VERY_LOW(1, "Very Low", "Very Low", "😩", "Struggling, heavy, really down"),
    LOW(2, "Low", "Low", "😔", "Flat, drained, not great"),
    OKAY(3, "Okay", "Okay", "😐", "Neutral, getting by, fine"),
    GOOD(4, "Good", "Good", "🙂", "Positive, steady, content"),
    GREAT(5, "Great", "Great", "😊", "Energised, happy, excellent");

    companion object {
        fun fromValue(value: Int): Mood = entries.first { it.value == value }
        fun fromValueOrNull(value: Int): Mood? = entries.firstOrNull { it.value == value }
    }
}

enum class FeelingTag(val label: String, val emoji: String) {
    CALM("Calm", "🌿"),
    HAPPY("Happy", "✨"),
    ANXIOUS("Anxious", "😰"),
    STRESSED("Stressed", "😤"),
    TIRED("Tired", "😴"),
    ENERGISED("Energised", "⚡"),
    FOCUSED("Focused", "🎯"),
    FRUSTRATED("Frustrated", "😣");

    companion object {
        fun fromLabel(label: String): FeelingTag? = entries.firstOrNull { it.label == label }
    }
}

enum class TimeOfDay(val label: String, val hourRange: IntRange) {
    MORNING("Morning", 5..11),
    AFTERNOON("Afternoon", 12..16),
    EVENING("Evening", 17..20),
    NIGHT("Night", 21..4);   // handles wrap-around in logic

    companion object {
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            in 17..20 -> EVENING
            else -> NIGHT
        }
    }
}
