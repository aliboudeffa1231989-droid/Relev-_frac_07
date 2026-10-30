package com.example.data

import com.example.data.local.DailyShiftMetadataEntity
import com.example.data.local.ParameterReadingEntity
import com.example.data.local.PetroLogDao
import kotlinx.coroutines.flow.Flow

class PetroLogRepository(private val dao: PetroLogDao) {

    fun observeShiftMetadata(dateIso: String): Flow<DailyShiftMetadataEntity?> =
        dao.observeShiftMetadata(dateIso)

    fun observeReadingsForDate(dateIso: String): Flow<List<ParameterReadingEntity>> =
        dao.observeReadingsForDate(dateIso)

    fun observeLoggedDates(): Flow<List<String>> =
        dao.observeLoggedDates()

    suspend fun updateShiftOperators(
        dateIso: String,
        dayOperatorName: String,
        dayOperatorSignature: String,
        nightOperatorName: String,
        nightOperatorSignature: String,
        shiftObservations: String
    ) {
        val current = dao.getShiftMetadataOnce(dateIso) ?: DailyShiftMetadataEntity(dateIso = dateIso)
        dao.upsertShiftMetadata(
            current.copy(
                dayOperatorName = dayOperatorName,
                dayOperatorSignature = dayOperatorSignature,
                nightOperatorName = nightOperatorName,
                nightOperatorSignature = nightOperatorSignature,
                shiftObservations = shiftObservations,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSlotLabel(
        dateIso: String,
        slotIndex: Int,
        newLabel: String
    ) {
        val sanitized = newLabel.trim().ifEmpty {
            when (slotIndex) {
                1 -> "08H00"
                2 -> "14H00"
                3 -> "20H00"
                else -> "02H00"
            }
        }
        val current = dao.getShiftMetadataOnce(dateIso) ?: DailyShiftMetadataEntity(dateIso = dateIso)
        val updated = when (slotIndex) {
            1 -> current.copy(slot1Label = sanitized, updatedAt = System.currentTimeMillis())
            2 -> current.copy(slot2Label = sanitized, updatedAt = System.currentTimeMillis())
            3 -> current.copy(slot3Label = sanitized, updatedAt = System.currentTimeMillis())
            4 -> current.copy(slot4Label = sanitized, updatedAt = System.currentTimeMillis())
            else -> current
        }
        dao.upsertShiftMetadata(updated)
    }

    suspend fun saveReadingValue(
        dateIso: String,
        parameterId: Int,
        slotIndex: Int,
        rawValue: String,
        isStoppedOrNa: Boolean
    ) {
        val sanitizedValue = rawValue.replace(',', '.').trim()
        dao.upsertReading(
            ParameterReadingEntity(
                dateIso = dateIso,
                parameterId = parameterId,
                slotIndex = slotIndex,
                rawValue = if (isStoppedOrNa) "" else sanitizedValue,
                isStoppedOrNa = isStoppedOrNa,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun copyPreviousSlotToCurrent(
        dateIso: String,
        fromSlotIndex: Int,
        toSlotIndex: Int
    ) {
        val allReadings = dao.getReadingsForDateOnce(dateIso)
        val sourceReadings = allReadings.filter { it.slotIndex == fromSlotIndex && it.isCompleted }
        if (sourceReadings.isEmpty()) return
        val existingTargetMap = allReadings
            .filter { it.slotIndex == toSlotIndex && it.isCompleted }
            .associateBy { it.parameterId }

        val toInsert = sourceReadings.mapNotNull { src ->
            if (existingTargetMap.containsKey(src.parameterId)) {
                null // Do not overwrite already filled entries in current slot
            } else {
                src.copy(
                    slotIndex = toSlotIndex,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            dao.upsertReadings(toInsert)
        }
    }

    suspend fun clearSlotReadings(dateIso: String, slotIndex: Int) {
        dao.clearSlotReadings(dateIso, slotIndex)
    }
}
