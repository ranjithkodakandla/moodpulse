package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SpendingEvent): Long

    @Update
    suspend fun update(event: SpendingEvent)

    @Query("SELECT * FROM spending_event ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SpendingEvent>>

    @Query("SELECT * FROM spending_event WHERE timestamp >= :fromMs ORDER BY timestamp DESC")
    suspend fun getSince(fromMs: Long): List<SpendingEvent>

    @Query("UPDATE spending_event SET userDecision = :decision WHERE id = :id")
    suspend fun setDecision(id: Long, decision: String)

    @Query("DELETE FROM spending_event")
    suspend fun deleteAll()
}
