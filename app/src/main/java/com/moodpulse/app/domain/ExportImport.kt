package com.moodpulse.app.domain

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.moodpulse.app.data.db.MoodEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ExportData(
    val schemaVersion: Int = 1,
    val exportTimestamp: String,
    val appVersion: String,
    val entries: List<ExportEntry>
)

data class ExportEntry(
    val id: Long,
    val mood: Int,
    val moodLabel: String,
    val timestampMs: Long,
    val timestampIso: String,
    val timezoneId: String,
    val feelingTags: List<String>,
    val note: String?,
    val source: String
)

object ExportImport {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun toJson(entries: List<MoodEntry>, appVersion: String): String {
        val data = ExportData(
            exportTimestamp = Instant.now().toString(),
            appVersion = appVersion,
            entries = entries.map { it.toExportEntry() }
        )
        return gson.toJson(data)
    }

    fun toCsv(entries: List<MoodEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("id,mood_value,mood_label,timestamp_ms,timestamp_iso,timezone,feeling_tags,note,source")
        entries.sortedByDescending { it.timestampMs }.forEach { e ->
            val iso = Instant.ofEpochMilli(e.timestampMs)
                .atZone(ZoneId.of(e.timezoneId)).format(isoFormatter)
            val tags = e.feelingTags.joinToString(";")
            val note = e.note?.replace("\"", "\"\"")?.let { "\"$it\"" } ?: ""
            sb.appendLine("${e.id},${e.mood},${Mood.fromValueOrNull(e.mood)?.label ?: ""},${e.timestampMs},${iso},${e.timezoneId},${tags},${note},${e.source}")
        }
        return sb.toString()
    }

    data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val errors: List<String>
    )

    fun parseJson(json: String): Result<ExportData> = runCatching {
        val data = gson.fromJson(json, ExportData::class.java)
        requireNotNull(data) { "Invalid JSON structure" }
        require(data.schemaVersion <= 1) { "Unsupported schema version: ${data.schemaVersion}" }
        require(data.entries != null) { "Missing entries array" }
        data
    }

    fun toMoodEntries(exportData: ExportData): List<MoodEntry> =
        exportData.entries.mapNotNull { e ->
            runCatching {
                MoodEntry(
                    id = 0, // will be auto-generated on insert
                    mood = e.mood.coerceIn(1, 5),
                    timestampMs = e.timestampMs,
                    timezoneId = e.timezoneId.ifBlank { "UTC" },
                    feelingTags = e.feelingTags,
                    note = e.note,
                    source = e.source.ifBlank { "import" }
                )
            }.getOrNull()
        }

    private fun MoodEntry.toExportEntry(): ExportEntry {
        val zoneId = runCatching { ZoneId.of(timezoneId) }.getOrDefault(ZoneId.of("UTC"))
        val iso = Instant.ofEpochMilli(timestampMs).atZone(zoneId).format(isoFormatter)
        return ExportEntry(
            id = id,
            mood = mood,
            moodLabel = Mood.fromValueOrNull(mood)?.label ?: "",
            timestampMs = timestampMs,
            timestampIso = iso,
            timezoneId = timezoneId,
            feelingTags = feelingTags,
            note = note,
            source = source
        )
    }
}
