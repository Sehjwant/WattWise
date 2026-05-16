package com.fit5046.wattwise

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

class WattWiseViewModel(application: Application) : AndroidViewModel(application) {

    // ── Room Database ─────────────────────────────────────────────────────────
    private val dao = WattWiseDatabase.getDatabase(application).applianceDao()

    // ── Auth / User ───────────────────────────────────────────────────────────
    var isLoggedIn  by mutableStateOf(false)
    var isOwner     by mutableStateOf(true)
    var householdId by mutableStateOf("HH-20261001")

    fun logout() {
        isLoggedIn = false
        fullName = ""
        suburb = ""
    }

    // ── Profile / Settings ────────────────────────────────────────────────────
    var fullName             by mutableStateOf("")
    var suburb               by mutableStateOf("")
    var householdSize        by mutableStateOf("2")
    var budgetGoal           by mutableStateOf("20.0")
    var billingType          by mutableStateOf("Time-of-Use")
    var offPeakHours         by mutableStateOf("11:00 PM")
    var notificationsEnabled by mutableStateOf(true)
    var householdSharing     by mutableStateOf(false)

    // ── Household Members ─────────────────────────────────────────────────────
    val householdMembers = mutableStateListOf(
        HouseholdMember("Alex Johnson",  "alex@gmail.com",  isOwner = true),
        HouseholdMember("Sarah Chen",    "sarah@gmail.com"),
        HouseholdMember("Mike Williams", "mike@gmail.com")
    )

    fun removeMember(index: Int) {
        if (index > 0 && index < householdMembers.size) {
            householdMembers.removeAt(index)
            val current = householdSize.toIntOrNull() ?: 1
            if (current > 1) householdSize = (current - 1).toString()
        }
    }

    // ── Appliances (Room-backed) ──────────────────────────────────────────────
    val appliances = mutableStateListOf<Appliance>()

    init {
        viewModelScope.launch {
            dao.getAll().collect { list ->
                if (list.isEmpty()) {
                    dao.insert(Appliance(name = "Samsung Washing Machine",    category = "Laundry",  wattage = 500,  notes = "Runs during off-peak hours"))
                    dao.insert(Appliance(name = "Mitsubishi Air Conditioner", category = "Cooling",  wattage = 1800, notes = "Set to 24°C"))
                    dao.insert(Appliance(name = "LG Dishwasher",              category = "Kitchen",  wattage = 1200, notes = "Eco mode enabled"))
                    dao.insert(Appliance(name = "LED Downlights x10",         category = "Lighting", wattage = 100,  notes = "Living room"))
                    dao.insert(Appliance(name = "Electric Oven",              category = "Kitchen",  wattage = 2400, notes = ""))
                } else {
                    appliances.clear()
                    appliances.addAll(list)
                }
            }
        }
    }

    fun addAppliance(appliance: Appliance) {
        viewModelScope.launch { dao.insert(appliance) }
    }

    fun deleteAppliance(id: Int) {
        viewModelScope.launch { dao.deleteById(id) }
    }

    fun updateAppliance(updated: Appliance) {
        viewModelScope.launch { dao.update(updated) }
    }

    fun nextId(): Int = (appliances.maxOfOrNull { it.id } ?: 0) + 1

    // ── Sensor / ContextEngine ────────────────────────────────────────────────
    var currentEnergyKwh   by mutableStateOf(0.85)
    var currentTariff      by mutableStateOf(0.18)
    var currentTariffTier  by mutableStateOf("Off-Peak")
    var roomTempC          by mutableStateOf(24.5)
    var occupancyCount     by mutableStateOf(2)
    var dailyCumulativeKwh by mutableStateOf(12.4)
    var contextState       by mutableStateOf("Normal")
    var contextTip         by mutableStateOf("Your energy usage looks great today!")
    var isWeekend          by mutableStateOf(false)
    var isHoliday          by mutableStateOf(false)

