package com.moodpulse.app.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * On-device Gemma 2B inference via MediaPipe LLM Inference API.
 * Model is downloaded once to app-private storage; all inference runs offline.
 */
class GemmaEngine(private val context: Context) {

    sealed class State {
        object NotDownloaded : State()
        data class Downloading(val progress: Float) : State()
        object Ready : State()
        data class Error(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.NotDownloaded)
    val state: StateFlow<State> = _state.asStateFlow()

    private var llm: LlmInference? = null

    // Gemma 2B INT4 quantised model (~1.5 GB), stored in app-private files/
    val modelFile: File get() = File(context.filesDir, MODEL_FILE_NAME)

    init {
        if (modelFile.exists() && modelFile.length() > MIN_MODEL_BYTES) {
            initLlm()
        }
    }

    internal fun initLlm() {
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .setTopK(40)
                .setTemperature(0.7f)
                .setRandomSeed(42)
                .build()
            llm = LlmInference.createFromOptions(context, options)
            _state.value = State.Ready
        } catch (e: Exception) {
            _state.value = State.Error("Failed to load model: ${e.message}")
        }
    }

    /** Analyse a mood note and return suggested FeelingTags. Returns empty list if model not ready. */
    suspend fun suggestTags(note: String): List<FeelingTag> = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext emptyList()
        if (note.isBlank()) return@withContext emptyList()
        runCatching {
            val response = inference.generateResponse(tagPrompt(note))
            parseTagsFromResponse(response)
        }.getOrDefault(emptyList())
    }

    /** Generate a personalised 2-sentence weekly insight from recent entries. */
    suspend fun weeklyInsight(entries: List<MoodEntry>): String = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext ""
        if (entries.isEmpty()) return@withContext ""
        runCatching { inference.generateResponse(insightPrompt(entries)) }.getOrDefault("")
    }

    /** Stream a conversational check-in reply token-by-token via the callback. */
    suspend fun streamCheckIn(
        userMessage: String,
        onToken: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext
        runCatching {
            inference.generateResponseAsync(checkInPrompt(userMessage)) { partial, done ->
                onToken(partial)
            }
        }
    }

    // ── Prompt builders ──────────────────────────────────────────────────

    private fun tagPrompt(note: String) = """
        <start_of_turn>user
        Analyse this short mood note. Reply with ONLY a comma-separated list of applicable tags from: CALM, HAPPY, ANXIOUS, STRESSED, TIRED, ENERGISED, FOCUSED, FRUSTRATED. No explanation.
        Note: "$note"
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()

    private fun insightPrompt(entries: List<MoodEntry>): String {
        val summary = entries.takeLast(14).joinToString("; ") { e ->
            "${Mood.fromValue(e.mood).label}(${e.feelingTags.joinToString(",")})"
        }
        return """
            <start_of_turn>user
            Recent mood check-ins: $summary
            Write a warm, encouraging 2-sentence personal insight about mood patterns. Be specific and positive.
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }

    private fun checkInPrompt(msg: String) = """
        <start_of_turn>user
        You are a supportive mood check-in companion. The user says: "$msg"
        Respond warmly in 1-2 sentences and ask one clarifying question about how they are feeling. Be concise.
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()

    private fun parseTagsFromResponse(response: String): List<FeelingTag> {
        val upper = response.uppercase()
        return FeelingTag.entries.filter { tag -> upper.contains(tag.name) }
    }

    fun close() {
        llm?.close()
        llm = null
    }

    companion object {
        const val MODEL_FILE_NAME = "gemma2b-it-cpu-int4.bin"
        // Minimum expected model file size (~800 MB after INT4 quant)
        const val MIN_MODEL_BYTES = 800L * 1024 * 1024
        // Hosted download URL — replace with your own CDN or Play Asset Delivery asset
        const val MODEL_DOWNLOAD_URL =
            "https://storage.googleapis.com/moodpulse-models/gemma2b-it-cpu-int4.bin"
    }
}
