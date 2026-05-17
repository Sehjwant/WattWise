package com.fit5046.wattwise

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Appliance::class, EnergyReading::class],
    version = 2,
    exportSchema = false
)
abstract class WattWiseDatabase : RoomDatabase() {

    abstract fun applianceDao(): ApplianceDao
    abstract fun energyReadingDao(): EnergyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: WattWiseDatabase? = null

        fun getDatabase(context: Context): WattWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WattWiseDatabase::class.java,
                    "wattwise_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}