package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_shift_metadata")
data class DailyShiftMetadataEntity(
    @PrimaryKey val dateIso: String, // e.g., "2026-10-30"
    val dayOperatorName: String = "",
    val dayOperatorSignature: String = "",
    val nightOperatorName: String = "",
    val nightOperatorSignature: String = "",
    val slot1Label: String = "08H00",
    val slot2Label: String = "14H00",
    val slot3Label: String = "20H00",
    val slot4Label: String = "02H00",
    val shiftObservations: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getSlotLabel(slotIndex: Int): String = when (slotIndex) {
        1 -> slot1Label.ifBlank { "08H00" }
        2 -> slot2Label.ifBlank { "14H00" }
        3 -> slot3Label.ifBlank { "20H00" }
        4 -> slot4Label.ifBlank { "02H00" }
        else -> "08H00"
    }
}

@Entity(
    tableName = "parameter_readings",
    primaryKeys = ["dateIso", "parameterId", "slotIndex"]
)
data class ParameterReadingEntity(
    val dateIso: String,
    val parameterId: Int,
    val slotIndex: Int, // 1 = 08H00, 2 = 14H00, 3 = 20H00, 4 = 02H00
    val rawValue: String = "",
    val isStoppedOrNa: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isCompleted: Boolean
        get() = isStoppedOrNa || rawValue.isNotBlank()

    val displayTableValue: String
        get() = when {
            isStoppedOrNa -> "ARRÊT"
            rawValue.isNotBlank() -> rawValue
            else -> "—"
        }
}
