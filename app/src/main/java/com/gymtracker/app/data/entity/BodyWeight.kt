package com.gymtracker.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single bodyweight/body-fat log entry. */
@Entity(tableName = "body_weight_entries")
data class BodyWeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weight: Double,
    val bodyFatPercent: Double? = null,
    val notes: String? = null
)

/**
 * Single-row table holding app-wide settings.
 * We always read/write the row with id = 1.
 */
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val units: String = "kg",              // "kg" or "lbs"
    val themeMode: String = "system",      // "system", "light", "dark"
    val activeSplitId: Long? = null,
    val defaultRestTimerSeconds: Int = 90,
    val hasCompletedOnboarding: Boolean = false
)
