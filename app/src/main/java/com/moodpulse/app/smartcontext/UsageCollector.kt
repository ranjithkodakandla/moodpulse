package com.moodpulse.app.smartcontext

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.moodpulse.app.data.db.AppUsageAggregate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class UsageCollector(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    fun hasPermission(): Boolean = runCatching {
        val mgr = usageStatsManager ?: return@runCatching false
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(1)
        val stats = mgr.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        stats != null && stats.isNotEmpty()
    }.getOrDefault(false)

    fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun collectYesterdayAggregates(excludedPackages: Set<String> = emptySet()): List<AppUsageAggregate> {
        val yesterday = LocalDate.now().minusDays(1)
        return collectForDate(yesterday, excludedPackages)
    }

    fun collectForDate(date: LocalDate, excludedPackages: Set<String> = emptySet()): List<AppUsageAggregate> {
        val mgr = usageStatsManager ?: return emptyList()
        val startMs = date.atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endMs = startMs + TimeUnit.DAYS.toMillis(1)

        val stats = mgr.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startMs, endMs
        ) ?: return emptyList()

        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return stats
            .filter { stat ->
                stat.totalTimeInForeground > 0 &&
                stat.packageName !in excludedPackages &&
                !isSystemPackage(stat.packageName)
            }
            .map { stat ->
                val category = AppClassifier.classify(stat.packageName)
                val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(stat.totalTimeInForeground).toInt()
                AppUsageAggregate(
                    date = dateStr,
                    packageName = stat.packageName,
                    appCategory = category.name,
                    totalMinutes = totalMinutes,
                    sessionCount = 1, // UsageStats doesn't expose session count easily
                    timePeriod = "DAY"
                )
            }
            .filter { it.totalMinutes >= 1 } // skip sub-minute usage
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return packageName.startsWith("com.android.") ||
               packageName.startsWith("android.") ||
               packageName == "com.google.android.gms" ||
               packageName == "com.google.android.gsf" ||
               packageName == "com.google.android.packageinstaller"
    }
}
