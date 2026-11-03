package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: MoodSuggestion): Long

    @Update
    suspend fun update(suggestion: MoodSuggestion)

    @Query("SELECT * FROM mood_suggestion WHERE status IN ('NEW', 'SHOWN') ORDER BY createdAt DESC LIMIT 5")
    fun observePendingList(): Flow<List<MoodSuggestion>>

    /**
     * Observes the currently active pending suggestion that is eligible for display.
     * Excludes acknowledged, confirmed, corrected, dismissed, expired, or currently snoozed suggestions.
     */
    @Query("""
        SELECT * FROM mood_suggestion 
        WHERE status IN ('NEW', 'SHOWN') 
          AND (snoozedUntil IS NULL OR snoozedUntil <= :now)
          AND createdAt >= (:now - :maxAgeMs)
        ORDER BY createdAt DESC 
        LIMIT 1
    """)
    fun observePending(now: Long = System.currentTimeMillis(), maxAgeMs: Long = 6 * 3600 * 1000L): Flow<MoodSuggestion?>

    @Query("""
        SELECT * FROM mood_suggestion 
        WHERE status IN ('NEW', 'SHOWN') 
          AND (snoozedUntil IS NULL OR snoozedUntil <= :now)
          AND createdAt >= (:now - :maxAgeMs)
        ORDER BY createdAt DESC 
        LIMIT 1
    """)
    suspend fun getPending(now: Long = System.currentTimeMillis(), maxAgeMs: Long = 6 * 3600 * 1000L): MoodSuggestion?

    @Query("SELECT * FROM mood_suggestion WHERE id = :id")
    suspend fun getById(id: Long): MoodSuggestion?

    @Query("SELECT * FROM mood_suggestion ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<MoodSuggestion>

    @Query("SELECT COUNT(*) FROM mood_suggestion WHERE createdAt >= :sinceTimestamp")
    suspend fun countSince(sinceTimestamp: Long): Int

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'CONFIRMED', userDecision = 'CONFIRMED',
            correctedMood = :mood, correctedFeelings = :feelings,
            lastInteractedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun setConfirmed(id: Long, mood: Int, feelings: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'CORRECTED', userDecision = 'CORRECTED',
            correctedMood = :mood, correctedFeelings = :feelings,
            feedbackComment = :comment, lastInteractedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun setCorrected(id: Long, mood: Int, feelings: String, comment: String? = null, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'ACKNOWLEDGED', userDecision = 'ACKNOWLEDGED',
            liked = :liked, feedbackComment = :comment,
            lastInteractedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun setAcknowledged(id: Long, liked: Boolean, comment: String? = null, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'DISMISSED', userDecision = 'DISMISSED',
            lastInteractedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun setDismissed(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'SNOOZED', snoozedUntil = :untilTimestamp,
            lastInteractedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun setSnoozed(id: Long, untilTimestamp: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE mood_suggestion 
        SET status = 'EXPIRED'
        WHERE status IN ('NEW', 'SHOWN') AND createdAt < :cutoffTimestamp
    """)
    suspend fun expireOlderThan(cutoffTimestamp: Long)

    @Query("UPDATE mood_suggestion SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

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
