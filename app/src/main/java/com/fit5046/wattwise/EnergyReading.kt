package com.fit5046.wattwise

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "energy_readings")
data class EnergyReading(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,        // "2026-04-17"
    val hour: Int,           // 0-23
    val category: String,    // "Cooling", "Kitchen" etc
    val totalKwh: Double,
    val tariffTier: String,
    val avgTempC: Double,
    val avgOccupancy: Int,
    val totalCost: Double
)