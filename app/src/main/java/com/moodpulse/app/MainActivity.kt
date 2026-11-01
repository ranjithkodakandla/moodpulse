package com.moodpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.moodpulse.app.data.datastore.AppPreferences
import com.moodpulse.app.ui.navigation.MoodPulseNavGraph
import com.moodpulse.app.ui.onboarding.PermissionOnboardingScreen
import com.moodpulse.app.ui.theme.MoodPulseTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MoodPulseApp

        setContent {
            val prefsState by app.container.preferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = AppPreferences())

            val isDark = when (prefsState.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MoodPulseTheme(darkTheme = isDark) {
                if (!prefsState.onboardingCompleted) {
                    PermissionOnboardingScreen(
                        onFinished = {
                            lifecycleScope.launch {
                                app.container.preferencesRepository.setOnboardingCompleted(true)
                            }
                        }
                    )
                } else {
                    MoodPulseNavGraph(container = app.container)
                }
            }
        }
    }
}
