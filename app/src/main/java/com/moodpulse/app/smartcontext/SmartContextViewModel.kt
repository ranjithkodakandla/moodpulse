package com.moodpulse.app.smartcontext

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.db.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    }

    fun openPermissionSettings() {
        usageCollector.openPermissionSettings()
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = db.smartContextPreferencesDao().get()
            if (current == null) {
                db.smartContextPreferencesDao().insert(SmartContextPreferences(monitoringEnabled = enabled))
            } else {
                db.smartContextPreferencesDao().setMonitoringEnabled(enabled)
            }
            if (enabled) {
                SmartContextWorker.schedule(context)
            } else {
                SmartContextWorker.cancel(context)
            }
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
