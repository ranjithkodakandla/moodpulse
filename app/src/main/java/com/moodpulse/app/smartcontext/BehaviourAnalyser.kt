package com.moodpulse.app.smartcontext

import com.moodpulse.app.data.db.AppUsageAggregate
import com.moodpulse.app.data.db.BehaviouralObservation
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CategoryBaseline(
    val category: String,
    val meanMinutesPerDay: Float,
    val stdDev: Float,
    val sampleDays: Int
)

data class BehaviourAnalysis(
    val observations: List<BehaviouralObservation>,
    val categoryTotals: Map<String, Int>,
    val deviations: Map<String, Float>
)

class BehaviourAnalyser {

    private val gson = Gson()

    fun analyse(
        recentHistory: List<AppUsageAggregate>,    // last 30 days baseline data
        todayData: List<AppUsageAggregate>          // today or yesterday to analyse
    ): BehaviourAnalysis {
        val baselines = computeBaselines(recentHistory)
        val todayCategoryTotals = todayData
            .groupBy { it.appCategory }
            .mapValues { (_, list) -> list.sumOf { it.totalMinutes } }

        val observations = mutableListOf<BehaviouralObservation>()
        val deviations = mutableMapOf<String, Float>()
        val now = System.currentTimeMillis()

        for ((category, todayMinutes) in todayCategoryTotals) {
            val baseline = baselines[category] ?: continue
            if (baseline.sampleDays < 3) continue // need at least 3 days of data
            if (baseline.stdDev < 1f) continue     // skip stable-at-zero categories

            val deviation = (todayMinutes - baseline.meanMinutesPerDay) / baseline.stdDev
            deviations[category] = deviation

            if (deviation >= 1.5f) {
                val evidence = mapOf(
                    "category" to category,
                    "todayMinutes" to todayMinutes,
                    "baselineMean" to baseline.meanMinutesPerDay,
                    "stdDev" to baseline.stdDev,
                    "zScore" to deviation
                )
                observations.add(
                    BehaviouralObservation(
                        observationType = "HIGH_${category}",
                        dateTime = now,
                        baselineValue = baseline.meanMinutesPerDay,
                        currentValue = todayMinutes.toFloat(),
                        evidenceJson = gson.toJson(evidence)
                    )
                )
            } else if (deviation <= -1.5f && baseline.meanMinutesPerDay >= 10f) {
                // Only flag low usage if person normally uses this category meaningfully
                val evidence = mapOf(
                    "category" to category,
                    "todayMinutes" to todayMinutes,
                    "baselineMean" to baseline.meanMinutesPerDay,
                    "stdDev" to baseline.stdDev,
                    "zScore" to deviation
                )
                observations.add(
                    BehaviouralObservation(
                        observationType = "LOW_${category}",
                        dateTime = now,
                        baselineValue = baseline.meanMinutesPerDay,
                        currentValue = todayMinutes.toFloat(),
                        evidenceJson = gson.toJson(evidence)
                    )
                )
            }
        }

        return BehaviourAnalysis(observations, todayCategoryTotals, deviations)
    }

    private fun computeBaselines(history: List<AppUsageAggregate>): Map<String, CategoryBaseline> {
        val byCategory = history.groupBy { it.appCategory }
        return byCategory.mapValues { (category, records) ->
            val byDate = records.groupBy { it.date }
                .mapValues { (_, dayRecords) -> dayRecords.sumOf { it.totalMinutes }.toFloat() }
            val values = byDate.values.toList()
            val mean = values.average().toFloat()
            val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
            val stdDev = Math.sqrt(variance.toDouble()).toFloat()
            CategoryBaseline(
                category = category,
                meanMinutesPerDay = mean,
                stdDev = stdDev,
                sampleDays = values.size
            )
        }
    }

    fun buildObservationSummary(observations: List<BehaviouralObservation>): String {
        if (observations.isEmpty()) return ""
        val lines = observations.map { obs ->
            val parts = obs.observationType.split("_", limit = 2)
            val direction = if (parts[0] == "HIGH") "more" else "less"
            val cat = parts.getOrElse(1) { "app" }.lowercase().replace("_", " ")
            val diff = Math.abs(obs.currentValue - obs.baselineValue).toInt()
            "${direction} ${cat} usage than usual (+${diff} min)"
        }
        return lines.joinToString("; ")
    }
}
