package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ---------- Workout CRUD ----------

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long

    @Update
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise)

    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExercise)

    @Insert
    suspend fun insertSet(set: WorkoutSet): Long

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): Workout?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutWithExercises(id: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getInProgressWorkout(): Flow<WorkoutWithExercises?>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>>

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>>

    // ---------- History ----------

    @Transaction
    @Query("SELECT * FROM workouts WHERE isCompleted = 1 ORDER BY date DESC, startTime DESC")
    fun getAllCompletedWorkouts(): Flow<List<WorkoutWithExercises>>

    @Query(
        """
        SELECT w.id AS id, w.dayName AS dayName, w.date AS date, w.startTime AS startTime, w.endTime AS endTime,
            COALESCE(SUM(s.setCount), 0) AS totalSets,
            COALESCE(SUM(s.volume), 0.0) AS totalVolume,
            COALESCE(COUNT(DISTINCT we.id), 0) AS exerciseCount
        FROM workouts w
        LEFT JOIN workout_exercises we ON we.workoutId = w.id
        LEFT JOIN (
            SELECT workoutExerciseId, COUNT(*) AS setCount, SUM(weight * reps) AS volume
            FROM workout_sets GROUP BY workoutExerciseId
        ) s ON s.workoutExerciseId = we.id
        WHERE w.isCompleted = 1
        GROUP BY w.id
        ORDER BY w.date DESC, w.startTime DESC
        """
    )
    fun getWorkoutSummaries(): Flow<List<WorkoutSummary>>

    @Query(
        """
        SELECT w.id AS id, w.dayName AS dayName, w.date AS date, w.startTime AS startTime, w.endTime AS endTime,
            COALESCE(SUM(s.setCount), 0) AS totalSets,
            COALESCE(SUM(s.volume), 0.0) AS totalVolume,
            COALESCE(COUNT(DISTINCT we.id), 0) AS exerciseCount
        FROM workouts w
        LEFT JOIN workout_exercises we ON we.workoutId = w.id
        LEFT JOIN (
            SELECT workoutExerciseId, COUNT(*) AS setCount, SUM(weight * reps) AS volume
            FROM workout_sets GROUP BY workoutExerciseId
        ) s ON s.workoutExerciseId = we.id
        WHERE w.isCompleted = 1 AND w.date BETWEEN :startDate AND :endDate
        GROUP BY w.id
        ORDER BY w.date DESC, w.startTime DESC
        """
    )
    fun getWorkoutSummariesInRange(startDate: Long, endDate: Long): Flow<List<WorkoutSummary>>

    @Query(
        "SELECT * FROM workouts WHERE isCompleted = 1 AND (dayName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') ORDER BY date DESC"
    )
    fun searchWorkouts(query: String): Flow<List<Workout>>

    /** The most recent completed workout with the given day name — used to prefill/duplicate. */
    @Transaction
    @Query("SELECT * FROM workouts WHERE dayName = :dayName AND isCompleted = 1 ORDER BY date DESC, startTime DESC LIMIT 1")
    suspend fun getLastWorkoutForDay(dayName: String): WorkoutWithExercises?

    // ---------- Progress / PRs ----------

    @Query(
        """
        SELECT w.id AS workoutId, w.date AS date,
            MAX(s.weight) AS maxWeight,
            (SELECT s2.reps FROM workout_sets s2 WHERE s2.workoutExerciseId = we.id ORDER BY s2.weight DESC LIMIT 1) AS topSetReps,
            MAX(s.weight * (1 + s.reps / 30.0)) AS estimatedOneRepMax,
            SUM(s.weight * s.reps) AS volume
        FROM workouts w
        INNER JOIN workout_exercises we ON we.workoutId = w.id
        INNER JOIN workout_sets s ON s.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND w.isCompleted = 1 AND s.isWarmup = 0
        GROUP BY w.id
        ORDER BY w.date ASC
        """
    )
    fun getExerciseHistory(exerciseId: Long): Flow<List<ExerciseHistoryPoint>>

    @Query(
        """
        SELECT MAX(s.weight) FROM workout_sets s
        INNER JOIN workout_exercises we ON we.id = s.workoutExerciseId
        INNER JOIN workouts w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId AND w.isCompleted = 1 AND s.isWarmup = 0
        """
    )
    fun getPersonalRecordWeight(exerciseId: Long): Flow<Double?>

    @Query(
        """
        SELECT MAX(s.weight * (1 + s.reps / 30.0)) FROM workout_sets s
        INNER JOIN workout_exercises we ON we.id = s.workoutExerciseId
        INNER JOIN workouts w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId AND w.isCompleted = 1 AND s.isWarmup = 0
        """
    )
    fun getPersonalRecordEstimatedOneRepMax(exerciseId: Long): Flow<Double?>

    @Query(
        """
        SELECT MAX(vol) FROM (
            SELECT w.id, SUM(s.weight * s.reps) AS vol
            FROM workouts w
            INNER JOIN workout_exercises we ON we.workoutId = w.id
            INNER JOIN workout_sets s ON s.workoutExerciseId = we.id
            WHERE we.exerciseId = :exerciseId AND w.isCompleted = 1
            GROUP BY w.id
        )
        """
    )
    fun getPersonalRecordVolume(exerciseId: Long): Flow<Double?>

    // ---------- Statistics ----------

    @Query("SELECT COUNT(*) FROM workouts WHERE isCompleted = 1")
    fun getTotalWorkoutCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM workouts WHERE isCompleted = 1 ORDER BY date DESC")
    fun getAllWorkoutDates(): Flow<List<Long>>

    @Query("SELECT COUNT(DISTINCT exerciseId) FROM workout_exercises we INNER JOIN workouts w ON w.id = we.workoutId WHERE w.isCompleted = 1")
    fun getDistinctExercisesPerformedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_sets s INNER JOIN workout_exercises we ON we.id = s.workoutExerciseId INNER JOIN workouts w ON w.id = we.workoutId WHERE w.isCompleted = 1")
    fun getTotalSetCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(s.reps), 0) FROM workout_sets s INNER JOIN workout_exercises we ON we.id = s.workoutExerciseId INNER JOIN workouts w ON w.id = we.workoutId WHERE w.isCompleted = 1")
    fun getTotalRepCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(s.weight * s.reps), 0.0) FROM workout_sets s INNER JOIN workout_exercises we ON we.id = s.workoutExerciseId INNER JOIN workouts w ON w.id = we.workoutId WHERE w.isCompleted = 1")
    fun getTotalWeightLifted(): Flow<Double>

    @Query("SELECT AVG(endTime - startTime) FROM workouts WHERE isCompleted = 1 AND endTime IS NOT NULL")
    fun getAverageWorkoutDurationMillis(): Flow<Double?>

    @Query("SELECT * FROM workouts WHERE isCompleted = 1 AND date = :date LIMIT 1")
    suspend fun getWorkoutForDate(date: Long): Workout?

    @Transaction
    @Query("SELECT * FROM workouts WHERE isCompleted = 1 AND date = :date LIMIT 1")
    fun getWorkoutWithExercisesForDate(date: Long): Flow<WorkoutWithExercises?>
}
