package com.moodpulse.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moodpulse.app.MoodPulseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as MoodPulseApp
            val prefs = app.container.preferencesRepository.preferences.first()
            if (prefs.remindersEnabled) {
                ReminderScheduler.schedule(context, prefs.reminderHour, prefs.reminderMinute)
            }
        }
    }
}
