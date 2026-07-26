package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.WorkoutSummary
import com.gymtracker.app.data.entity.WorkoutWithExercises
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: GymRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val summaries: StateFlow<List<WorkoutSummary>> = repository.getWorkoutSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSummaries: StateFlow<List<WorkoutSummary>> = combine(summaries, _query) { list, q ->
        if (q.isBlank()) list else list.filter { it.dayName.contains(q, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) {
        _query.value = q
    }
}

class WorkoutDetailViewModel(private val repository: GymRepository) : ViewModel() {

    private val _workoutId = MutableStateFlow<Long?>(null)

    val workout: StateFlow<WorkoutWithExercises?> = _workoutId
        .filterNotNull()
        .flatMapLatest { id -> repository.getWorkoutWithExercises(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(workoutId: Long) {
        _workoutId.value = workoutId
    }

    fun duplicateAsNewWorkout(onDone: (Long) -> Unit) {
        val w = workout.value ?: return
        viewModelScope.launch {
            val newId = repository.duplicateWorkout(w, copySets = true)
            onDone(newId)
        }
    }
}
