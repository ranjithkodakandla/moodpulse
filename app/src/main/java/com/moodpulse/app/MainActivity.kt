package com.moodpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodpulse.app.data.datastore.UserPreferencesRepository
import com.moodpulse.app.data.datastore.dataStore
import com.moodpulse.app.ui.navigation.MoodPulseNavGraph
import com.moodpulse.app.ui.theme.MoodPulseTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MoodPulseApp

        setContent {
            val themeMode by dataStore.data.map { prefs ->
                prefs[androidx.datastore.preferences.core.stringPreferencesKey("theme_mode")] ?: "system"
            }.collectAsStateWithLifecycle(initialValue = "system")

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MoodPulseTheme(darkTheme = isDark) {
                MoodPulseNavGraph(container = app.container)
            }
        }
    }
}
