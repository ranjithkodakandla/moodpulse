package com.moodpulse.app.smartcontext

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.moodpulse.app.data.db.AppUsageAggregate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class UsageCollector(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    fun hasPermission(): Boolean {
        // 1. Primary Android check: AppOpsManager (instant, authoritative upon granting in system settings)
        try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            if (appOps != null) {
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.packageName
                    )
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.packageName
                    )
                }
                if (mode == AppOpsManager.MODE_ALLOWED) {
                    return true
                }
            }
        } catch (_: Throwable) {}

        // 2. Secondary fallback check: queryUsageStats over the last 7 days
        return runCatching {
            val mgr = usageStatsManager ?: return@runCatching false
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(7)
            val stats = mgr.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            stats != null && stats.isNotEmpty()
        }.getOrDefault(false)
    }

    fun openPermissionSettings() {
        val intentWithPackage = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:${context.packageName}")
        }
        val fallbackIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intentWithPackage)
        } catch (_: Throwable) {
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Throwable) {}
        }
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
