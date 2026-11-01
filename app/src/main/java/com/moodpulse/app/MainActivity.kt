package com.moodpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.moodpulse.app.data.datastore.dataStore
import com.moodpulse.app.ui.navigation.MoodPulseNavGraph
import com.moodpulse.app.ui.onboarding.PermissionOnboardingScreen
import com.moodpulse.app.ui.theme.MoodPulseTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MoodPulseApp
        val crashFile = File(filesDir, "last_crash.txt")

        setContent {
            val crashLog = remember {
                if (crashFile.exists()) crashFile.readText().also { crashFile.delete() } else null
            }
            if (crashLog != null) {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(Modifier.height(48.dp))
                            Text("Crash Report", fontSize = 20.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Text("Screenshot this and share with developer:", fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(crashLog, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
                return@setContent
            }
            val themeMode by dataStore.data.map { prefs ->
                prefs[KEY_THEME_MODE] ?: "system"
            }.collectAsStateWithLifecycle(initialValue = "system")

            val onboardingDone by dataStore.data.map { prefs ->
                prefs[KEY_ONBOARDING_DONE] ?: false
            }.collectAsStateWithLifecycle(initialValue = null)

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MoodPulseTheme(darkTheme = isDark) {
                when (val done = onboardingDone) {
                    null -> {} // still loading, show nothing (splash)
                    false -> PermissionOnboardingScreen(
                        onFinished = {
                            lifecycleScope.launch {
                                dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
                            }
                        }
                    )
                    else -> MoodPulseNavGraph(container = app.container)
                }
            }
        }
    }
}
