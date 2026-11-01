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
            val baseline = baselines[category]

            // 1. Z-Score Statistical Detection if we have at least 3 days baseline
            if (baseline != null && baseline.sampleDays >= 3 && baseline.stdDev >= 1f) {
                val deviation = (todayMinutes - baseline.meanMinutesPerDay) / baseline.stdDev
                deviations[category] = deviation

                if (deviation >= 1.3f) {
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
                } else if (deviation <= -1.3f && baseline.meanMinutesPerDay >= 10f) {
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
            } else {
                // 2. Direct Adaptive Heuristic Detection for new installs & daily habit triggers
                val isHighUsage = when (category) {
                    "SHOPPING" -> todayMinutes >= 15
                    "SOCIAL" -> todayMinutes >= 30
                    "ENTERTAINMENT" -> todayMinutes >= 45
                    "GAMING" -> todayMinutes >= 30
                    "PRODUCTIVITY" -> todayMinutes >= 20
                    else -> todayMinutes >= 45
                }
                if (isHighUsage) {
                    val evidence = mapOf(
                        "category" to category,
                        "todayMinutes" to todayMinutes,
                        "threshold" to "heuristic"
                    )
                    observations.add(
                        BehaviouralObservation(
                            observationType = "HIGH_${category}",
                            dateTime = now,
                            baselineValue = 0f,
                            currentValue = todayMinutes.toFloat(),
                            evidenceJson = gson.toJson(evidence)
                        )
                    )
                }
            }
        }

        // 3. If no observation hit high threshold but habits exist, flag top category so AI can correlate
        if (observations.isEmpty() && todayCategoryTotals.isNotEmpty()) {
            val top = todayCategoryTotals.maxByOrNull { it.value }
            if (top != null && top.value >= 5) {
                val evidence = mapOf("category" to top.key, "todayMinutes" to top.value, "reason" to "dominant_activity")
                observations.add(
                    BehaviouralObservation(
                        observationType = "HIGH_${top.key}",
                        dateTime = now,
                        baselineValue = 0f,
                        currentValue = top.value.toFloat(),
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
