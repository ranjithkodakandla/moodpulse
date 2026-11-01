package com.moodpulse.app

import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import com.moodpulse.app.domain.TimeOfDay
import org.junit.Assert.*
import org.junit.Test

class MoodTest {

    @Test
    fun `all mood values are unique`() {
        val values = Mood.entries.map { it.value }
        assertEquals(values.distinct().size, values.size)
    }

    @Test
    fun `mood values are 1 through 5`() {
        val values = Mood.entries.map { it.value }.sorted()
        assertEquals(listOf(1, 2, 3, 4, 5), values)
    }

    @Test
    fun `fromValue returns correct mood`() {
        assertEquals(Mood.VERY_LOW, Mood.fromValue(1))
        assertEquals(Mood.LOW, Mood.fromValue(2))
        assertEquals(Mood.OKAY, Mood.fromValue(3))
        assertEquals(Mood.GOOD, Mood.fromValue(4))
        assertEquals(Mood.GREAT, Mood.fromValue(5))
    }

    @Test
    fun `fromValueOrNull returns null for invalid`() {
        assertNull(Mood.fromValueOrNull(0))
        assertNull(Mood.fromValueOrNull(6))
        assertNull(Mood.fromValueOrNull(-1))
    }

    @Test
    fun `feeling tag from label round trip`() {
        FeelingTag.entries.forEach { tag ->
            assertEquals(tag, FeelingTag.fromLabel(tag.label))
        }
    }

    @Test
    fun `time of day morning for 8am`() {
        assertEquals(TimeOfDay.MORNING, TimeOfDay.fromHour(8))
    }

    @Test
    fun `time of day afternoon for 14`() {
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.fromHour(14))
    }

    @Test
    fun `time of day night for midnight`() {
        assertEquals(TimeOfDay.NIGHT, TimeOfDay.fromHour(0))
    }

    @Test
    fun `all moods have non-empty labels and emojis`() {
        Mood.entries.forEach { mood ->
            assertTrue(mood.label.isNotBlank())
            assertTrue(mood.emoji.isNotBlank())
            assertTrue(mood.description.isNotBlank())
        }
    }

    @Test
    fun `all feeling tags have non-empty labels`() {
        FeelingTag.entries.forEach { tag ->
            assertTrue(tag.label.isNotBlank())
            assertTrue(tag.emoji.isNotBlank())
        }
    }
}
