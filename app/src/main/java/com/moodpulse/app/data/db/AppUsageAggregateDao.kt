package com.moodpulse.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageAggregateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aggregates: List<AppUsageAggregate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aggregate: AppUsageAggregate)

    @Query("SELECT * FROM app_usage_aggregate WHERE date = :date ORDER BY totalMinutes DESC")
    suspend fun getByDate(date: String): List<AppUsageAggregate>

    @Query("SELECT * FROM app_usage_aggregate WHERE date >= :fromDate ORDER BY date DESC")
    suspend fun getSince(fromDate: String): List<AppUsageAggregate>

    @Query("SELECT * FROM app_usage_aggregate WHERE date >= :fromDate ORDER BY date DESC")
    fun observeSince(fromDate: String): Flow<List<AppUsageAggregate>>

    @Query("SELECT * FROM app_usage_aggregate WHERE appCategory = :category AND date >= :fromDate")
    suspend fun getByCategorySince(category: String, fromDate: String): List<AppUsageAggregate>

    @Query("SELECT DISTINCT date FROM app_usage_aggregate ORDER BY date DESC LIMIT 1")
    suspend fun getLatestDate(): String?

    @Query("SELECT COUNT(DISTINCT date) FROM app_usage_aggregate")
    suspend fun getDayCount(): Int

    @Query("DELETE FROM app_usage_aggregate")
    suspend fun deleteAll()

    @Query("DELETE FROM app_usage_aggregate WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)
}
