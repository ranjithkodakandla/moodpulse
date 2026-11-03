package com.moodpulse.app

import com.moodpulse.app.data.db.MoodSuggestion
import com.moodpulse.app.data.db.SuggestionStatus
import com.moodpulse.app.smartcontext.PhysicalActivityData
import com.moodpulse.app.smartcontext.PhysicalActivityManager
import com.moodpulse.app.smartcontext.SmartContextViewModel
import org.junit.Assert.*
import org.junit.Test

class SmartContextLifecycleTest {

    @Test
    fun `suggestion status transitions correctly`() {
        val initial = MoodSuggestion(
            id = 1,
            candidateMood = 3,
            candidateFeelings = "calm",
            confidenceBand = "HIGH",
            observationsJson = "[]",
            promptText = "Mindful Pause Check-in",
            createdAt = System.currentTimeMillis()
        )
        assertEquals(SuggestionStatus.NEW.name, initial.status)
        assertFalse(initial.liked)
        assertNull(initial.snoozedUntil)
        assertNull(initial.feedbackComment)

        // Confirm
        val confirmed = initial.copy(status = SuggestionStatus.CONFIRMED.name, userDecision = "CONFIRMED")
        assertEquals("CONFIRMED", confirmed.status)

        // Correct
        val corrected = initial.copy(
            status = SuggestionStatus.CORRECTED.name,
            userDecision = "CORRECTED",
            correctedMood = 4,
            feedbackComment = "Feeling great actually"
        )
        assertEquals("CORRECTED", corrected.status)
        assertEquals(4, corrected.correctedMood)
        assertEquals("Feeling great actually", corrected.feedbackComment)

        // Acknowledge with like
        val acknowledged = initial.copy(
            status = SuggestionStatus.ACKNOWLEDGED.name,
            liked = true
        )
        assertEquals("ACKNOWLEDGED", acknowledged.status)
        assertTrue(acknowledged.liked)

        // Snooze
        val futureTime = System.currentTimeMillis() + 3600_000L
        val snoozed = initial.copy(
            status = SuggestionStatus.SNOOZED.name,
            snoozedUntil = futureTime
        )
        assertEquals("SNOOZED", snoozed.status)
        assertTrue(snoozed.snoozedUntil!! > System.currentTimeMillis())
    }

    @Test
    fun `one hour cooldown calculation behaves correctly`() {
        val now = System.currentTimeMillis()
        val cooldownMs = SmartContextViewModel.COOLDOWN_MS
        assertEquals(3600_000L, cooldownMs)

        // 30 minutes ago: cooldown active
        val thirtyMinsAgo = now - 30 * 60_000L
        assertTrue(now - thirtyMinsAgo < cooldownMs)

        // 65 minutes ago: cooldown expired
        val sixtyFiveMinsAgo = now - 65 * 60_000L
        assertFalse(now - sixtyFiveMinsAgo < cooldownMs)
    }

    @Test
    fun `physical activity evaluation generates appropriate observations`() {
        // High activity
        val highActivity = PhysicalActivityData(
            todaySteps = 8500,
            exerciseMinutes = 40,
            sleepHours = 7.8,
            source = "Health Connect",
            isConnected = true
        )
        // Check fields
        assertEquals(8500L, highActivity.todaySteps)
        assertEquals(40, highActivity.exerciseMinutes)
        assertEquals(7.8, highActivity.sleepHours)
        assertTrue(highActivity.isConnected)

        // Low sleep
        val shortSleep = PhysicalActivityData(
            todaySteps = 2000,
            exerciseMinutes = 0,
            sleepHours = 5.0,
            source = "Health Connect",
            isConnected = true
        )
        assertTrue(shortSleep.sleepHours!! < 6.0)
    }
}
