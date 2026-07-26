package com.gymtracker.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/** A WorkoutExercise together with the Exercise it refers to and all logged sets. */
data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExercise,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: Exercise,
    @Relation(parentColumn = "id", entityColumn = "workoutExerciseId")
    val sets: List<WorkoutSet>
)

/** A full workout with every exercise and set performed during it. */
data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = WorkoutExercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)

/** A split together with its ordered days. */
data class SplitWithDays(
    @Embedded val split: WorkoutSplit,
    @Relation(parentColumn = "id", entityColumn = "splitId")
    val days: List<WorkoutDay>
)

/** Lightweight summary row used for history list / calendar, avoids loading full set data. */
data class WorkoutSummary(
    val id: Long,
    val dayName: String,
    val date: Long,
    val startTime: Long,
    val endTime: Long?,
    val totalSets: Int,
    val totalVolume: Double,
    val exerciseCount: Int
)

/** One point in an exercise's progress history, used for charts. */
data class ExerciseHistoryPoint(
    val workoutId: Long,
    val date: Long,
    val maxWeight: Double,
    val topSetReps: Int,
    val estimatedOneRepMax: Double,
    val volume: Double
)
