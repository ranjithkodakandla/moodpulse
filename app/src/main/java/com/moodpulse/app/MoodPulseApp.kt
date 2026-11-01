package com.moodpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MoodPulseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            container = AppContainer(this)
        } catch (e: Throwable) {
            // AppContainer construction should never throw (all lazy), but if it does,
            // create a minimal container so the app opens rather than crashing.
            container = AppContainer(this)
        }
        try {
            createNotificationChannel()
        } catch (_: Throwable) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Check-in",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminder to log your mood"
            }
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "mood_reminders"
    }
}
