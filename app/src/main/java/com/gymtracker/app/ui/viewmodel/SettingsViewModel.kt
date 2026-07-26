package com.gymtracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.Settings
import com.gymtracker.app.data.entity.SplitWithDays
import com.gymtracker.app.data.entity.WorkoutDay
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.data.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BackupEvent {
    object Success : BackupEvent()
    data class Error(val message: String) : BackupEvent()
}

class SettingsViewModel(private val repository: GymRepository) : ViewModel() {

    val settings: StateFlow<Settings> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    val activeSplit: StateFlow<SplitWithDays?> = repository.getActiveSplitWithDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSplits: StateFlow<List<SplitWithDays>> = repository.getAllSplits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _backupEvent = MutableStateFlow<BackupEvent?>(null)
    val backupEvent: StateFlow<BackupEvent?> = _backupEvent.asStateFlow()

    fun setUnits(units: String) {
        viewModelScope.launch { repository.updateSettings { it.copy(units = units) } }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.updateSettings { it.copy(themeMode = mode) } }
    }

    fun setDefaultRestTimer(seconds: Int) {
        viewModelScope.launch { repository.updateSettings { it.copy(defaultRestTimerSeconds = seconds) } }
    }

    fun changeSplitFrequency(daysPerWeek: Int) {
        viewModelScope.launch { repository.createRecommendedSplit(daysPerWeek) }
    }

    fun activateSplit(splitId: Long) {
        viewModelScope.launch { repository.activateSplit(splitId) }
    }

    fun renameDay(day: WorkoutDay, newName: String) {
        viewModelScope.launch { repository.renameWorkoutDay(day, newName) }
    }

    fun setDayAsRest(day: WorkoutDay) {
        viewModelScope.launch { repository.setWorkoutDayAsRest(day) }
    }

    fun exportBackup(context: Context, destination: Uri) {
        viewModelScope.launch {
            val result = BackupManager.exportBackup(context, destination)
            _backupEvent.value = result.fold(
                onSuccess = { BackupEvent.Success },
                onFailure = { BackupEvent.Error(it.message ?: "Export failed") }
            )
        }
    }

    fun importBackup(context: Context, source: Uri) {
        viewModelScope.launch {
            val result = BackupManager.importBackup(context, source)
            _backupEvent.value = result.fold(
                onSuccess = { BackupEvent.Success },
                onFailure = { BackupEvent.Error(it.message ?: "Import failed") }
            )
        }
    }

    fun clearBackupEvent() {
        _backupEvent.value = null
    }
}
