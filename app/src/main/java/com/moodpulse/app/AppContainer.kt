package com.moodpulse.app

import android.content.Context
import com.moodpulse.app.ai.GemmaEngine
import com.moodpulse.app.data.datastore.UserPreferencesRepository
import com.moodpulse.app.data.db.MoodDatabase
import com.moodpulse.app.data.repository.MoodRepository
import com.moodpulse.app.smartcontext.SuggestionEngine
import com.moodpulse.app.smartcontext.UsageCollector

class AppContainer(val context: Context) {
    val appVersion: String by lazy {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")
    }

    val database: MoodDatabase by lazy { MoodDatabase.getInstance(context) }

    val moodRepository: MoodRepository by lazy { MoodRepository(database.moodDao()) }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    val gemmaEngine: GemmaEngine by lazy { GemmaEngine(context) }

    val usageCollector: UsageCollector by lazy { UsageCollector(context) }

    val suggestionEngine: SuggestionEngine by lazy { SuggestionEngine(gemmaEngine) }
}
