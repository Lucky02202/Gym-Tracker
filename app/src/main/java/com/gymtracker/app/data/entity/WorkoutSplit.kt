package com.gymtracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents the user's chosen training split, e.g. "3-day Push/Pull/Legs".
 * Only one split is active at a time (isActive = true).
 */
@Entity(tableName = "workout_splits")
data class WorkoutSplit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val daysPerWeek: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A single day within a split, e.g. "Push", "Pull", "Legs", "Rest".
 * dayIndex is the position within the rotation (0-based), which is mapped
 * onto real calendar days starting from the split's creation/anchor date.
 */
@Entity(
    tableName = "workout_days",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSplit::class,
            parentColumns = ["id"],
            childColumns = ["splitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: Long,
    val dayIndex: Int,
    val name: String,
    val isRestDay: Boolean = false
)
