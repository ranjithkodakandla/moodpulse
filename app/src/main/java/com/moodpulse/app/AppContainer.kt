package com.moodpulse.app

import android.content.Context
import com.moodpulse.app.data.datastore.UserPreferencesRepository
import com.moodpulse.app.data.db.MoodDatabase
import com.moodpulse.app.data.repository.MoodRepository

class AppContainer(val context: Context) {
    val appVersion: String by lazy {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("1.0.0")
    }

    val database: MoodDatabase by lazy { MoodDatabase.getInstance(context) }

    val moodRepository: MoodRepository by lazy { MoodRepository(database.moodDao()) }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}
