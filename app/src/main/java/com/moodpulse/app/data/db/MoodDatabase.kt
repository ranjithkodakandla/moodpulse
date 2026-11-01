package com.moodpulse.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MoodEntry::class],
    version = 1,
    exportSchema = true
)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao

    companion object {
        private const val DATABASE_NAME = "moodpulse.db"

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
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}
