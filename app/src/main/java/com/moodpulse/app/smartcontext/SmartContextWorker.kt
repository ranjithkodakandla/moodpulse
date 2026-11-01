package com.moodpulse.app.smartcontext

import android.content.Context
import androidx.work.*
import com.moodpulse.app.MoodPulseApp
import com.moodpulse.app.data.db.MoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SmartContextWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as MoodPulseApp
            val container = app.container
            val db = container.database

            val prefs = db.smartContextPreferencesDao().get() ?: return@withContext Result.success()
            if (!prefs.monitoringEnabled) return@withContext Result.success()

            val collector = UsageCollector(applicationContext)
            if (!collector.hasPermission()) return@withContext Result.success()

            val excludedPackages = parseExcludedPackages(prefs.excludedPackagesJson)
            val yesterdayAggregates = collector.collectYesterdayAggregates(excludedPackages)

            if (yesterdayAggregates.isEmpty()) return@withContext Result.success()

            db.appUsageAggregateDao().insertAll(yesterdayAggregates)

            // Prune data older than 90 days
            val cutoffDate = LocalDate.now().minusDays(90)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
            db.appUsageAggregateDao().deleteOlderThan(cutoffDate)

            // Check cooldown before generating a new suggestion
            val now = System.currentTimeMillis()
            val cooldownMs = prefs.suggestionCooldownMinutes * 60_000L
            if (now - prefs.lastSuggestionAt < cooldownMs) return@withContext Result.success()

            // Get 30 days baseline
            val baselineFrom = LocalDate.now().minusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
            val baselineData = db.appUsageAggregateDao().getSince(baselineFrom)

            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val yesterdayData = db.appUsageAggregateDao().getByDate(yesterday)

            val analyser = BehaviourAnalyser()
            val analysis = analyser.analyse(baselineData, yesterdayData)

            if (analysis.observations.isEmpty()) return@withContext Result.success()

            db.behaviouralObservationDao().insertAll(analysis.observations)

            val recentMoods = db.moodDao().getRecentEntries(10).first()
            val engine = SuggestionEngine(container.gemmaEngine)
            val suggestion = engine.generateSuggestion(analysis.observations, recentMoods)
                ?: return@withContext Result.success()

            db.moodSuggestionDao().insert(suggestion)
            db.smartContextPreferencesDao().setLastSuggestionAt(now)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun parseExcludedPackages(json: String): Set<String> {
        return try {
            val list = com.google.gson.Gson().fromJson(json, Array<String>::class.java)
            list.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    companion object {
        private const val WORK_NAME = "smart_context_daily"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SmartContextWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