    // ── Live Monitor Feed ─────────────────────────────────────────────────────
    val liveReadings = mutableStateListOf(
        SensorReading("Washing Machine", 0.50, "Off-Peak", 23.1, 2, 0.09),
        SensorReading("Air Conditioner", 1.80, "Peak",     31.5, 3, 0.36),
        SensorReading("Dishwasher",      1.20, "Shoulder", 22.8, 2, 0.18),
        SensorReading("LED Lights",      0.10, "Off-Peak", 21.0, 1, 0.02),
        SensorReading("Electric Oven",   2.40, "Peak",     24.0, 4, 0.48),
        SensorReading("Washing Machine", 0.50, "Off-Peak", 23.1, 2, 0.09),
        SensorReading("Air Conditioner", 1.80, "Peak",     31.5, 3, 0.36)
    )

    // ── Budget progress ───────────────────────────────────────────────────────
    val budgetProgress: Float
        get() {
            val budget = budgetGoal.toFloatOrNull() ?: 20f
            return (dailyCumulativeKwh / budget).toFloat().coerceIn(0f, 1f)
        }

    // ── Search ────────────────────────────────────────────────────────────────
    var searchQuery by mutableStateOf("")

    val filteredAppliances: List<Appliance>
        get() = if (searchQuery.isBlank()) appliances
        else appliances.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }

    // ── Weather (Retrofit / OpenWeatherMap) ───────────────────────────────────
    private val weatherRepository = WeatherRepository()

    var weather by mutableStateOf(WeatherUiState())
        private set

    fun fetchWeather() {
        viewModelScope.launch {
            weather = WeatherUiState(isLoading = true)
            val city = suburb.ifBlank { "Melbourne" } + ",AU"
            weather = weatherRepository.getWeather(city)
            updateContextFromWeather()
        }
    }

    private fun updateContextFromWeather() {
        val outdoorTemp = weather.outdoorTempC
        contextState = when {
            outdoorTemp > 35 && currentEnergyKwh > 1.5 -> "Critical"
            outdoorTemp > 28 && budgetProgress >= 0.8f  -> "Warning"
            outdoorTemp < 10 && currentEnergyKwh > 2.0  -> "Warning"
            budgetProgress >= 1.0f                       -> "Critical"
            budgetProgress >= 0.8f                       -> "Warning"
            else                                         -> "Normal"
        }
        contextTip = when (contextState) {
            "Critical" -> "Critical: ${weather.energyImpact}. Shift appliances to off-peak immediately."
            "Warning"  -> "Warning: ${weather.energyImpact}. Monitor usage closely."
            else       -> "${weather.description.replaceFirstChar { it.uppercase() }} in ${weather.city}. ${weather.energyImpact}."
        }
    }

    // ── Next Hour Forecast (TFLite placeholder) ───────────────────────────────
    var nextHourForecastKwh by mutableStateOf(0.0)
        private set

    fun updateForecast(predictedKwh: Double) {
        nextHourForecastKwh = predictedKwh
    }

    // ── Household Messaging ───────────────────────────────────────────────────
    val messages = mutableStateListOf(
        HouseholdMessage(
            id         = 1,
            senderName = "WattWise — ContextEngine",
            body       = "⚡ Peak tariff active (0.22 AUD/kWh). Consider deferring the " +
                    "washing machine and dishwasher until after 10 PM off-peak.",
            timestamp  = "8:02 AM",
            type       = MessageType.ALERT
        ),
        HouseholdMessage(
            id         = 2,
            senderName = "Sarah Chen",
            body       = "Got it — I'll run the dishwasher tonight. Is the oven still on?",
            timestamp  = "8:05 AM",
            type       = MessageType.RECEIVED
        ),
        HouseholdMessage(
            id         = 3,
            senderName = "Alex Johnson",
            body       = "No I turned it off. The AC is the big one right now — room temp is 31°C.",
            timestamp  = "8:07 AM",
            type       = MessageType.SENT
        ),
        HouseholdMessage(
            id         = 4,
            senderName = "WattWise — WorkManager",
            body       = "⚠️ Budget alert: you have used 80% of your daily energy budget " +
                    "(16.0 / 20.0 kWh). Shift remaining appliances to off-peak to avoid overage.",
            timestamp  = "1:14 PM",
            type       = MessageType.ALERT
        ),
        HouseholdMessage(
            id         = 5,
            senderName = "Mike Williams",
            body       = "I'm heading out — should be zero occupancy from 2–6 PM. " +
                    "Turning off the AC now.",
            timestamp  = "1:18 PM",
            type       = MessageType.RECEIVED
        ),
        HouseholdMessage(
            id         = 6,
            senderName = "Alex Johnson",
            body       = "Thanks Mike 👍 that'll help a lot with the budget.",
            timestamp  = "1:20 PM",
            type       = MessageType.SENT
        ),
        HouseholdMessage(
            id         = 7,
            senderName = "WattWise — ContextEngine",
            body       = "🌿 Standby waste detected: 0.3 kWh consumed with zero occupancy. " +
                    "Check for appliances left in standby mode.",
            timestamp  = "3:45 PM",
            type       = MessageType.ALERT
        ),
        HouseholdMessage(
            id         = 8,
            senderName = "Sarah Chen",
            body       = "Probably the TV on standby in the living room — I'll switch it off " +
                    "at the wall when I get home.",
            timestamp  = "3:52 PM",
            type       = MessageType.RECEIVED
        )
    )

    fun sendMessage(text: String) {
        val newId = (messages.maxOfOrNull { it.id } ?: 0) + 1
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val amPm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
        messages.add(
            HouseholdMessage(
                id         = newId,
                senderName = fullName.ifBlank { "Alex Johnson" },
                body       = text,
                timestamp  = String.format("%d:%02d %s", if (hour == 0) 12 else hour, minute, amPm),
                type       = MessageType.SENT
            )
        )
    }

    // ── SmartMeterSimulator + ContextEngine ───────────────────────────────────
    private var simulator: SmartMeterSimulator? = null
    var isSimulatorRunning by mutableStateOf(false)

    fun startSimulator(context: Context) {
        if (isSimulatorRunning) return
        isSimulatorRunning = true
        simulator = SmartMeterSimulator(context)

        viewModelScope.launch {
            simulator!!.stream().collect { row ->
                // Update sensory data from CSV row
                currentEnergyKwh  = row.energyKwh
                currentTariff     = row.tariffPerKwh
                currentTariffTier = row.tariffTier
                roomTempC         = row.roomTempC
                occupancyCount    = row.occupancyCount
                isWeekend         = row.isWeekend
                isHoliday         = row.isHoliday

                // Accumulate daily energy
                dailyCumulativeKwh += row.energyKwh * 0.01

                // Update live readings feed
                val newReading = SensorReading(
                    applianceName  = row.applianceName,
                    energyKwh      = row.energyKwh,
                    tariffTier     = row.tariffTier,
                    roomTempC      = row.roomTempC,
                    occupancy      = row.occupancyCount,
                    costPerSession = row.energyKwh * row.tariffPerKwh
                )
                if (liveReadings.size >= 20) liveReadings.removeAt(0)
                liveReadings.add(0, newReading)

                // Run ContextEngine to compute situation state
                val result = ContextEngine.compute(
                    row           = row,
                    budgetGoal    = budgetGoal.toDoubleOrNull() ?: 20.0,
                    cumulativeKwh = dailyCumulativeKwh
                )
                contextState = result.stateLabel
                contextTip   = result.tip

                // Add alert to messaging screen if triggered
                result.alertMessage?.let { alert ->
                    val newId = (messages.maxOfOrNull { it.id } ?: 0) + 1
                    messages.add(
                        HouseholdMessage(
                            id         = newId,
                            senderName = "WattWise — ContextEngine",
                            body       = alert,
                            timestamp  = java.time.LocalTime.now().let {
                                String.format(
                                    "%d:%02d %s",
                                    if (it.hour % 12 == 0) 12 else it.hour % 12,
                                    it.minute,
                                    if (it.hour < 12) "AM" else "PM"
                                )
                            },
                            type = MessageType.ALERT
                        )
                    )
                }
            }
        }
    }
}