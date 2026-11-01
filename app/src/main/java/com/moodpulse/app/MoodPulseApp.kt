package com.moodpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.io.File

class MoodPulseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val msg = buildString {
                    append("THREAD: ${t.name}\n")
                    append("${e::class.java.name}: ${e.message}\n")
                    e.stackTrace.take(20).forEach { append("  at $it\n") }
                    var cause = e.cause
                    while (cause != null) {
                        append("Caused by: ${cause::class.java.name}: ${cause.message}\n")
                        cause.stackTrace.take(10).forEach { append("  at $it\n") }
                        cause = cause.cause
                    }
                }
                File(filesDir, "last_crash.txt").writeText(msg)
            } catch (_: Throwable) {}
            prev?.uncaughtException(t, e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            container = AppContainer(this)
        } catch (e: Throwable) {
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
