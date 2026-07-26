package com.gymtracker.app

import android.app.Application
import com.gymtracker.app.data.AppDatabase
import com.gymtracker.app.data.repository.GymRepository

/**
 * Application class. Holds a single lazily-created instance of the database and repository,
 * acting as a lightweight manual DI container (no Hilt/Dagger needed for this app's size).
 */
class GymTrackerApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: GymRepository by lazy {
        GymRepository(
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao(),
            workoutSplitDao = database.workoutSplitDao(),
            bodyWeightDao = database.bodyWeightDao(),
            settingsDao = database.settingsDao()
        )
    }
}
