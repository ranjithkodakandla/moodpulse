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
    val physicalActivity: PhysicalActivityData = PhysicalActivityData(),
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val cooldownRemainingMinutes: Int = 0
)

class SmartContextViewModel(
    private val db: MoodDatabase,
    private val context: Context
) : ViewModel() {

    private val usageCollector = UsageCollector(context)
    val physicalActivityManager = PhysicalActivityManager(context)
    private val suggestionEngine: SuggestionEngine by lazy {
        (context.applicationContext as? MoodPulseApp)?.container?.suggestionEngine ?: SuggestionEngine(null)
    }

    private val _uiState = MutableStateFlow(SmartContextUiState())
    val uiState: StateFlow<SmartContextUiState> = _uiState.asStateFlow()

    companion object {
        const val COOLDOWN_MS = 60 * 60 * 1000L // 1 hour persistent cooldown
        const val MAX_SUGGESTION_AGE_MS = 6 * 60 * 60 * 1000L // 6 hours expiration
    }

    init {
        viewModelScope.launch {
            try {
                combine(
                    db.smartContextPreferencesDao().observe()
                        .catch { emit(null) }
                        .map { it ?: SmartContextPreferences() },
                    db.moodSuggestionDao().observePendingList()
                        .catch { emit(emptyList()) }
                ) { prefs, suggestionList ->
                    val now = System.currentTimeMillis()
                    // Filter out snoozed and expired suggestions
                    val activePending = suggestionList.firstOrNull { suggestion ->
                        val notSnoozed = suggestion.snoozedUntil == null || suggestion.snoozedUntil <= now
                        val notExpired = (now - suggestion.createdAt) < MAX_SUGGESTION_AGE_MS
                        notSnoozed && notExpired
                    }

                    // Expire older ones in the background
                    if (suggestionList.any { (now - it.createdAt) >= MAX_SUGGESTION_AGE_MS }) {
                        db.moodSuggestionDao().expireOlderThan(now - MAX_SUGGESTION_AGE_MS)
                    }

                    val elapsedSinceLast = now - prefs.lastSuggestionAt
                    val remainingCooldown = if (elapsedSinceLast < COOLDOWN_MS) {
                        ((COOLDOWN_MS - elapsedSinceLast) / 60_000L).toInt().coerceAtLeast(1)
                    } else 0

                    Triple(prefs, activePending, remainingCooldown)
                }.collect { (prefs, activeSuggestion, cooldownRemaining) ->
                    try {
                        val hasPermission = usageCollector.hasPermission()
                        val observations = db.behaviouralObservationDao().getRecent(10)
                        val recentCategoryTotals = loadRecentCategoryTotals()
                        val activity = physicalActivityManager.readTodayActivity()

                        _uiState.update {
                            it.copy(
                                prefs = prefs,
                                pendingSuggestion = activeSuggestion,
                                recentObservations = observations,
                                categoryTotals = recentCategoryTotals,
                                physicalActivity = activity,
                                hasPermission = hasPermission,
                                cooldownRemainingMinutes = cooldownRemaining
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
                val now = System.currentTimeMillis()
                val prefs = db.smartContextPreferencesDao().get() ?: SmartContextPreferences()

                // Check persistent 1-hour cooldown unless forced
                if (!forceGenerateSuggestion && (now - prefs.lastSuggestionAt < COOLDOWN_MS)) {
                    return@launch
                }

                // Check if an active suggestion is already waiting
                val existingPending = db.moodSuggestionDao().getPending(now, MAX_SUGGESTION_AGE_MS)
                if (existingPending != null && !forceGenerateSuggestion) {
                    return@launch
                }

                val today = usageCollector.collectTodayAggregates()
                val yesterday = usageCollector.collectYesterdayAggregates()
                val allRecent = today + yesterday
                if (allRecent.isNotEmpty()) {
                    db.appUsageAggregateDao().insertAll(allRecent)
                }

                val baselineFrom = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val baselineData = db.appUsageAggregateDao().getSince(baselineFrom)
                val analyser = BehaviourAnalyser()
                val digitalAnalysis = analyser.analyse(baselineData, if (today.isNotEmpty()) today else yesterday)

                // Biometric / Physical activity signals
                val activity = physicalActivityManager.readTodayActivity()
                val activityObservations = physicalActivityManager.evaluateActivityObservations(activity)

                val combinedObservations = digitalAnalysis.observations + activityObservations
                if (combinedObservations.isNotEmpty()) {
                    db.behaviouralObservationDao().insertAll(combinedObservations)

                    val recentMoods = db.moodDao().getRecentEntries(10).first()
                    val suggestion = suggestionEngine.generateSuggestion(combinedObservations, recentMoods)
                    if (suggestion != null) {
                        // Prevent identical repeated suggestions within 12 hours
                        val recentSuggestions = db.moodSuggestionDao().getRecent(4)
                        val isDuplicate = recentSuggestions.any {
                            it.promptText == suggestion.promptText && (now - it.createdAt < 12 * 3600 * 1000L)
                        }
                        if (!isDuplicate || forceGenerateSuggestion) {
                            db.moodSuggestionDao().insert(suggestion)
                            db.smartContextPreferencesDao().setLastSuggestionAt(now)
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
                    val now = System.currentTimeMillis()
                    db.moodSuggestionDao().insert(suggestion)
                    db.smartContextPreferencesDao().setLastSuggestionAt(now)
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
            val now = System.currentTimeMillis()
            db.moodSuggestionDao().setConfirmed(
                id = suggestionId,
                mood = confirmedMood,
                feelings = confirmedFeelings.joinToString(","),
                timestamp = now
            )
            _uiState.update { it.copy(pendingSuggestion = null) }

            // Insert real MoodEntry so it shows in today's timeline, streak, and charts!
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

    fun correctSuggestion(suggestionId: Long, correctedMood: Int, correctedFeelings: List<String>, comment: String? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            db.moodSuggestionDao().setCorrected(
                id = suggestionId,
                mood = correctedMood,
                feelings = correctedFeelings.joinToString(","),
                comment = comment,
                timestamp = now
            )
            _uiState.update { it.copy(pendingSuggestion = null) }

            val entry = MoodEntry(
                mood = correctedMood,
                timestampMs = now,
                timezoneId = ZoneId.systemDefault().id,
                feelingTags = correctedFeelings,
                note = if (!comment.isNullOrBlank()) "✦ Mindful Check-in (adjusted): $comment" else "✦ Mindful Check-in (adjusted) via Smart Context",
                source = "smart_context",
                createdAtMs = now,
                updatedAtMs = now
            )
            db.moodDao().insert(entry)
        }
    }

    fun acknowledgeSuggestion(suggestionId: Long, liked: Boolean = true, comment: String? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            db.moodSuggestionDao().setAcknowledged(
                id = suggestionId,
                liked = liked,
                comment = comment,
                timestamp = now
            )
            _uiState.update { it.copy(pendingSuggestion = null) }
        }
    }

    fun dismissSuggestion(suggestionId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            db.moodSuggestionDao().setDismissed(suggestionId, now)
            _uiState.update { it.copy(pendingSuggestion = null) }
        }
    }

    fun snoozeSuggestion(suggestionId: Long, durationMinutes: Int = 60) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            db.moodSuggestionDao().setSnoozed(
                id = suggestionId,
                untilTimestamp = now + durationMinutes * 60_000L,
                timestamp = now
            )
            _uiState.update { it.copy(pendingSuggestion = null) }
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
