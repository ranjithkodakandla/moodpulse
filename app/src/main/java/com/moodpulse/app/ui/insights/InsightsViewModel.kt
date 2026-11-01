package com.moodpulse.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.repository.MoodRepository
import com.moodpulse.app.domain.InsightsEngine
import com.moodpulse.app.domain.WeeklyInsights
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class InsightsUiState(
    val currentWeek: WeeklyInsights? = null,
    val observation: String = "",
    val totalAllTime: Int = 0,
    val isLoading: Boolean = true
)

class InsightsViewModel(private val repository: MoodRepository) : ViewModel() {

    private val zone = ZoneId.systemDefault()

    val uiState: StateFlow<InsightsUiState> = repository.allEntries.map { all ->
        val now = Instant.now().atZone(zone)
        val weekStart = now.toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant()
        val weekEnd = weekStart.plusSeconds(7 * 24 * 3600)
        val prevWeekStart = weekStart.minusSeconds(7 * 24 * 3600)

        val thisWeek = all.filter { e ->
            val ts = Instant.ofEpochMilli(e.timestampMs)
            ts >= weekStart && ts < weekEnd
        }
        val prevWeek = all.filter { e ->
            val ts = Instant.ofEpochMilli(e.timestampMs)
            ts >= prevWeekStart && ts < weekStart
        }

        val insights = InsightsEngine.computeWeeklyInsights(
            currentWeekEntries = thisWeek,
            prevWeekEntries = prevWeek,
            allEntries = all,
            weekStartMs = weekStart.toEpochMilli(),
            weekEndMs = weekEnd.toEpochMilli(),
            zoneId = zone
        )

        InsightsUiState(
            currentWeek = insights,
            observation = InsightsEngine.generateObservation(insights),
            totalAllTime = all.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InsightsUiState()
    )

    class Factory(private val repository: MoodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            InsightsViewModel(repository) as T
    }
}
