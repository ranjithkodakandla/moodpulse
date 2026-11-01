package com.moodpulse.app

import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.InsightsEngine
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.domain.MoodTrend
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class InsightsEngineTest {

    private val zone = ZoneId.of("UTC")

    private fun entryAt(moodValue: Int, epochMs: Long): MoodEntry = MoodEntry(
        mood = moodValue,
        timestampMs = epochMs,
        timezoneId = "UTC"
    )

    private fun entryAtDate(moodValue: Int, date: LocalDate, hour: Int = 10): MoodEntry {
        val epochMs = date.atTime(hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        return entryAt(moodValue, epochMs)
    }

    private fun weekRange(): Pair<Long, Long> {
        val now = Instant.now()
        val weekStart = LocalDate.now(zone).atStartOfDay().toInstant(ZoneOffset.UTC)
        val weekEnd = weekStart.plusSeconds(7 * 24 * 3600)
        return weekStart.toEpochMilli() to weekEnd.toEpochMilli()
    }

    @Test
    fun `insufficient data when fewer than 3 entries`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val entries = listOf(
            entryAtDate(4, today),
            entryAtDate(3, today)
        )
        val insights = InsightsEngine.computeWeeklyInsights(entries, emptyList(), entries, start, end, zone)
        assertFalse(insights.hasEnoughData)
        assertEquals(MoodTrend.INSUFFICIENT_DATA, insights.moodTrend)
    }

    @Test
    fun `computes average mood correctly`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val entries = listOf(
            entryAtDate(2, today, 9),
            entryAtDate(4, today, 14),
            entryAtDate(3, today, 20)
        )
        val insights = InsightsEngine.computeWeeklyInsights(entries, emptyList(), entries, start, end, zone)
        assertTrue(insights.hasEnoughData)
        assertEquals(3f, insights.averageMood, 0.01f)
    }

    @Test
    fun `identifies most frequent mood`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val entries = listOf(
            entryAtDate(4, today, 9),
            entryAtDate(4, today, 14),
            entryAtDate(3, today, 20)
        )
        val insights = InsightsEngine.computeWeeklyInsights(entries, emptyList(), entries, start, end, zone)
        assertEquals(Mood.GOOD, insights.mostFrequentMood)
    }

    @Test
    fun `detects improving trend`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val currentWeek = listOf(
            entryAtDate(4, today, 9),
            entryAtDate(5, today, 14),
            entryAtDate(4, today, 20)
        )
        val prevWeek = listOf(
            entryAtDate(2, today.minusDays(7), 9),
            entryAtDate(2, today.minusDays(7), 14),
            entryAtDate(3, today.minusDays(7), 20)
        )
        val insights = InsightsEngine.computeWeeklyInsights(currentWeek, prevWeek, currentWeek + prevWeek, start, end, zone)
        assertEquals(MoodTrend.IMPROVING, insights.moodTrend)
    }

    @Test
    fun `detects declining trend`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val currentWeek = listOf(
            entryAtDate(2, today, 9),
            entryAtDate(1, today, 14),
            entryAtDate(2, today, 20)
        )
        val prevWeek = listOf(
            entryAtDate(4, today.minusDays(7), 9),
            entryAtDate(5, today.minusDays(7), 14),
            entryAtDate(4, today.minusDays(7), 20)
        )
        val insights = InsightsEngine.computeWeeklyInsights(currentWeek, prevWeek, currentWeek + prevWeek, start, end, zone)
        assertEquals(MoodTrend.DECLINING, insights.moodTrend)
    }

    @Test
    fun `streak is 0 when no entries`() {
        assertEquals(0, InsightsEngine.computeStreak(emptyList(), zone))
    }

    @Test
    fun `streak counts consecutive days`() {
        val today = LocalDate.now(zone)
        val entries = listOf(
            entryAtDate(3, today),
            entryAtDate(4, today.minusDays(1)),
            entryAtDate(3, today.minusDays(2))
        )
        val streak = InsightsEngine.computeStreak(entries, zone)
        assertEquals(3, streak)
    }

    @Test
    fun `streak breaks on missing day`() {
        val today = LocalDate.now(zone)
        val entries = listOf(
            entryAtDate(3, today),
            entryAtDate(4, today.minusDays(2)) // gap on day 1
        )
        val streak = InsightsEngine.computeStreak(entries, zone)
        assertEquals(1, streak)
    }

    @Test
    fun `generates helpful message for insufficient data`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val entries = listOf(entryAtDate(4, today))
        val insights = InsightsEngine.computeWeeklyInsights(entries, emptyList(), entries, start, end, zone)
        val obs = InsightsEngine.generateObservation(insights)
        assertTrue(obs.isNotBlank())
        assertFalse(obs.contains("null"))
    }

    @Test
    fun `generates observation for sufficient data`() {
        val (start, end) = weekRange()
        val today = LocalDate.now(zone)
        val entries = (0..4).map { entryAtDate(4, today.minusDays(it.toLong()), 10 + it) }
        val insights = InsightsEngine.computeWeeklyInsights(entries, emptyList(), entries, start, end, zone)
        val obs = InsightsEngine.generateObservation(insights)
        assertTrue(obs.contains("Good"))
    }
}
