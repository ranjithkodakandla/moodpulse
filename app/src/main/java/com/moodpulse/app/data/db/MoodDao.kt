package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry): Long

    @Update
    suspend fun update(entry: MoodEntry)

    @Delete
    suspend fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM mood_entries ORDER BY timestampMs DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: Long): MoodEntry?

    @Query("""
        SELECT * FROM mood_entries
        WHERE timestampMs >= :startMs AND timestampMs < :endMs
        ORDER BY timestampMs DESC
    """)
    fun getEntriesInRange(startMs: Long, endMs: Long): Flow<List<MoodEntry>>

    @Query("""
        SELECT * FROM mood_entries
        WHERE timestampMs >= :startMs AND timestampMs < :endMs
        ORDER BY timestampMs DESC
    """)
    suspend fun getEntriesInRangeOnce(startMs: Long, endMs: Long): List<MoodEntry>

    @Query("SELECT * FROM mood_entries ORDER BY timestampMs DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<MoodEntry>>

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM mood_entries WHERE timestampMs >= :startMs AND timestampMs < :endMs")
    suspend fun getCountInRange(startMs: Long, endMs: Long): Int
}
