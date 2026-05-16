package com.fit5046.wattwise

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * SmartMeterSimulator
 * Reads the Kaggle Household Electricity Usage CSV from assets/
 * and emits one row every 3 seconds via a Kotlin Flow,
 * simulating a continuous real-time sensor stream.
 *
 * Sensory inputs: energy_kwh, room_temp_c, occupancy_count, tariff_per_kwh_usd
 * Non-sensory inputs: is_weekend, holiday_flag, day_of_week, user budget
 */

// ── Data model for one CSV row ────────────────────────────────────────────────
data class CsvSensorRow(
    // Sensory inputs (simulated from CSV)
    val energyKwh: Double,
    val roomTempC: Double,
    val occupancyCount: Int,
    val tariffPerKwh: Double,
    val applianceName: String,

    // Non-sensory inputs
    val isWeekend: Boolean,
    val isHoliday: Boolean,
    val timeOfDay: String,
    val tariffTier: String
)

// ── SmartMeterSimulator ───────────────────────────────────────────────────────
class SmartMeterSimulator(private val context: Context) {

    companion object {
        private const val CSV_FILE = "household_electricity_usage.csv"
        private const val EMIT_INTERVAL_MS = 3000L
        private const val MAX_ROWS = 200
    }

    // ── CSV Parser ────────────────────────────────────────────────────────────
    fun loadCsv(): List<CsvSensorRow> {
        val rows = mutableListOf<CsvSensorRow>()
        try {
            context.assets.open(CSV_FILE).bufferedReader().use { reader ->
                val header = reader.readLine() ?: return emptyList()
                val columns = header.split(",").map { it.trim().lowercase() }

                val idxEnergy    = columns.indexOfFirst { it.contains("energy_kwh") }
                val idxTemp      = columns.indexOfFirst { it.contains("room_temp") }
                val idxOccupancy = columns.indexOfFirst { it.contains("occupancy") }
                val idxTariff    = columns.indexOfFirst { it.contains("tariff_per_kwh") }
                val idxAppliance = columns.indexOfFirst { it.contains("appliance") }
                val idxWeekend   = columns.indexOfFirst { it.contains("is_weekend") }
                val idxHoliday   = columns.indexOfFirst { it.contains("holiday_flag") }
                val idxTimeOfDay = columns.indexOfFirst { it.contains("day_of_week") }

                var lineCount = 0
                reader.forEachLine { line ->
                    if (lineCount >= MAX_ROWS) return@forEachLine
                    val cols = line.split(",").map { it.trim() }
                    if (cols.size < 4) return@forEachLine
                    try {
                        val energy    = cols.getOrNull(idxEnergy)?.toDoubleOrNull() ?: 0.0
                        val temp      = cols.getOrNull(idxTemp)?.toDoubleOrNull() ?: 22.0
                        val occupancy = cols.getOrNull(idxOccupancy)?.toIntOrNull() ?: 1
                        val tariff    = cols.getOrNull(idxTariff)?.toDoubleOrNull() ?: 0.18
                        val appliance = cols.getOrNull(idxAppliance) ?: "Unknown Appliance"
                        val weekend   = cols.getOrNull(idxWeekend)?.trim() == "1"
                        val holiday   = cols.getOrNull(idxHoliday)?.trim() == "1"
                        val timeOfDay = cols.getOrNull(idxTimeOfDay) ?: "Weekday"
                        val tariffTier = when {
                            tariff >= 0.22 -> "Peak"
                            tariff >= 0.15 -> "Shoulder"
                            else           -> "Off-Peak"
                        }
                        rows.add(
                            CsvSensorRow(
                                energyKwh      = energy,
                                roomTempC      = temp,
                                occupancyCount = occupancy,
                                tariffPerKwh   = tariff,
                                applianceName  = appliance,
                                isWeekend      = weekend,
                                isHoliday      = holiday,
                                timeOfDay      = timeOfDay,
                                tariffTier     = tariffTier
                            )
                        )
                        lineCount++
                    } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rows
    }
}