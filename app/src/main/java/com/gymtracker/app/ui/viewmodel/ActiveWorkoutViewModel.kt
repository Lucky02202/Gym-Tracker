package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.entity.Exercise
import com.gymtracker.app.data.entity.Workout
import com.gymtracker.app.data.entity.WorkoutExerciseWithSets
import com.gymtracker.app.data.entity.WorkoutWithExercises
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the active/in-progress workout screen: adding exercises, logging sets,
 * and finishing or discarding the session.
 */
class ActiveWorkoutViewModel(private val repository: GymRepository) : ViewModel() {

    private var workoutId: Long = -1

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    val workout: StateFlow<WorkoutWithExercises?> = repository.getInProgressWorkout()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentExercises: StateFlow<List<Exercise>> = repository.getRecentExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startOrResume(dayName: String) {
        viewModelScope.launch {
            workoutId = if (workout.value == null) {
                repository.startWorkout(dayName)
            } else {
                workout.value!!.workout.id
            }
        }
    }

    fun prefillFromLastWorkout(dayName: String) {
        viewModelScope.launch {
            val last = repository.getLastWorkoutForDay(dayName) ?: return@launch
            val currentWorkoutId = workout.value?.workout?.id ?: return@launch
            last.exercises.sortedBy { it.workoutExercise.orderIndex }.forEach { exWithSets ->
                repository.addExerciseToWorkout(currentWorkoutId, exWithSets.exercise.id, exWithSets.workoutExercise.orderIndex)
            }
        }
    }

    fun addExercise(exerciseId: Long) {
        val w = workout.value ?: return
        viewModelScope.launch {
            val nextOrder = w.exercises.size
            repository.addExerciseToWorkout(w.workout.id, exerciseId, nextOrder)
        }
    }

    fun quickAddExercise(name: String, muscleGroup: String) {
        viewModelScope.launch {
            val newId = repository.addExercise(name, muscleGroup, null)
            addExercise(newId)
        }
    }

    fun removeExercise(exerciseWithSets: WorkoutExerciseWithSets) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(exerciseWithSets.workoutExercise)
        }
    }

    /**
     * Adds a new set to the given exercise. If a previous set exists, its weight/reps
     * are used as sensible defaults (per spec: "automatically copy the previous weight and reps").
     */
    fun addSet(exerciseWithSets: WorkoutExerciseWithSets) {
        viewModelScope.launch {
            val previous = exerciseWithSets.sets.maxByOrNull { it.setNumber }
            repository.addSet(
                workoutExerciseId = exerciseWithSets.workoutExercise.id,
                setNumber = (previous?.setNumber ?: 0) + 1,
                weight = previous?.weight ?: 0.0,
                reps = previous?.reps ?: 0,
                rir = previous?.rir,
                notes = null,
                isWarmup = false
            )
        }
    }

    fun updateSet(set: com.gymtracker.app.data.entity.WorkoutSet) {
        viewModelScope.launch { repository.updateSet(set) }
    }

    fun deleteSet(set: com.gymtracker.app.data.entity.WorkoutSet) {
        viewModelScope.launch { repository.deleteSet(set) }
    }

    fun tick() {
        _elapsedSeconds.value += 1
    }

    fun setElapsedFromStart(startTime: Long) {
        _elapsedSeconds.value = ((System.currentTimeMillis() - startTime) / 1000).toInt()
    }

    suspend fun finishWorkout(notes: String?) {
        val w = workout.value ?: return
        repository.finishWorkout(w.workout, notes)
    }

    suspend fun discardWorkout() {
        val w = workout.value ?: return
        repository.discardWorkout(w.workout)
    }
}
