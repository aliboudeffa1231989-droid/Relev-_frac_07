package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyShiftMetadataEntity::class,
        ParameterReadingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PetroLogDatabase : RoomDatabase() {
    abstract fun petroLogDao(): PetroLogDao

    companion object {
        @Volatile
        private var INSTANCE: PetroLogDatabase? = null

        fun getInstance(context: Context): PetroLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetroLogDatabase::class.java,
                    "petrolog_24h_offline.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
