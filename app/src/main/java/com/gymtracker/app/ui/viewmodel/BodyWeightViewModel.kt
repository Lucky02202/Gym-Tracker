package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.BodyWeightEntry
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyWeightViewModel(private val repository: GymRepository) : ViewModel() {

    val entries: StateFlow<List<BodyWeightEntry>> = repository.getAllBodyWeightEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(date: Long, weight: Double, bodyFatPercent: Double?, notes: String?) {
        viewModelScope.launch {
            repository.addBodyWeightEntry(date, weight, bodyFatPercent, notes)
        }
    }

    fun updateEntry(entry: BodyWeightEntry) {
        viewModelScope.launch { repository.updateBodyWeightEntry(entry) }
    }

    fun deleteEntry(entry: BodyWeightEntry) {
        viewModelScope.launch { repository.deleteBodyWeightEntry(entry) }
    }
}
