package com.moodpulse.app.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.db.MoodEntry
import com.moodpulse.app.data.repository.MoodRepository
import com.moodpulse.app.domain.FeelingTag
import com.moodpulse.app.domain.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

data class LogMoodState(
    val selectedMood: Mood? = null,
    val selectedTags: Set<FeelingTag> = emptySet(),
    val note: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val editingEntryId: Long? = null
)

class LogMoodViewModel(private val repository: MoodRepository) : ViewModel() {

    private val _state = MutableStateFlow(LogMoodState())
    val state: StateFlow<LogMoodState> = _state.asStateFlow()

    fun selectMood(mood: Mood) = _state.update { it.copy(selectedMood = mood) }

    fun toggleTag(tag: FeelingTag) = _state.update { s ->
        val tags = s.selectedTags.toMutableSet()
        if (tags.contains(tag)) tags.remove(tag) else tags.add(tag)
        s.copy(selectedTags = tags)
    }

    fun updateNote(note: String) = _state.update { it.copy(note = note) }

    fun save() {
        val mood = _state.value.selectedMood ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = MoodEntry(
                mood = mood.value,
                timestampMs = System.currentTimeMillis(),
                timezoneId = ZoneId.systemDefault().id,
                feelingTags = _state.value.selectedTags.map { it.label },
                note = _state.value.note.trim().ifBlank { null },
                source = "app"
            )
            repository.saveEntry(entry)
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun loadForEdit(entryId: Long) {
        viewModelScope.launch {
            val entry = repository.getById(entryId) ?: return@launch
            _state.update {
                it.copy(
                    editingEntryId = entryId,
                    selectedMood = Mood.fromValueOrNull(entry.mood),
                    selectedTags = entry.feelingTags
                        .mapNotNull { t -> FeelingTag.fromLabel(t) }
                        .toSet(),
                    note = entry.note ?: ""
                )
            }
        }
    }

    fun saveEdit() {
        val mood = _state.value.selectedMood ?: return
        val entryId = _state.value.editingEntryId ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val existing = repository.getById(entryId) ?: return@launch
            val updated = existing.copy(
                mood = mood.value,
                feelingTags = _state.value.selectedTags.map { it.label },
                note = _state.value.note.trim().ifBlank { null },
                updatedAtMs = System.currentTimeMillis()
            )
            repository.updateEntry(updated)
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun deleteEntry(entryId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            val entry = repository.getById(entryId) ?: return@launch
            repository.deleteEntry(entry)
            onDone()
        }
    }

    fun reset() = _state.update { LogMoodState() }

    class Factory(private val repository: MoodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            LogMoodViewModel(repository) as T
    }
}
