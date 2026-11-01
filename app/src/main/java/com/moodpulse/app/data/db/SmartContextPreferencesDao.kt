package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartContextPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prefs: SmartContextPreferences)

    @Update
    suspend fun update(prefs: SmartContextPreferences)

    @Query("SELECT * FROM smart_context_prefs WHERE id = 1")
    fun observe(): Flow<SmartContextPreferences?>

    @Query("SELECT * FROM smart_context_prefs WHERE id = 1")
    suspend fun get(): SmartContextPreferences?

    @Query("UPDATE smart_context_prefs SET monitoringEnabled = :enabled WHERE id = 1")
    suspend fun setMonitoringEnabled(enabled: Boolean)

    @Query("UPDATE smart_context_prefs SET lastSuggestionAt = :timestamp WHERE id = 1")
    suspend fun setLastSuggestionAt(timestamp: Long)

    @Query("DELETE FROM smart_context_prefs")
    suspend fun deleteAll()
}
