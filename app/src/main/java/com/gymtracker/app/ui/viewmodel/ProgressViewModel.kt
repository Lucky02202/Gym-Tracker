package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.Exercise
import com.gymtracker.app.data.entity.ExerciseHistoryPoint
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgressViewModel(private val repository: GymRepository) : ViewModel() {

    val exercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class ExerciseProgressUiState(
    val exercise: Exercise? = null,
    val history: List<ExerciseHistoryPoint> = emptyList(),
    val prWeight: Double? = null,
    val prOneRepMax: Double? = null,
    val prVolume: Double? = null
)

class ExerciseProgressViewModel(private val repository: GymRepository) : ViewModel() {

    private val _exerciseId = MutableStateFlow<Long?>(null)

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    val uiState: StateFlow<ExerciseProgressUiState> = _exerciseId
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                repository.getExerciseHistory(id),
                repository.getPersonalRecordWeight(id),
                repository.getPersonalRecordOneRepMax(id),
                repository.getPersonalRecordVolume(id)
            ) { history, prWeight, pr1rm, prVolume ->
                ExerciseProgressUiState(history = history, prWeight = prWeight, prOneRepMax = pr1rm, prVolume = prVolume)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseProgressUiState())

    fun load(exerciseId: Long) {
        _exerciseId.value = exerciseId
        viewModelScope.launch {
            _exercise.value = repository.getExerciseById(exerciseId)
        }
    }
}
