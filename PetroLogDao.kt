package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetroLogDao {

    @Query("SELECT * FROM daily_shift_metadata WHERE dateIso = :dateIso LIMIT 1")
    fun observeShiftMetadata(dateIso: String): Flow<DailyShiftMetadataEntity?>

    @Query("SELECT * FROM daily_shift_metadata WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getShiftMetadataOnce(dateIso: String): DailyShiftMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShiftMetadata(metadata: DailyShiftMetadataEntity)

    @Query("SELECT * FROM parameter_readings WHERE dateIso = :dateIso")
    fun observeReadingsForDate(dateIso: String): Flow<List<ParameterReadingEntity>>

    @Query("SELECT * FROM parameter_readings WHERE dateIso = :dateIso")
    suspend fun getReadingsForDateOnce(dateIso: String): List<ParameterReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReading(reading: ParameterReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReadings(readings: List<ParameterReadingEntity>)

    @Query("DELETE FROM parameter_readings WHERE dateIso = :dateIso AND slotIndex = :slotIndex")
    suspend fun clearSlotReadings(dateIso: String, slotIndex: Int)

    @Query("SELECT DISTINCT dateIso FROM parameter_readings ORDER BY dateIso DESC")
    fun observeLoggedDates(): Flow<List<String>>
}
