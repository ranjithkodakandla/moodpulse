package com.moodpulse.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodpulse.app.data.datastore.AppPreferences
import com.moodpulse.app.data.datastore.UserPreferencesRepository
import com.moodpulse.app.data.repository.MoodRepository
import com.moodpulse.app.domain.ExportImport
import com.moodpulse.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val importError: String? = null,
    val importSuccess: String? = null,
    val showDeleteConfirm: Boolean = false,
    val totalEntries: Int = 0
)

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository,
    private val moodRepo: MoodRepository,
    private val context: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                prefsRepo.preferences.collect { prefs ->
                    _ui.update { it.copy(preferences = prefs) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                moodRepo.allEntries.collect { entries ->
                    _ui.update { it.copy(totalEntries = entries.size) }
                }
            } catch (_: Exception) {}
        }
    }

    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setRemindersEnabled(enabled)
        if (enabled) {
            ReminderScheduler.schedule(
                context,
                _ui.value.preferences.reminderHour,
                _ui.value.preferences.reminderMinute
            )
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        prefsRepo.setReminderTime(hour, minute)
        if (_ui.value.preferences.remindersEnabled) {
            ReminderScheduler.schedule(context, hour, minute)
        }
    }

    fun setThemeMode(mode: String) = viewModelScope.launch {
        prefsRepo.setThemeMode(mode)
    }

    fun exportJson(uri: Uri) = viewModelScope.launch {
        _ui.update { it.copy(isExporting = true, exportError = null) }
        runCatching {
            val entries = moodRepo.allEntries.first()
            val json = ExportImport.toJson(entries, "1.0.0")
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        }.onFailure { e ->
            _ui.update { it.copy(exportError = "Export failed: ${e.message}") }
        }
        _ui.update { it.copy(isExporting = false) }
    }

    fun exportCsv(uri: Uri) = viewModelScope.launch {
        _ui.update { it.copy(isExporting = true, exportError = null) }
        runCatching {
            val entries = moodRepo.allEntries.first()
            val csv = ExportImport.toCsv(entries)
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }.onFailure { e ->
            _ui.update { it.copy(exportError = "Export failed: ${e.message}") }
        }
        _ui.update { it.copy(isExporting = false) }
    }

    fun importJson(uri: Uri) = viewModelScope.launch {
        _ui.update { it.copy(importError = null, importSuccess = null) }
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: throw IllegalStateException("Cannot read file")
            val exportData = ExportImport.parseJson(json).getOrThrow()
            val entries = ExportImport.toMoodEntries(exportData)
            var imported = 0
            entries.forEach { entry ->
                moodRepo.saveEntry(entry)
                imported++
            }
            _ui.update { it.copy(importSuccess = "Imported $imported entries") }
        }.onFailure { e ->
            _ui.update { it.copy(importError = "Import failed: ${e.message}") }
        }
    }

    fun showDeleteConfirm() = _ui.update { it.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() = _ui.update { it.copy(showDeleteConfirm = false) }

    fun deleteAllData() = viewModelScope.launch {
        moodRepo.deleteAllEntries()
        _ui.update { it.copy(showDeleteConfirm = false) }
    }

    fun clearMessages() = _ui.update { it.copy(exportError = null, importError = null, importSuccess = null) }

    class Factory(
        private val prefsRepo: UserPreferencesRepository,
        private val moodRepo: MoodRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefsRepo, moodRepo, context) as T
    }
}
