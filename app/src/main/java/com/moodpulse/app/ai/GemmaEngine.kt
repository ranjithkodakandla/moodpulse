package com.moodpulse.app.ai

import android.content.Context
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
 * MediaPipe is loaded via reflection so a native library failure doesn't crash the app —
 * all AI features simply become no-ops until the user downloads the model.
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

    // Held as Any? to avoid a direct compile-time reference to LlmInference.
    // If the MediaPipe native library fails to load, this stays null and all AI
    // calls return graceful empty results instead of crashing the process.
    private var llm: Any? = null

    val modelFile: File get() = File(context.filesDir, MODEL_FILE_NAME)

    init {
        if (modelFile.exists() && modelFile.length() > MIN_MODEL_BYTES) {
            initLlm()
        }
    }

    internal fun initLlm() {
        try {
            val inferenceClass = Class.forName(
                "com.google.mediapipe.tasks.genai.llminference.LlmInference"
            )
            val optionsClass = Class.forName(
                "com.google.mediapipe.tasks.genai.llminference.LlmInference\$LlmInferenceOptions"
            )
            val builderClass = Class.forName(
                "com.google.mediapipe.tasks.genai.llminference.LlmInference\$LlmInferenceOptions\$Builder"
            )

            val builder = builderClass.getDeclaredConstructor().newInstance()
            builderClass.getMethod("setModelPath", String::class.java)
                .invoke(builder, modelFile.absolutePath)
            builderClass.getMethod("setMaxTokens", Int::class.java).invoke(builder, 512)
            builderClass.getMethod("setTopK", Int::class.java).invoke(builder, 40)
            builderClass.getMethod("setTemperature", Float::class.java).invoke(builder, 0.7f)
            builderClass.getMethod("setRandomSeed", Int::class.java).invoke(builder, 42)
            val options = builderClass.getMethod("build").invoke(builder)

            llm = inferenceClass.getMethod(
                "createFromOptions",
                Context::class.java, optionsClass
            ).invoke(null, context, options)

            _state.value = State.Ready
        } catch (e: Throwable) {
            _state.value = State.Error("Failed to load model: ${e.message}")
        }
    }

    suspend fun suggestTags(note: String): List<FeelingTag> = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext emptyList()
        if (note.isBlank()) return@withContext emptyList()
        runCatching {
            val response = callGenerateResponse(inference, tagPrompt(note))
            parseTagsFromResponse(response)
        }.getOrDefault(emptyList())
    }

    suspend fun weeklyInsight(entries: List<MoodEntry>): String = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext ""
        if (entries.isEmpty()) return@withContext ""
        runCatching { callGenerateResponse(inference, insightPrompt(entries)) }.getOrDefault("")
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext ""
        runCatching { callGenerateResponse(inference, prompt) }.getOrDefault("")
    }

    suspend fun streamCheckIn(
        userMessage: String,
        onToken: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val inference = llm ?: return@withContext
        runCatching {
            val response = callGenerateResponse(inference, checkInPrompt(userMessage))
            onToken(response)
        }
    }

    private fun callGenerateResponse(inference: Any, prompt: String): String {
        return inference.javaClass
            .getMethod("generateResponse", String::class.java)
            .invoke(inference, prompt) as? String ?: ""
    }

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
        runCatching {
            llm?.javaClass?.getMethod("close")?.invoke(llm)
        }
        llm = null
    }

    companion object {
        const val MODEL_FILE_NAME = "gemma2b-it-cpu-int4.bin"
        const val MIN_MODEL_BYTES = 800L * 1024 * 1024
        const val MODEL_DOWNLOAD_URL =
            "https://storage.googleapis.com/moodpulse-models/gemma2b-it-cpu-int4.bin"
    }
}
