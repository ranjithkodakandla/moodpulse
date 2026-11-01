package com.moodpulse.app.ai

import android.content.Context
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.FeelingTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Stub GemmaEngine — MediaPipe dependency removed to ensure app stability.
 * All methods return graceful empty results. On-device AI will be re-enabled
 * in a future release once device compatibility is confirmed.
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

    val modelFile: File get() = File(context.filesDir, MODEL_FILE_NAME)

    fun initLlm() {
        _state.value = State.Error("On-device AI coming in a future update")
    }

    suspend fun suggestTags(note: String): List<FeelingTag> = emptyList()

    suspend fun weeklyInsight(entries: List<MoodEntry>): String = ""

    suspend fun generateResponse(prompt: String): String = ""

    suspend fun streamCheckIn(userMessage: String, onToken: (String) -> Unit) {}

    fun close() {}

    companion object {
        const val MODEL_FILE_NAME = "gemma2b-it-cpu-int4.bin"
        const val MIN_MODEL_BYTES = 800L * 1024 * 1024
        const val MODEL_DOWNLOAD_URL =
            "https://storage.googleapis.com/moodpulse-models/gemma2b-it-cpu-int4.bin"
    }
}
