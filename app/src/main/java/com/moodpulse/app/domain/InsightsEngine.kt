package com.moodpulse.app.domain

import com.moodpulse.app.data.db.MoodEntry
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class WeeklyInsights(
    val weekStartMs: Long,
    val weekEndMs: Long,
    val totalEntries: Int,
    val mostFrequentMood: Mood?,
    val moodDistribution: Map<Mood, Int>,
    val averageMood: Float,
    val morningAvg: Float?,
    val afternoonAvg: Float?,
    val eveningAvg: Float?,
    val nightAvg: Float?,
    val peakTimeOfDay: TimeOfDay?,
    val dailyEntries: Map<Int, Int>,      // dayOfWeek(1=Mon) -> count
    val prevWeekAverageMood: Float?,
    val moodTrend: MoodTrend,
    val streakDays: Int,
    val hasEnoughData: Boolean
)

enum class MoodTrend { IMPROVING, DECLINING, STABLE, INSUFFICIENT_DATA }

object InsightsEngine {

    fun computeWeeklyInsights(
        currentWeekEntries: List<MoodEntry>,
        prevWeekEntries: List<MoodEntry>,
        allEntries: List<MoodEntry>,
        weekStartMs: Long,
        weekEndMs: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): WeeklyInsights {
        val hasEnoughData = currentWeekEntries.size >= 3

        if (!hasEnoughData) {
            return WeeklyInsights(
                weekStartMs = weekStartMs,
                weekEndMs = weekEndMs,
                totalEntries = currentWeekEntries.size,
                mostFrequentMood = null,
                moodDistribution = emptyMap(),
                averageMood = 0f,
                morningAvg = null,
                afternoonAvg = null,
                eveningAvg = null,
                nightAvg = null,
                peakTimeOfDay = null,
                dailyEntries = emptyMap(),
                prevWeekAverageMood = null,
                moodTrend = MoodTrend.INSUFFICIENT_DATA,
                streakDays = computeStreak(allEntries, zoneId),
                hasEnoughData = false
            )
        }

        val distribution = currentWeekEntries
            .groupBy { Mood.fromValue(it.mood) }
            .mapValues { it.value.size }

        val mostFrequent = distribution.maxByOrNull { it.value }?.key
        val avgMood = currentWeekEntries.map { it.mood }.average().toFloat()

        val byTimeOfDay = currentWeekEntries.groupBy { entry ->
            val hour = Instant.ofEpochMilli(entry.timestampMs)
                .atZone(zoneId).hour
            TimeOfDay.fromHour(hour)
        }

        fun avgForTod(tod: TimeOfDay) = byTimeOfDay[tod]
            ?.map { it.mood }?.average()?.toFloat()

        val morningAvg = avgForTod(TimeOfDay.MORNING)
        val afternoonAvg = avgForTod(TimeOfDay.AFTERNOON)
        val eveningAvg = avgForTod(TimeOfDay.EVENING)
        val nightAvg = avgForTod(TimeOfDay.NIGHT)

        val peakTod = listOfNotNull(
            morningAvg?.let { TimeOfDay.MORNING to it },
            afternoonAvg?.let { TimeOfDay.AFTERNOON to it },
            eveningAvg?.let { TimeOfDay.EVENING to it },
            nightAvg?.let { TimeOfDay.NIGHT to it }
        ).maxByOrNull { it.second }?.first

        val dailyEntries = currentWeekEntries.groupBy { entry ->
            Instant.ofEpochMilli(entry.timestampMs).atZone(zoneId).dayOfWeek.value
        }.mapValues { it.value.size }

        val prevAvg = if (prevWeekEntries.size >= 3)
            prevWeekEntries.map { it.mood }.average().toFloat()
        else null

        val trend = when {
            prevAvg == null -> MoodTrend.STABLE
            avgMood - prevAvg > 0.5f -> MoodTrend.IMPROVING
            avgMood - prevAvg < -0.5f -> MoodTrend.DECLINING
            else -> MoodTrend.STABLE
        }

        return WeeklyInsights(
            weekStartMs = weekStartMs,
            weekEndMs = weekEndMs,
            totalEntries = currentWeekEntries.size,
            mostFrequentMood = mostFrequent,
            moodDistribution = distribution,
            averageMood = avgMood,
            morningAvg = morningAvg,
            afternoonAvg = afternoonAvg,
            eveningAvg = eveningAvg,
            nightAvg = nightAvg,
            peakTimeOfDay = peakTod,
            dailyEntries = dailyEntries,
            prevWeekAverageMood = prevAvg,
            moodTrend = trend,
            streakDays = computeStreak(allEntries, zoneId),
            hasEnoughData = true
        )
    }

    fun computeStreak(allEntries: List<MoodEntry>, zoneId: ZoneId): Int {
        if (allEntries.isEmpty()) return 0
        val today = Instant.now().atZone(zoneId).toLocalDate()
        val entryDays = allEntries
            .map { Instant.ofEpochMilli(it.timestampMs).atZone(zoneId).toLocalDate() }
            .toSortedSet(compareByDescending { it })

        var streak = 0
        var checkDate = today
        while (entryDays.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        // If no entry today, check if yesterday continues the streak
        if (streak == 0 && entryDays.contains(today.minusDays(1))) {
            checkDate = today.minusDays(1)
            while (entryDays.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
        }
        return streak
    }

    fun generateObservation(insights: WeeklyInsights): String {
        if (!insights.hasEnoughData) {
            val moodLabel = insights.mostFrequentMood?.label?.lowercase()
            return when (insights.totalEntries) {
                0 -> "Tap \"+ Log Mood\" to capture your first check-in. The on-device Gemma AI will synthesize your patterns and digital habits here."
                1 -> "You started the week feeling $moodLabel. Every log builds your baseline — keep checking in to uncover how sleep and screen habits shape your mood."
                else -> "${insights.totalEntries} check-ins recorded so far. Your patterns are taking shape, reflecting a steady baseline. One more check-in will unlock full weekly trends."
            }
        }

        val moodLabel = insights.mostFrequentMood?.label ?: "balanced"
        val trendNote = when (insights.moodTrend) {
            MoodTrend.IMPROVING -> " Your overall mood trajectory has lifted compared to last week."
            MoodTrend.DECLINING -> " Mood has dipped slightly compared to last week — remember to honor your downtime."
            MoodTrend.STABLE -> " Your emotional rhythm has remained steady and consistent."
            MoodTrend.INSUFFICIENT_DATA -> ""
        }

        val peakNote = insights.peakTimeOfDay?.let {
            " You tend to feel most centered in the ${it.label.lowercase()}."
        } ?: ""

        return "Your predominant feeling this week was $moodLabel.$trendNote$peakNote"
    }
}
