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
}
