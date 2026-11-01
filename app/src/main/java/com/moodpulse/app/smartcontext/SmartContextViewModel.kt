package com.moodpulse.app.smartcontext

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.MoodPulseApp
import com.moodpulse.app.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SmartContextUiState(
    val prefs: SmartContextPreferences = SmartContextPreferences(),
    val pendingSuggestion: MoodSuggestion? = null,
    val recentObservations: List<BehaviouralObservation> = emptyList(),
    val categoryTotals: Map<String, Int> = emptyMap(),
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false
)

class SmartContextViewModel(
    private val db: MoodDatabase,
    private val context: Context
) : ViewModel() {

    private val usageCollector = UsageCollector(context)
    private val suggestionEngine: SuggestionEngine by lazy {
        (context.applicationContext as? MoodPulseApp)?.container?.suggestionEngine ?: SuggestionEngine(null)
    }

    private val _uiState = MutableStateFlow(SmartContextUiState())
    val uiState: StateFlow<SmartContextUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    db.smartContextPreferencesDao().observe()
                        .catch { emit(null) }
                        .map { it ?: SmartContextPreferences() },
                    db.moodSuggestionDao().observePending()
                        .catch { emit(null) }
                ) { prefs, suggestion ->
                    prefs to suggestion
                }.collect { (prefs, suggestion) ->
                    try {
                        val hasPermission = usageCollector.hasPermission()
                        val observations = db.behaviouralObservationDao().getRecent(10)
                        val recentCategoryTotals = loadRecentCategoryTotals()
                        _uiState.update {
                            it.copy(
                                prefs = prefs,
                                pendingSuggestion = suggestion,
                                recentObservations = observations,
                                categoryTotals = recentCategoryTotals,
                                hasPermission = hasPermission
                            )
                        }
                    } catch (_: Exception) { /* keep last known state */ }
                }
            } catch (_: Exception) { /* db unavailable, stay on defaults */ }
        }

        // Auto-refresh habit data and check if an AI suggestion should be generated
        refreshHabits()
    }

    fun hasPermission(): Boolean = usageCollector.hasPermission()

    fun refreshPermission() {
        val granted = usageCollector.hasPermission()
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) {
            refreshHabits()
        }
    }

    fun openPermissionSettings() {
        usageCollector.openPermissionSettings()
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !usageCollector.hasPermission()) {
                refreshPermission()
                if (!usageCollector.hasPermission()) return@launch
            }
            val current = db.smartContextPreferencesDao().get() ?: SmartContextPreferences()
            val updated = current.copy(monitoringEnabled = enabled)
            db.smartContextPreferencesDao().insert(updated)
            _uiState.update { it.copy(prefs = updated, hasPermission = usageCollector.hasPermission()) }
            if (enabled) {
                SmartContextWorker.schedule(context)
                refreshHabits(forceGenerateSuggestion = true)
            } else {
                SmartContextWorker.cancel(context)
            }
        }
    }

    fun refreshHabits(forceGenerateSuggestion: Boolean = false) {
        if (!usageCollector.hasPermission()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = usageCollector.collectTodayAggregates()
                val yesterday = usageCollector.collectYesterdayAggregates()
                val allRecent = today + yesterday
                if (allRecent.isNotEmpty()) {
                    db.appUsageAggregateDao().insertAll(allRecent)
                }

                val baselineFrom = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val baselineData = db.appUsageAggregateDao().getSince(baselineFrom)
                val analyser = BehaviourAnalyser()
                val analysis = analyser.analyse(baselineData, if (today.isNotEmpty()) today else yesterday)

                if (analysis.observations.isNotEmpty()) {
                    db.behaviouralObservationDao().insertAll(analysis.observations)

                    val existingPending = db.moodSuggestionDao().getPending()
                    if (existingPending == null || forceGenerateSuggestion) {
                        val recentMoods = db.moodDao().getRecentEntries(10).first()
                        val suggestion = suggestionEngine.generateSuggestion(analysis.observations, recentMoods)
                        if (suggestion != null) {
                            db.moodSuggestionDao().insert(suggestion)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    /** Manually triggers an AI Mindful Check-in from habits */
    fun generateSuggestionFromHabits(targetCategory: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = usageCollector.collectTodayAggregates()
                val yesterday = usageCollector.collectYesterdayAggregates()
                val activeData = if (today.isNotEmpty()) today else yesterday
                val analyser = BehaviourAnalyser()
                val baselineFrom = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val baselineData = db.appUsageAggregateDao().getSince(baselineFrom)
                val analysis = analyser.analyse(baselineData, activeData)

                val obs = analysis.observations.ifEmpty {
                    val cat = targetCategory ?: "SOCIAL"
                    listOf(
                        BehaviouralObservation(
                            observationType = "HIGH_$cat",
                            dateTime = System.currentTimeMillis(),
                            baselineValue = 0f,
                            currentValue = 45f,
                            evidenceJson = """{"category":"$cat","reason":"manual_checkin"}"""
                        )
                    )
                }
                db.behaviouralObservationDao().insertAll(obs)
                val recentMoods = db.moodDao().getRecentEntries(10).first()
                val suggestion = suggestionEngine.generateSuggestion(obs, recentMoods)
                if (suggestion != null) {
                    db.moodSuggestionDao().insert(suggestion)
                }
            } catch (_: Throwable) {}
        }
    }

    fun updatePreferences(update: SmartContextPreferences.() -> SmartContextPreferences) {
        viewModelScope.launch {
            val current = db.smartContextPreferencesDao().get() ?: SmartContextPreferences()
            db.smartContextPreferencesDao().insert(current.update())
        }
    }

    fun confirmSuggestion(suggestionId: Long, confirmedMood: Int, confirmedFeelings: List<String>) {
        viewModelScope.launch {
            db.moodSuggestionDao().setCorrection(
                id = suggestionId,
                decision = "CONFIRMED",
                mood = confirmedMood,
                feelings = confirmedFeelings.joinToString(",")
            )

            // Insert real MoodEntry so it shows in today's timeline, streak, and charts!
            val now = System.currentTimeMillis()
            val entry = MoodEntry(
                mood = confirmedMood,
                timestampMs = now,
                timezoneId = ZoneId.systemDefault().id,
                feelingTags = confirmedFeelings,
                note = "✦ Mindful Check-in via Smart Context digital habit tracking",
                source = "smart_context",
                createdAtMs = now,
                updatedAtMs = now
            )
            db.moodDao().insert(entry)
        }
    }

    fun dismissSuggestion(suggestionId: Long) {
        viewModelScope.launch {
            db.moodSuggestionDao().setDecision(suggestionId, "DISMISSED")
        }
    }

    fun correctSuggestion(suggestionId: Long, correctedMood: Int, correctedFeelings: List<String>) {
        viewModelScope.launch {
            db.moodSuggestionDao().setCorrection(
                id = suggestionId,
                decision = "CORRECTED",
                mood = correctedMood,
                feelings = correctedFeelings.joinToString(",")
            )
            val now = System.currentTimeMillis()
            val entry = MoodEntry(
                mood = correctedMood,
                timestampMs = now,
                timezoneId = ZoneId.systemDefault().id,
                feelingTags = correctedFeelings,
                note = "✦ Mindful Check-in (adjusted) via Smart Context",
                source = "smart_context",
                createdAtMs = now,
                updatedAtMs = now
            )
            db.moodDao().insert(entry)
        }
    }

    private suspend fun loadRecentCategoryTotals(): Map<String, Int> {
        val fromDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
        return db.appUsageAggregateDao().getSince(fromDate)
            .groupBy { it.appCategory }
            .mapValues { (_, list) -> list.sumOf { it.totalMinutes } }
    }

    class Factory(
        private val db: MoodDatabase,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SmartContextViewModel(db, context) as T
    }
}
