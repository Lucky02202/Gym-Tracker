package com.gymtracker.app.data.repository

import com.gymtracker.app.data.dao.BodyWeightDao
import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.SettingsDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.dao.WorkoutSplitDao
import com.gymtracker.app.data.entity.*
import com.gymtracker.app.data.util.DateUtils
import com.gymtracker.app.data.util.SplitGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for all app data. ViewModels talk only to this class,
 * never to DAOs directly.
 */
class GymRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutSplitDao: WorkoutSplitDao,
    private val bodyWeightDao: BodyWeightDao,
    private val settingsDao: SettingsDao
) {

    // ---------------- Settings ----------------

    fun getSettings(): Flow<Settings> = settingsDao.getSettings().map { it ?: Settings() }

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        val current = settingsDao.getSettingsOnce() ?: Settings()
        settingsDao.upsert(transform(current))
    }

    // ---------------- Exercises ----------------

    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    fun getFavoriteExercises(): Flow<List<Exercise>> = exerciseDao.getFavoriteExercises()
    fun searchExercises(query: String): Flow<List<Exercise>> = exerciseDao.searchExercises(query)
    fun getRecentExercises(limit: Int = 10): Flow<List<Exercise>> = exerciseDao.getRecentExercises(limit)
    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getExerciseById(id)

    suspend fun addExercise(name: String, muscleGroup: String, notes: String?): Long =
        exerciseDao.insert(Exercise(name = name, muscleGroup = muscleGroup, notes = notes))

    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)
    suspend fun toggleFavorite(exercise: Exercise) = exerciseDao.update(exercise.copy(isFavorite = !exercise.isFavorite))

    // ---------------- Splits ----------------

    fun getActiveSplitWithDays(): Flow<SplitWithDays?> = workoutSplitDao.getActiveSplitWithDays()
    fun getAllSplits(): Flow<List<SplitWithDays>> = workoutSplitDao.getAllSplitsWithDays()

    /** Creates a new split for the given weekly frequency using the recommended template, and activates it. */
    suspend fun createRecommendedSplit(daysPerWeek: Int): Long {
        val names = SplitGenerator.generate(daysPerWeek)
        val weekly = SplitGenerator.spreadAcrossWeek(names)
        val splitId = workoutSplitDao.insertSplit(
            WorkoutSplit(name = SplitGenerator.splitName(daysPerWeek), daysPerWeek = daysPerWeek, isActive = false)
        )
        val days = weekly.mapIndexed { index, dayName ->
            WorkoutDay(
                splitId = splitId,
                dayIndex = index,
                name = dayName ?: "Rest",
                isRestDay = dayName == null
            )
        }
        workoutSplitDao.insertDays(days)
        workoutSplitDao.activateSplit(splitId)
        updateSettings { it.copy(activeSplitId = splitId, hasCompletedOnboarding = true) }
        return splitId
    }

    suspend fun renameWorkoutDay(day: WorkoutDay, newName: String) =
        workoutSplitDao.updateDay(day.copy(name = newName, isRestDay = false))

    suspend fun setWorkoutDayAsRest(day: WorkoutDay) =
        workoutSplitDao.updateDay(day.copy(name = "Rest", isRestDay = true))

    suspend fun activateSplit(splitId: Long) {
        workoutSplitDao.activateSplit(splitId)
        updateSettings { it.copy(activeSplitId = splitId) }
    }

    /** Returns today's WorkoutDay for the currently active split, or null if none is set up yet. */
    suspend fun getTodayWorkoutDay(): WorkoutDay? {
        val split = workoutSplitDao.getActiveSplitWithDays().first() ?: return null
        val todayIndex = DateUtils.weekdayIndex()
        return split.days.find { it.dayIndex == todayIndex }
    }

    // ---------------- Active workout logging ----------------

    fun getInProgressWorkout(): Flow<WorkoutWithExercises?> = workoutDao.getInProgressWorkout()
    fun getWorkoutWithExercises(id: Long): Flow<WorkoutWithExercises?> = workoutDao.getWorkoutWithExercises(id)

    suspend fun startWorkout(dayName: String): Long {
        return workoutDao.insertWorkout(
            Workout(dayName = dayName, date = DateUtils.startOfToday(), startTime = System.currentTimeMillis())
        )
    }

    suspend fun finishWorkout(workout: Workout, notes: String?) {
        workoutDao.updateWorkout(
            workout.copy(endTime = System.currentTimeMillis(), isCompleted = true, notes = notes)
        )
    }

    suspend fun discardWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, orderIndex: Int): Long =
        workoutDao.insertWorkoutExercise(WorkoutExercise(workoutId = workoutId, exerciseId = exerciseId, orderIndex = orderIndex))

    suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExercise) =
        workoutDao.deleteWorkoutExercise(workoutExercise)

    suspend fun addSet(workoutExerciseId: Long, setNumber: Int, weight: Double, reps: Int, rir: Int?, notes: String?, isWarmup: Boolean = false): Long =
        workoutDao.insertSet(
            WorkoutSet(
                workoutExerciseId = workoutExerciseId,
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                rir = rir,
                notes = notes,
                isWarmup = isWarmup
            )
        )

    suspend fun updateSet(set: WorkoutSet) = workoutDao.updateSet(set)
    suspend fun deleteSet(set: WorkoutSet) = workoutDao.deleteSet(set)

    /** Returns the most recent completed workout for a given day name, used to duplicate/prefill. */
    suspend fun getLastWorkoutForDay(dayName: String): WorkoutWithExercises? = workoutDao.getLastWorkoutForDay(dayName)

    /** Duplicates a previous workout's exercises (and optionally its sets) into a brand new in-progress workout. */
    suspend fun duplicateWorkout(source: WorkoutWithExercises, copySets: Boolean = true): Long {
        val newWorkoutId = startWorkout(source.workout.dayName)
        source.exercises.sortedBy { it.workoutExercise.orderIndex }.forEachIndexed { index, exWithSets ->
            val newWeId = addExerciseToWorkout(newWorkoutId, exWithSets.exercise.id, index)
            if (copySets) {
                exWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                    addSet(newWeId, set.setNumber, set.weight, set.reps, set.rir, null, set.isWarmup)
                }
            }
        }
        return newWorkoutId
    }

    // ---------------- History ----------------

    fun getWorkoutSummaries(): Flow<List<WorkoutSummary>> = workoutDao.getWorkoutSummaries()
    fun getAllCompletedWorkouts(): Flow<List<WorkoutWithExercises>> = workoutDao.getAllCompletedWorkouts()
    fun searchWorkouts(query: String): Flow<List<Workout>> = workoutDao.searchWorkouts(query)
    fun getWorkoutWithExercisesForDate(date: Long): Flow<WorkoutWithExercises?> = workoutDao.getWorkoutWithExercisesForDate(date)
    fun getWorkoutSummariesInRange(start: Long, end: Long): Flow<List<WorkoutSummary>> = workoutDao.getWorkoutSummariesInRange(start, end)

    // ---------------- Progress & PRs ----------------

    fun getExerciseHistory(exerciseId: Long): Flow<List<ExerciseHistoryPoint>> = workoutDao.getExerciseHistory(exerciseId)
    fun getPersonalRecordWeight(exerciseId: Long): Flow<Double?> = workoutDao.getPersonalRecordWeight(exerciseId)
    fun getPersonalRecordOneRepMax(exerciseId: Long): Flow<Double?> = workoutDao.getPersonalRecordEstimatedOneRepMax(exerciseId)
    fun getPersonalRecordVolume(exerciseId: Long): Flow<Double?> = workoutDao.getPersonalRecordVolume(exerciseId)

    // ---------------- Statistics ----------------

    data class Stats(
        val totalWorkouts: Int,
        val currentStreak: Int,
        val longestStreak: Int,
        val exercisesPerformed: Int,
        val totalSets: Int,
        val totalReps: Int,
        val totalWeightLifted: Double,
        val averageWorkoutDurationMillis: Long
    )

    fun getStatistics(): Flow<Stats> {
        val part1 = combine(
            workoutDao.getTotalWorkoutCount(),
            workoutDao.getAllWorkoutDates(),
            workoutDao.getDistinctExercisesPerformedCount(),
            workoutDao.getTotalSetCount(),
            workoutDao.getTotalRepCount()
        ) { totalWorkouts, dates, exercisesPerformed, totalSets, totalReps ->
            PartialStats1(totalWorkouts, dates, exercisesPerformed, totalSets, totalReps)
        }
        val part2 = combine(
            workoutDao.getTotalWeightLifted(),
            workoutDao.getAverageWorkoutDurationMillis()
        ) { totalWeight, avgDuration ->
            PartialStats2(totalWeight, avgDuration)
        }
        return combine(part1, part2) { p1, p2 ->
            Stats(
                totalWorkouts = p1.totalWorkouts,
                currentStreak = DateUtils.currentStreak(p1.dates),
                longestStreak = DateUtils.longestStreak(p1.dates),
                exercisesPerformed = p1.exercisesPerformed,
                totalSets = p1.totalSets,
                totalReps = p1.totalReps,
                totalWeightLifted = p2.totalWeight,
                averageWorkoutDurationMillis = (p2.avgDuration ?: 0.0).toLong()
            )
        }
    }

    private data class PartialStats1(
        val totalWorkouts: Int,
        val dates: List<Long>,
        val exercisesPerformed: Int,
        val totalSets: Int,
        val totalReps: Int
    )

    private data class PartialStats2(val totalWeight: Double, val avgDuration: Double?)

    // ---------------- Body weight ----------------

    fun getAllBodyWeightEntries(): Flow<List<BodyWeightEntry>> = bodyWeightDao.getAllEntries()
    fun getLatestBodyWeightEntry(): Flow<BodyWeightEntry?> = bodyWeightDao.getLatestEntry()

    suspend fun addBodyWeightEntry(date: Long, weight: Double, bodyFatPercent: Double?, notes: String?) =
        bodyWeightDao.insert(BodyWeightEntry(date = date, weight = weight, bodyFatPercent = bodyFatPercent, notes = notes))

    suspend fun updateBodyWeightEntry(entry: BodyWeightEntry) = bodyWeightDao.update(entry)
    suspend fun deleteBodyWeightEntry(entry: BodyWeightEntry) = bodyWeightDao.delete(entry)
}
