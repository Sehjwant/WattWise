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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.util.Log

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

    private val dao = WattWiseDatabase.getDatabase(application).applianceDao()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ── Auth / User ───────────────────────────────────────────────────────────
    var isLoggedIn    by mutableStateOf(false)
    var isOwner       by mutableStateOf(true)
    var householdId   by mutableStateOf("HH-20261001")
    var authError     by mutableStateOf<String?>(null)
    var isAuthLoading by mutableStateOf(false)

    fun logout() {
        auth.signOut()
        isLoggedIn = false
        fullName   = ""
        suburb     = ""
        authError  = null
        messages.clear()
    }

    // ── Email/Password Sign In ────────────────────────────────────────────────
    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        isAuthLoading = true
        authError = null
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                if (user != null) {
                    fullName = user.displayName ?: user.email?.substringBefore("@") ?: ""
                    loadUserProfile(user.uid) // listenToMessages called inside after householdId loads
                    isLoggedIn = true
                    onSuccess()
                }
            } catch (e: Exception) {
                authError = when {
                    e.message?.contains("no user record") == true -> "No account found with this email"
                    e.message?.contains("password is invalid") == true -> "Incorrect password"
                    e.message?.contains("badly formatted") == true -> "Please enter a valid email address"
                    e.message?.contains("network") == true -> "Network error. Please check your connection"
                    else -> e.message ?: "Sign in failed"
                }
            } finally {
                isAuthLoading = false
            }
        }
    }

    // ── Email/Password Registration ───────────────────────────────────────────
    fun registerWithEmail(
        name: String, email: String, password: String,
        role: String, householdIdInput: String, onSuccess: () -> Unit
    ) {
        isAuthLoading = true
        authError = null
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                if (user != null) {
                    fullName    = name
                    isOwner     = role == "Owner"
                    householdId = if (isOwner) "HH-${System.currentTimeMillis() % 100000}"
                    else householdIdInput.ifBlank { "HH-00000" }
                    saveUserProfile(user.uid, name, email, role, householdId)
                    isLoggedIn = true
                    listenToMessages() // householdId already set above
                    onSuccess()
                }
            } catch (e: Exception) {
                authError = when {
                    e.message?.contains("email address is already in use") == true -> "An account with this email already exists"
                    e.message?.contains("weak password") == true -> "Password is too weak"
                    e.message?.contains("badly formatted") == true -> "Please enter a valid email address"
                    else -> e.message ?: "Registration failed"
                }
            } finally {
                isAuthLoading = false
            }
        }
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────
    fun firebaseAuthWithGoogle(idToken: String, onSuccess: () -> Unit) {
        isAuthLoading = true
        authError = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                val user = auth.currentUser
                if (user != null) {
                    fullName = user.displayName ?: "Google User"
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    if (!doc.exists()) {
                        isOwner     = true
                        householdId = "HH-${System.currentTimeMillis() % 100000}"
                        saveUserProfile(user.uid, fullName, user.email ?: "", "Owner", householdId)
                        listenToMessages() // new user, householdId set directly above
                    } else {
                        loadUserProfile(user.uid) // listenToMessages called inside after householdId loads
                    }
                    isLoggedIn = true
                    onSuccess()
                }
            } catch (e: Exception) {
                authError = e.message ?: "Google sign-in failed"
            } finally {
                isAuthLoading = false
            }
        }
    }

    // ── Firestore: Save User Profile ──────────────────────────────────────────
    private suspend fun saveUserProfile(uid: String, name: String, email: String, role: String, hhId: String) {
        try {
            firestore.collection("users").document(uid).set(hashMapOf(
                "fullName"    to name,
                "email"       to email,
                "role"        to role,
                "householdId" to hhId,
                "createdAt"   to com.google.firebase.Timestamp.now()
            )).await()
        } catch (e: Exception) {
            Log.e("WattWiseAuth", "Failed to save user profile", e)
        }
    }

    // ── Firestore: Load User Profile ──────────────────────────────────────────
    // listenToMessages() is called here after householdId is confirmed loaded
    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    fullName    = doc.getString("fullName") ?: fullName
                    isOwner     = doc.getString("role") == "Owner"
                    householdId = doc.getString("householdId") ?: householdId
                    listenToMessages() // called after householdId is set from Firestore
                }
            } catch (e: Exception) {
                Log.e("WattWiseAuth", "Failed to load user profile", e)
            }
        }
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

    fun addAppliance(appliance: Appliance) { viewModelScope.launch { dao.insert(appliance) } }
    fun deleteAppliance(id: Int)           { viewModelScope.launch { dao.deleteById(id) } }
    fun updateAppliance(updated: Appliance){ viewModelScope.launch { dao.update(updated) } }
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

    // ── Next Hour Forecast ────────────────────────────────────────────────────
    var nextHourForecastKwh by mutableStateOf(0.0)
        private set
    fun updateForecast(predictedKwh: Double) { nextHourForecastKwh = predictedKwh }

    // ── Household Messaging (Firestore-backed) ────────────────────────────────
    // Stored in Firestore: /households/{householdId}/messages/{messageId}
    // Real-time snapshot listener keeps all household devices in sync.
    val messages = mutableStateListOf<HouseholdMessage>()

    fun listenToMessages() {
        firestore.collection("households")
            .document(householdId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WattWiseMessaging", "Listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages.clear()
                    var idCounter = 1
                    for (doc in snapshot.documents) {
                        val senderName   = doc.getString("senderName") ?: ""
                        val body         = doc.getString("body") ?: ""
                        val timestamp    = doc.getString("timestamp") ?: ""
                        val typeStr      = doc.getString("type") ?: "SENT"
                        val resolvedType = when {
                            typeStr == "ALERT"     -> MessageType.ALERT
                            senderName == fullName -> MessageType.SENT
                            else                   -> MessageType.RECEIVED
                        }
                        messages.add(
                            HouseholdMessage(
                                id         = idCounter++,
                                senderName = senderName,
                                body       = body,
                                timestamp  = timestamp,
                                type       = resolvedType
                            )
                        )
                    }
                }
            }
    }

    fun sendMessage(text: String) {
        val cal       = java.util.Calendar.getInstance()
        val hour      = cal.get(java.util.Calendar.HOUR)
        val minute    = cal.get(java.util.Calendar.MINUTE)
        val amPm      = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
        val timestamp = String.format("%d:%02d %s", if (hour == 0) 12 else hour, minute, amPm)

        firestore.collection("households")
            .document(householdId)
            .collection("messages")
            .add(hashMapOf(
                "senderName" to fullName.ifBlank { "Unknown" },
                "body"       to text,
                "timestamp"  to timestamp,
                "type"       to "SENT",
                "createdAt"  to com.google.firebase.Timestamp.now()
            ))
            .addOnFailureListener { e ->
                Log.e("WattWiseMessaging", "Failed to send message", e)
            }
    }

    fun sendAlertMessage(alertBody: String) {
        val cal       = java.util.Calendar.getInstance()
        val hour      = cal.get(java.util.Calendar.HOUR)
        val minute    = cal.get(java.util.Calendar.MINUTE)
        val amPm      = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
        val timestamp = String.format("%d:%02d %s", if (hour == 0) 12 else hour, minute, amPm)

        firestore.collection("households")
            .document(householdId)
            .collection("messages")
            .add(hashMapOf(
                "senderName" to "WattWise — ContextEngine",
                "body"       to alertBody,
                "timestamp"  to timestamp,
                "type"       to "ALERT",
                "createdAt"  to com.google.firebase.Timestamp.now()
            ))
            .addOnFailureListener { e ->
                Log.e("WattWiseMessaging", "Failed to send alert", e)
            }
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
                currentEnergyKwh  = row.energyKwh
                currentTariff     = row.tariffPerKwh
                currentTariffTier = row.tariffTier
                roomTempC         = row.roomTempC
                occupancyCount    = row.occupancyCount
                isWeekend         = row.isWeekend
                isHoliday         = row.isHoliday
                dailyCumulativeKwh += row.energyKwh * 0.01

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

                val result = ContextEngine.compute(
                    row           = row,
                    budgetGoal    = budgetGoal.toDoubleOrNull() ?: 20.0,
                    cumulativeKwh = dailyCumulativeKwh
                )
                contextState = result.stateLabel
                contextTip   = result.tip

                result.alertMessage?.let { alert -> sendAlertMessage(alert) }
            }
        }
    }
}