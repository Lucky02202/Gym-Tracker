package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.entity.SplitWithDays
import com.gymtracker.app.data.entity.WorkoutDay
import com.gymtracker.app.data.entity.WorkoutSplit
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSplitDao {

    @Insert
    suspend fun insertSplit(split: WorkoutSplit): Long

    @Insert
    suspend fun insertDays(days: List<WorkoutDay>)

    @Update
    suspend fun updateSplit(split: WorkoutSplit)

    @Update
    suspend fun updateDay(day: WorkoutDay)

    @Query("UPDATE workout_splits SET isActive = 0")
    suspend fun deactivateAllSplits()

    @Transaction
    @Query("SELECT * FROM workout_splits WHERE isActive = 1 LIMIT 1")
    fun getActiveSplitWithDays(): Flow<SplitWithDays?>

    @Transaction
    @Query("SELECT * FROM workout_splits ORDER BY createdAt DESC")
    fun getAllSplitsWithDays(): Flow<List<SplitWithDays>>

    @Query("SELECT * FROM workout_days WHERE splitId = :splitId ORDER BY dayIndex ASC")
    fun getDaysForSplit(splitId: Long): Flow<List<WorkoutDay>>

    @Delete
    suspend fun deleteSplit(split: WorkoutSplit)

    /** Sets a new split as active in one atomic operation. */
    @Transaction
    suspend fun activateSplit(splitId: Long) {
        deactivateAllSplits()
        setSplitActive(splitId)
    }

    @Query("UPDATE workout_splits SET isActive = 1 WHERE id = :splitId")
    suspend fun setSplitActive(splitId: Long)
}
