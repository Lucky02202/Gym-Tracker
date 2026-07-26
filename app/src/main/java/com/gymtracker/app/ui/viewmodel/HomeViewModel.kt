package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.WorkoutDay
import com.gymtracker.app.data.entity.WorkoutWithExercises
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasActiveSplit: Boolean = false,
    val todayDayName: String? = null,
    val isRestDay: Boolean = false,
    val inProgressWorkout: WorkoutWithExercises? = null,
    val lastWorkoutForToday: WorkoutWithExercises? = null,
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0
)

class HomeViewModel(private val repository: GymRepository) : ViewModel() {

    private val _todayDay = MutableStateFlow<WorkoutDay?>(null)
    private val _loading = MutableStateFlow(true)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getActiveSplitWithDays(),
        repository.getInProgressWorkout(),
        repository.getStatistics(),
        _todayDay,
        _loading
    ) { split, inProgress, stats, todayDay, loading ->
        HomeUiState(
            isLoading = loading,
            hasActiveSplit = split != null,
            todayDayName = todayDay?.name,
            isRestDay = todayDay?.isRestDay ?: false,
            inProgressWorkout = inProgress,
            totalWorkouts = stats.totalWorkouts,
            currentStreak = stats.currentStreak
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        refreshToday()
    }

    fun refreshToday() {
        viewModelScope.launch {
            _loading.value = true
            _todayDay.value = repository.getTodayWorkoutDay()
            _loading.value = false
        }
    }

    suspend fun startTodayWorkout(): Long? {
        val dayName = _todayDay.value?.name ?: return null
        return repository.startWorkout(dayName)
    }
}
