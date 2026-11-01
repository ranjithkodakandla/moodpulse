package com.moodpulse.app

import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.ExportImport
import org.junit.Assert.*
import org.junit.Test

class ExportImportTest {

    private fun sampleEntry(id: Long = 1L, mood: Int = 4) = MoodEntry(
        id = id,
        mood = mood,
        timestampMs = 1730000000000L,
        timezoneId = "UTC",
        feelingTags = listOf("Calm", "Happy"),
        note = "Test note",
        source = "app"
    )

    @Test
    fun `json round trip preserves entry data`() {
        val entries = listOf(sampleEntry(1, 4), sampleEntry(2, 3))
        val json = ExportImport.toJson(entries, "1.0.0")
        assertTrue(json.contains("\"schemaVersion\""))
        assertTrue(json.contains("\"entries\""))

        val parsed = ExportImport.parseJson(json).getOrThrow()
        assertEquals(1, parsed.schemaVersion)
        assertEquals(2, parsed.entries.size)

        val restored = ExportImport.toMoodEntries(parsed)
        assertEquals(4, restored[0].mood)
        assertEquals(listOf("Calm", "Happy"), restored[0].feelingTags)
        assertEquals("Test note", restored[0].note)
    }

    @Test
    fun `csv contains header and entries`() {
        val entries = listOf(sampleEntry(1, 5))
        val csv = ExportImport.toCsv(entries)
        assertTrue(csv.startsWith("id,mood_value"))
        assertTrue(csv.contains("5"))
        assertTrue(csv.contains("Great"))
        assertTrue(csv.contains("Calm"))
    }

    @Test
    fun `parse json fails on invalid json`() {
        val result = ExportImport.parseJson("not json at all {{{")
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse json fails on unsupported schema version`() {
        val json = """{"schemaVersion":99,"exportTimestamp":"2024-01-01T00:00:00Z","appVersion":"1.0","entries":[]}"""
        val result = ExportImport.parseJson(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `csv handles note with commas`() {
        val entry = sampleEntry(1).copy(note = "Feeling good, really good")
        val csv = ExportImport.toCsv(listOf(entry))
        assertTrue(csv.contains("\"Feeling good, really good\""))
    }

    @Test
    fun `csv handles unicode in note`() {
        val entry = sampleEntry(1).copy(note = "今日は良い気分 🌿")
        val csv = ExportImport.toCsv(listOf(entry))
        assertTrue(csv.contains("今日は良い気分"))
    }

    @Test
    fun `to mood entries clamps invalid mood values`() {
        val json = """{"schemaVersion":1,"exportTimestamp":"2024-01-01T00:00:00Z","appVersion":"1.0","entries":[{"id":1,"mood":99,"moodLabel":"","timestampMs":1000000,"timestampIso":"","timezoneId":"UTC","feelingTags":[],"note":null,"source":"app"}]}"""
        val parsed = ExportImport.parseJson(json).getOrThrow()
        val entries = ExportImport.toMoodEntries(parsed)
        assertEquals(5, entries[0].mood) // clamped to max
    }

    @Test
    fun `empty entries produces valid json`() {
        val json = ExportImport.toJson(emptyList(), "1.0.0")
        val parsed = ExportImport.parseJson(json).getOrThrow()
        assertEquals(0, parsed.entries.size)
    }
}
