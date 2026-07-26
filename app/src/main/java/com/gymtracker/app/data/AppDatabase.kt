package com.gymtracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gymtracker.app.data.dao.BodyWeightDao
import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.SettingsDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.dao.WorkoutSplitDao
import com.gymtracker.app.data.entity.BodyWeightEntry
import com.gymtracker.app.data.entity.Exercise
import com.gymtracker.app.data.entity.Settings
import com.gymtracker.app.data.entity.Workout
import com.gymtracker.app.data.entity.WorkoutDay
import com.gymtracker.app.data.entity.WorkoutExercise
import com.gymtracker.app.data.entity.WorkoutSet
import com.gymtracker.app.data.entity.WorkoutSplit

@Database(
    entities = [
        Exercise::class,
        Workout::class,
        WorkoutExercise::class,
        WorkoutSet::class,
        WorkoutSplit::class,
        WorkoutDay::class,
        BodyWeightEntry::class,
        Settings::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSplitDao(): WorkoutSplitDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "gym_progress_tracker.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }

        /**
         * Closes and clears the current instance. Used by the backup/restore flow, which
         * copies raw database files on disk and must not hold an open connection while doing so.
         */
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
