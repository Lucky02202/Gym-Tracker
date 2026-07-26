package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>

    /** Exercises most recently used, for a fast "recent exercises" picker. */
    @Query(
        """
        SELECT e.* FROM exercises e
        INNER JOIN (
            SELECT we.exerciseId AS exerciseId, MAX(w.date) AS lastUsed
            FROM workout_exercises we
            INNER JOIN workouts w ON w.id = we.workoutId
            GROUP BY we.exerciseId
        ) recent ON recent.exerciseId = e.id
        ORDER BY recent.lastUsed DESC
        LIMIT :limit
        """
    )
    fun getRecentExercises(limit: Int = 10): Flow<List<Exercise>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}
