package com.moodpulse.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.repository.MoodRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HistoryUiState(
    val entriesByDate: Map<LocalDate, List<MoodEntry>> = emptyMap(),
    val allDatesWithEntries: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateEntries: List<MoodEntry> = emptyList(),
    val isLoading: Boolean = true
)

class HistoryViewModel(private val repository: MoodRepository) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.allEntries,
        _selectedDate
    ) { all, selected ->
        val byDate = all.groupBy { entry ->
            Instant.ofEpochMilli(entry.timestampMs).atZone(zone).toLocalDate()
        }
        HistoryUiState(
            entriesByDate = byDate,
            allDatesWithEntries = byDate.keys,
            selectedDate = selected,
            selectedDateEntries = (byDate[selected] ?: emptyList()).sortedByDescending { it.timestampMs },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun selectDate(date: LocalDate) = _selectedDate.update { date }

    class Factory(private val repository: MoodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            HistoryViewModel(repository) as T
    }
}
