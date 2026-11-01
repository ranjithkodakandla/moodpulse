package com.moodpulse.app.data.repository

import com.moodpulse.app.data.db.MoodDao
import com.moodpulse.app.data.db.MoodEntry
import kotlinx.coroutines.flow.Flow

class MoodRepository(private val dao: MoodDao) {

    val allEntries: Flow<List<MoodEntry>> = dao.getAllEntries()

    fun getRecentEntries(limit: Int = 20): Flow<List<MoodEntry>> = dao.getRecentEntries(limit)

    fun getEntriesInRange(startMs: Long, endMs: Long): Flow<List<MoodEntry>> =
        dao.getEntriesInRange(startMs, endMs)

    suspend fun getEntriesInRangeOnce(startMs: Long, endMs: Long): List<MoodEntry> =
        dao.getEntriesInRangeOnce(startMs, endMs)

    suspend fun saveEntry(entry: MoodEntry): Long = dao.insert(entry)

    suspend fun updateEntry(entry: MoodEntry) = dao.update(entry)

    suspend fun deleteEntry(entry: MoodEntry) = dao.delete(entry)

    suspend fun deleteAllEntries() = dao.deleteAll()

    suspend fun getTotalCount(): Int = dao.getTotalCount()

    suspend fun getById(id: Long): MoodEntry? = dao.getById(id)

    suspend fun getCountInRange(startMs: Long, endMs: Long): Int =
        dao.getCountInRange(startMs, endMs)
}
