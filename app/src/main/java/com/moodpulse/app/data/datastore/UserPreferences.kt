package com.moodpulse.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class AppPreferences(
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val themeMode: String = "system",   // "light", "dark", "system"
    val onboardingCompleted: Boolean = false,
    val hasRatedApp: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val HAS_RATED_APP = booleanPreferencesKey("has_rated_app")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            AppPreferences(
                remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: false,
                reminderHour = prefs[Keys.REMINDER_HOUR] ?: 20,
                reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
                themeMode = prefs[Keys.THEME_MODE] ?: "system",
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                hasRatedApp = prefs[Keys.HAS_RATED_APP] ?: false
            )
        }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setHasRatedApp(rated: Boolean) {
        context.dataStore.edit { it[Keys.HAS_RATED_APP] = rated }
    }
}
