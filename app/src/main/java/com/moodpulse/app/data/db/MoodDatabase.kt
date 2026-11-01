package com.moodpulse.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MoodEntry::class,
        AppUsageAggregate::class,
        BehaviouralObservation::class,
        MoodSuggestion::class,
        SpendingEvent::class,
        SmartContextPreferences::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(FeelingTagConverter::class)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun appUsageAggregateDao(): AppUsageAggregateDao
    abstract fun behaviouralObservationDao(): BehaviouralObservationDao
    abstract fun moodSuggestionDao(): MoodSuggestionDao
    abstract fun spendingEventDao(): SpendingEventDao
    abstract fun smartContextPreferencesDao(): SmartContextPreferencesDao

    companion object {
        private const val DATABASE_NAME = "moodpulse.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_usage_aggregate` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `appCategory` TEXT NOT NULL,
                        `totalMinutes` INTEGER NOT NULL,
                        `sessionCount` INTEGER NOT NULL,
                        `timePeriod` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `behavioural_observation` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `observationType` TEXT NOT NULL,
                        `dateTime` INTEGER NOT NULL,
                        `baselineValue` REAL NOT NULL,
                        `currentValue` REAL NOT NULL,
                        `evidenceJson` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mood_suggestion` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `candidateMood` INTEGER NOT NULL,
                        `candidateFeelings` TEXT NOT NULL,
                        `confidenceBand` TEXT NOT NULL,
                        `observationsJson` TEXT NOT NULL,
                        `promptText` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `userDecision` TEXT,
                        `correctedMood` INTEGER,
                        `correctedFeelings` TEXT
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `spending_event` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` REAL NOT NULL,
                        `currency` TEXT NOT NULL,
                        `merchant` TEXT,
                        `timestamp` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `isPlanned` INTEGER NOT NULL,
                        `moodAtTime` INTEGER,
                        `userDecision` TEXT
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `smart_context_prefs` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `monitoringEnabled` INTEGER NOT NULL,
                        `excludedPackagesJson` TEXT NOT NULL,
                        `quietHoursStart` INTEGER NOT NULL,
                        `quietHoursEnd` INTEGER NOT NULL,
                        `suggestionNotificationsEnabled` INTEGER NOT NULL,
                        `spendingAlertsEnabled` INTEGER NOT NULL,
                        `spendingThreshold` REAL NOT NULL,
                        `lastSuggestionAt` INTEGER NOT NULL,
                        `suggestionCooldownMinutes` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        @Volatile
        private var instance: MoodDatabase? = null

        fun getInstance(context: Context): MoodDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): MoodDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MoodDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}
