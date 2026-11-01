package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviouralObservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<BehaviouralObservation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: BehaviouralObservation): Long

    @Query("SELECT * FROM behavioural_observation WHERE dateTime >= :fromMs ORDER BY dateTime DESC")
    fun observeSince(fromMs: Long): Flow<List<BehaviouralObservation>>

    @Query("SELECT * FROM behavioural_observation WHERE dateTime >= :fromMs ORDER BY dateTime DESC")
    suspend fun getSince(fromMs: Long): List<BehaviouralObservation>

    @Query("SELECT * FROM behavioural_observation ORDER BY dateTime DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<BehaviouralObservation>

    @Query("DELETE FROM behavioural_observation")
    suspend fun deleteAll()
}
