package com.moodpulse.app.smartcontext

import com.moodpulse.app.data.db.BehaviouralObservation
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.db.MoodSuggestion
import com.moodpulse.app.ai.GemmaEngine
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SuggestionEngine(private val gemmaEngine: GemmaEngine?) {

    private val gson = Gson()
    private val analyser = BehaviourAnalyser()

    suspend fun generateSuggestion(
        observations: List<BehaviouralObservation>,
        recentMoods: List<MoodEntry>
    ): MoodSuggestion? = withContext(Dispatchers.Default) {
        if (observations.isEmpty()) return@withContext null

        val (candidateMood, confidenceBand, suggestedFeelings) = inferMoodFromObservations(observations, recentMoods)
        val observationsJson = gson.toJson(observations.map { it.observationType })

        val promptText = buildPromptText(observations, recentMoods, candidateMood, suggestedFeelings)

        // Use Gemma to generate a natural language phrasing if available
        val naturalPrompt = if (gemmaEngine != null) {
            try {
                val systemPrompt = """You are a gentle, non-judgmental wellness companion.
Based on the observation summary below, write ONE short sentence (max 20 words) that tentatively asks if the user might be feeling a certain way.
Do NOT use medical terminology. Do NOT be alarming. Be warm and curious.
Observation: $promptText
Reply with just the one-sentence question."""
                gemmaEngine.generateResponse(systemPrompt).trim().take(200)
            } catch (e: Exception) {
                buildFallbackPromptText(observations, candidateMood)
            }
        } else {
            buildFallbackPromptText(observations, candidateMood)
        }

        MoodSuggestion(
            candidateMood = candidateMood,
            candidateFeelings = suggestedFeelings.joinToString(","),
            confidenceBand = confidenceBand,
            observationsJson = observationsJson,
            promptText = naturalPrompt
        )
    }

    private fun inferMoodFromObservations(
        observations: List<BehaviouralObservation>,
        recentMoods: List<MoodEntry>
    ): Triple<Int, String, List<String>> {
        val highCategories = observations
            .filter { it.observationType.startsWith("HIGH_") }
            .map { it.observationType.removePrefix("HIGH_") }
        val lowCategories = observations
            .filter { it.observationType.startsWith("LOW_") }
            .map { it.observationType.removePrefix("LOW_") }

        val stressSignals = highCategories.count { it in listOf("SOCIAL", "SHOPPING", "ENTERTAINMENT", "GAMING") }
        val withdrawalSignals = lowCategories.count { it in listOf("PRODUCTIVITY", "COMMUNICATION", "HEALTH") }

        val recentAvgMood = if (recentMoods.isNotEmpty())
            recentMoods.take(5).map { it.mood }.average().toFloat()
        else 3f

        return when {
            stressSignals >= 2 || (stressSignals >= 1 && withdrawalSignals >= 1) -> {
                Triple(2, "LOW", listOf("stressed", "overwhelmed", "restless"))
            }
            highCategories.contains("SHOPPING") && highCategories.contains("ENTERTAINMENT") -> {
                Triple(2, "LOW", listOf("distracted", "restless", "seeking comfort"))
            }
            highCategories.contains("PRODUCTIVITY") && lowCategories.isEmpty() -> {
                Triple(4, "MEDIUM", listOf("focused", "engaged", "motivated"))
            }
            lowCategories.contains("SOCIAL") && lowCategories.contains("COMMUNICATION") -> {
                Triple(2, "LOW", listOf("withdrawn", "quiet", "reflective"))
            }
            else -> {
                val baseMood = recentAvgMood.toInt().coerceIn(2, 4)
                Triple(baseMood, "LOW", listOf("unsure", "mixed"))
            }
        }
    }

    private fun buildPromptText(
        observations: List<BehaviouralObservation>,
        recentMoods: List<MoodEntry>,
        candidateMood: Int,
        suggestedFeelings: List<String>
    ): String {
        val summary = analyser.buildObservationSummary(observations)
        val moodLabel = when (candidateMood) {
            1 -> "very low"
            2 -> "low"
            3 -> "okay"
            4 -> "good"
            5 -> "great"
            else -> "mixed"
        }
        return "User has $summary. Tentative mood: $moodLabel (${suggestedFeelings.joinToString(", ")})"
    }

    private fun buildFallbackPromptText(
        observations: List<BehaviouralObservation>,
        candidateMood: Int
    ): String {
        val summary = analyser.buildObservationSummary(observations)
        return when (candidateMood) {
            1, 2 -> "Noticed $summary — are you feeling a bit stressed or low today?"
            3 -> "Noticed $summary — how are you actually feeling?"
            4, 5 -> "Noticed $summary — feeling pretty good today?"
            else -> "Noticed $summary — want to log how you're feeling?"
        }
    }
}
