package com.fit5046.wattwise

import kotlinx.coroutines.flow.Flow

class EnergyReadingRepository(private val dao: EnergyReadingDao) {

    // ── Insert ────────────────────────────────────────────────────────────────
    suspend fun insert(reading: EnergyReading) = dao.insert(reading)

    suspend fun insertAll(readings: List<EnergyReading>) = dao.insertAll(readings)

    // ── Queries ───────────────────────────────────────────────────────────────
    fun getReadingsByDateRange(fromDate: String, toDate: String): Flow<List<EnergyReading>> =
        dao.getReadingsByDateRange(fromDate, toDate)

    fun getDailyTotals(fromDate: String, toDate: String): Flow<List<DailyTotal>> =
        dao.getDailyTotals(fromDate, toDate)

    fun getCategoryBreakdown(fromDate: String, toDate: String): Flow<List<CategoryTotal>> =
        dao.getCategoryBreakdown(fromDate, toDate)

    // ── Seeding ───────────────────────────────────────────────────────────────
    suspend fun getCount(): Int = dao.getCount()

    suspend fun deleteAll() = dao.deleteAll()
}