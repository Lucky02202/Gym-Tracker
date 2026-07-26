package com.gymtracker.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-defined exercise, e.g. "Bench Press", "Romanian Deadlift".
 * Exercises are fully custom — the user can add anything they want.
 */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Common muscle group presets shown as suggestions when adding an exercise. */
object MuscleGroups {
    val PRESETS = listOf(
        "Chest", "Back", "Shoulders", "Biceps", "Triceps",
        "Legs", "Glutes", "Core", "Forearms", "Full Body", "Cardio", "Other"
    )
}
