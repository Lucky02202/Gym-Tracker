package com.gymtracker.app.data.dao

import androidx.room.*
import com.gymtracker.app.data.entity.BodyWeightEntry
import com.gymtracker.app.data.entity.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {

    @Insert
    suspend fun insert(entry: BodyWeightEntry): Long

    @Update
    suspend fun update(entry: BodyWeightEntry)

    @Delete
    suspend fun delete(entry: BodyWeightEntry)

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<BodyWeightEntry>>

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestEntry(): Flow<BodyWeightEntry?>

    @Query("SELECT * FROM body_weight_entries WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun getEntriesInRange(start: Long, end: Long): Flow<List<BodyWeightEntry>>
}

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: Settings)
}
