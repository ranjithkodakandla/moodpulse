package com.moodpulse.app.ai

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.MoodPulseApp
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.domain.FeelingTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class GemmaViewModel(app: Application) : AndroidViewModel(app) {

    private val engine: GemmaEngine = (app as MoodPulseApp).container.gemmaEngine

    val engineState: StateFlow<GemmaEngine.State> = engine.state

    private val _suggestedTags = MutableStateFlow<List<FeelingTag>>(emptyList())
    val suggestedTags: StateFlow<List<FeelingTag>> = _suggestedTags.asStateFlow()

    private val _weeklyInsight = MutableStateFlow("")
    val weeklyInsight: StateFlow<String> = _weeklyInsight.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _checkInReply = MutableStateFlow("")
    val checkInReply: StateFlow<String> = _checkInReply.asStateFlow()

    /** Called when user finishes typing a note — debounce in the composable. */
    fun analyseNote(note: String) {
        if (engine.state.value !is GemmaEngine.State.Ready) return
        viewModelScope.launch {
            _suggestedTags.value = engine.suggestTags(note)
        }
    }

    fun generateWeeklyInsight(entries: List<MoodEntry>) {
        if (engine.state.value !is GemmaEngine.State.Ready) return
        viewModelScope.launch {
            _weeklyInsight.value = engine.weeklyInsight(entries)
        }
    }

    fun streamCheckIn(message: String) {
        if (engine.state.value !is GemmaEngine.State.Ready) return
        viewModelScope.launch {
            _checkInReply.value = ""
            engine.streamCheckIn(message) { token ->
                _checkInReply.value += token
            }
        }
    }

    /** Download Gemma model via HTTPS to app-private storage. */
    fun downloadModel() {
        viewModelScope.launch(Dispatchers.IO) {
            val dest = engine.modelFile
            try {
                val conn = URL(GemmaEngine.MODEL_DOWNLOAD_URL).openConnection() as HttpURLConnection
                conn.connect()
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: 1L
                var downloaded = 0L
                dest.parentFile?.mkdirs()
                dest.outputStream().buffered().use { out ->
                    BufferedInputStream(conn.inputStream).use { ins ->
                        val buf = ByteArray(8192)
                        var n: Int
                        while (ins.read(buf).also { n = it } != -1) {
                            out.write(buf, 0, n)
                            downloaded += n
                            _downloadProgress.value = downloaded.toFloat() / total
                        }
                    }
                }
                _downloadProgress.value = 1f
                // Re-initialise engine on main thread
                launch(Dispatchers.Main) { engine.initLlm() }
            } catch (e: Exception) {
                Log.e("GemmaViewModel", "Model download failed", e)
                dest.delete()
                // Surface error through engine state (needs internal fun — see GemmaEngine)
            }
        }
    }

    fun clearSuggestedTags() { _suggestedTags.value = emptyList() }

    override fun onCleared() {
        super.onCleared()
        engine.close()
    }
}
