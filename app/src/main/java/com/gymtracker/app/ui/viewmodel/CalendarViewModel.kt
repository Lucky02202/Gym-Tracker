package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.WorkoutSummary
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CalendarViewModel(repository: GymRepository) : ViewModel() {

    /** All completed workout summaries; the UI groups these by month/day for the calendar grid. */
    val summaries: StateFlow<List<WorkoutSummary>> = repository.getWorkoutSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
