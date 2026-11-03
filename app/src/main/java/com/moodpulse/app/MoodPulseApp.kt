package com.moodpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.moodpulse.app.ui.crash.CrashActivity
import com.moodpulse.app.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MoodPulseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stackTrace = Log.getStackTraceString(throwable)
                val intent = Intent(this, CrashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("EXTRA_STACK_TRACE", stackTrace)
                }
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            } catch (t: Throwable) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()

        // Ensure default daily reminder is active if enabled
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = container.preferencesRepository.preferences.first()
                if (prefs.remindersEnabled) {
                    ReminderScheduler.schedule(applicationContext, prefs.reminderHour, prefs.reminderMinute)
                }
            } catch (_: Throwable) {}
        }
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
