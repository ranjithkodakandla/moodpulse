package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: MoodSuggestion): Long

    @Update
    suspend fun update(suggestion: MoodSuggestion)

    @Query("SELECT * FROM mood_suggestion WHERE userDecision IS NULL ORDER BY createdAt DESC LIMIT 1")
    fun observePending(): Flow<MoodSuggestion?>

    @Query("SELECT * FROM mood_suggestion WHERE userDecision IS NULL ORDER BY createdAt DESC LIMIT 1")
    suspend fun getPending(): MoodSuggestion?

    @Query("SELECT * FROM mood_suggestion WHERE id = :id")
    suspend fun getById(id: Long): MoodSuggestion?

    @Query("SELECT * FROM mood_suggestion ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<MoodSuggestion>

    @Query("UPDATE mood_suggestion SET userDecision = :decision WHERE id = :id")
    suspend fun setDecision(id: Long, decision: String)

    @Query("""
        UPDATE mood_suggestion SET userDecision = :decision,
        correctedMood = :mood, correctedFeelings = :feelings WHERE id = :id
    """)
    suspend fun setCorrection(id: Long, decision: String, mood: Int, feelings: String)

    @Query("DELETE FROM mood_suggestion")
    suspend fun deleteAll()
}
