package com.fit5046.wattwise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EnergyReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: EnergyReading)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<EnergyReading>)

    @Query("SELECT * FROM energy_readings WHERE date BETWEEN :fromDate AND :toDate ORDER BY date, hour")
    fun getReadingsByDateRange(fromDate: String, toDate: String): Flow<List<EnergyReading>>

    @Query("SELECT date, SUM(totalKwh) as totalKwh FROM energy_readings WHERE date BETWEEN :fromDate AND :toDate GROUP BY date ORDER BY date")
    fun getDailyTotals(fromDate: String, toDate: String): Flow<List<DailyTotal>>

    @Query("SELECT category, SUM(totalKwh) as totalKwh FROM energy_readings WHERE date BETWEEN :fromDate AND :toDate GROUP BY category")
    fun getCategoryBreakdown(fromDate: String, toDate: String): Flow<List<CategoryTotal>>

    @Query("SELECT COUNT(*) FROM energy_readings")
    suspend fun getCount(): Int

    @Query("DELETE FROM energy_readings")
    suspend fun deleteAll()
}

data class DailyTotal(val date: String, val totalKwh: Double)
data class CategoryTotal(val category: String, val totalKwh: Double)