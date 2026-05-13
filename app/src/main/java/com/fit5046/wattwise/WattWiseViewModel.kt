package com.fit5046.wattwise

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Appliance(
    val id: Int,
    val name: String,
    val category: String,
    val wattage: Int,
    val notes: String = ""
)

data class SensorReading(
    val applianceName: String,
    val energyKwh: Double,
    val tariffTier: String,
    val roomTempC: Double,
    val occupancy: Int,
    val costPerSession: Double
)

data class HouseholdMember(
    val name: String,
    val email: String,
    val isOwner: Boolean = false
)

class WattWiseViewModel : ViewModel() {

    // Auth / User
    var isLoggedIn  by mutableStateOf(false)
    var isOwner     by mutableStateOf(true)
    var householdId by mutableStateOf("HH-20261001")

    // Profile / Settings
    var fullName           by mutableStateOf("")
    var suburb             by mutableStateOf("")
    var householdSize      by mutableStateOf("2")
    var budgetGoal         by mutableStateOf("20.0")
    var billingType        by mutableStateOf("Time-of-Use")
    var offPeakHours       by mutableStateOf("11:00 PM")
    var notificationsEnabled by mutableStateOf(true)
    var householdSharing   by mutableStateOf(false)

    // Household Members
    val householdMembers = mutableStateListOf(
        HouseholdMember("Alex Johnson", "alex@gmail.com", isOwner = true),
        HouseholdMember("Sarah Chen",   "sarah@gmail.com"),
        HouseholdMember("Mike Williams","mike@gmail.com")
    )

    fun removeMember(index: Int) {
        if (index > 0 && index < householdMembers.size) {
            householdMembers.removeAt(index)
            val current = householdSize.toIntOrNull() ?: 1
            if (current > 1) householdSize = (current - 1).toString()
        }
    }

    // Appliances
    val appliances = mutableStateListOf(
        Appliance(1, "Samsung Washing Machine",  "Laundry",  500,  "Runs during off-peak hours"),
        Appliance(2, "Mitsubishi Air Conditioner","Cooling", 1800, "Set to 24 C"),
        Appliance(3, "LG Dishwasher",            "Kitchen", 1200, "Eco mode enabled"),
        Appliance(4, "LED Downlights x10",       "Lighting",  100, "Living room"),
        Appliance(5, "Electric Oven",            "Kitchen", 2400, "")
    )

    fun addAppliance(appliance: Appliance)   { appliances.add(appliance) }
    fun deleteAppliance(id: Int)             { appliances.removeIf { it.id == id } }
    fun updateAppliance(updated: Appliance) {
        val i = appliances.indexOfFirst { it.id == updated.id }
        if (i != -1) appliances[i] = updated
    }
    fun nextId(): Int = (appliances.maxOfOrNull { it.id } ?: 0) + 1

    // Sensor / ContextEngine
    var currentEnergyKwh    by mutableStateOf(0.85)
    var currentTariff       by mutableStateOf(0.18)
    var currentTariffTier   by mutableStateOf("Off-Peak")
    var roomTempC           by mutableStateOf(24.5)
    var occupancyCount      by mutableStateOf(2)
    var dailyCumulativeKwh  by mutableStateOf(12.4)
    var contextState        by mutableStateOf("Normal")
    var contextTip          by mutableStateOf("Your energy usage looks great today!")
    var isWeekend           by mutableStateOf(false)
    var isHoliday           by mutableStateOf(false)

    // Live Monitor Feed
    val liveReadings = mutableStateListOf(
        SensorReading("Washing Machine",   0.50, "Off-Peak", 23.1, 2, 0.09),
        SensorReading("Air Conditioner",   1.80, "Peak",     31.5, 3, 0.36),
        SensorReading("Dishwasher",        1.20, "Shoulder", 22.8, 2, 0.18),
        SensorReading("LED Lights",        0.10, "Off-Peak", 21.0, 1, 0.02),
        SensorReading("Electric Oven",     2.40, "Peak",     24.0, 4, 0.48),
        SensorReading("Washing Machine",   0.50, "Off-Peak", 23.1, 2, 0.09),
        SensorReading("Air Conditioner",   1.80, "Peak",     31.5, 3, 0.36)
    )

    // Budget progress
    val budgetProgress: Float
        get() {
            val budget = budgetGoal.toFloatOrNull() ?: 20f
            return (dailyCumulativeKwh / budget).toFloat().coerceIn(0f, 1f)
        }

    // Search
    var searchQuery by mutableStateOf("")

    val filteredAppliances: List<Appliance>
        get() = if (searchQuery.isBlank()) appliances
        else appliances.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
}
