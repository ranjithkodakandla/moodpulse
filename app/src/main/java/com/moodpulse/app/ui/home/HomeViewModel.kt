package com.moodpulse.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.db.MoodDatabase
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.repository.MoodRepository
import com.moodpulse.app.domain.InsightsEngine
import com.moodpulse.app.domain.Mood
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val todayEntries: List<MoodEntry> = emptyList(),
    val recentEntries: List<MoodEntry> = emptyList(),
    val todayObservation: String? = null,
    val streakDays: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(private val repository: MoodRepository) : ViewModel() {

    private val zone = ZoneId.systemDefault()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allEntries,
        repository.getRecentEntries(50)
    ) { all, recent ->
        val now = Instant.now()
        val todayStart = now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()
        val todayEnd = todayStart.plus(1, ChronoUnit.DAYS)
        val todayEntries = all.filter {
            val ts = Instant.ofEpochMilli(it.timestampMs)
            ts >= todayStart && ts < todayEnd
        }
        val streak = InsightsEngine.computeStreak(all, zone)
        val observation = if (todayEntries.size >= 2) {
            val avg = todayEntries.map { it.mood }.average()
            val mood = Mood.fromValueOrNull(avg.toInt().coerceIn(1, 5))
            mood?.let { "You've been feeling ${it.label.lowercase()} today overall." }
        } else null

        HomeUiState(
            todayEntries = todayEntries,
            recentEntries = recent.take(10),
            todayObservation = observation,
            streakDays = streak,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    class Factory(
        private val repository: MoodRepository,
        @Suppress("UNUSED_PARAMETER") private val database: MoodDatabase? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
