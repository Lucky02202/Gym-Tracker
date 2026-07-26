package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(repository: GymRepository) : ViewModel() {

    val stats: StateFlow<GymRepository.Stats> = repository.getStatistics()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GymRepository.Stats(0, 0, 0, 0, 0, 0, 0.0, 0)
        )
}
