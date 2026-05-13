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
        Appliance(1, "Samsung Washing Machine",  "Laundry",  500,
cat > app/src/main/java/com/fit5046/wattwise/WattWiseViewModel.kt << 'EOF'
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
        Appliance(2, "Mitsubishi Air Conditioner","Cooling", 1800, "Set to 24°C"),
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
}
