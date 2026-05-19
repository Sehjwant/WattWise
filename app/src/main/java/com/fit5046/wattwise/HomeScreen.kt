package com.fit5046.wattwise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WattWiseViewModel,
    onNavigateToMessaging: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    val contextColor = when (viewModel.contextState) {
        "Warning"  -> Color(0xFFF57F17)
        "Critical" -> Color(0xFFB71C1C)
        else       -> Color(0xFF2E7D32)
    }

    val progressColor = when {
        viewModel.budgetProgress >= 1.0f -> Color(0xFFB71C1C)
        viewModel.budgetProgress >= 0.8f -> Color(0xFFF57F17)
        else                             -> Color(0xFF2E7D32)
    }

    val weatherIcon = when (viewModel.weather.condition.lowercase()) {
        "clear"        -> Icons.Default.WbSunny
        "clouds"       -> Icons.Default.Cloud
        "rain"         -> Icons.Default.Umbrella
        "drizzle"      -> Icons.Default.Grain
        "thunderstorm" -> Icons.Default.FlashOn
        "snow"         -> Icons.Default.AcUnit
        "mist", "fog"  -> Icons.Default.BlurOn
        else           -> Icons.Default.DeviceThermostat
    }

    val co2Today = viewModel.dailyCumulativeKwh * 0.79
    val alertCount = viewModel.messages.count { it.type == MessageType.ALERT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Home, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WattWise", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchWeather() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Weather",
                            tint = Color(0xFF69F0AE))
                    }
                    IconButton(onClick = onNavigateToMessaging) {
                        BadgedBox(badge = {
                            if (alertCount > 0) {
                                Badge(containerColor = Color(0xFFE53935)) {
                                    Text("$alertCount", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Forum, contentDescription = "Household Chat",
                                tint = Color(0xFF69F0AE))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Greeting
            Text(
                text = "Hello, ${viewModel.fullName.ifEmpty { "User" }} 👋",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)
            )
            Text(
                text = if (viewModel.isWeekend) "Weekend — expect higher usage patterns"
                else if (viewModel.isHoliday) "Public holiday — home usage likely elevated"
                else "Today is a regular weekday",
                fontSize = 13.sp, color = Color.Gray
            )

            // ── Weather Card (Retrofit / OpenWeatherMap) ──────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1))
            ) {
                when {
                    viewModel.weather.isLoading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Fetching weather for ${viewModel.suburb.ifBlank { "Melbourne" }}...",
                                color = Color.White, fontSize = 14.sp
                            )
                        }
                    }
                    viewModel.weather.error != null -> {
                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(viewModel.weather.error!!,
                                color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = weatherIcon,
                                    contentDescription = viewModel.weather.condition,
                                    tint = Color.White, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(viewModel.weather.city,
                                        color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                    Text(
                                        "${String.format("%.1f", viewModel.weather.outdoorTempC)}°C",
                                        color = Color.White, fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        viewModel.weather.description
                                            .replaceFirstChar { it.uppercase() },
                                        color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                WeatherDetailRow("Feels like",
                                    "${String.format("%.1f", viewModel.weather.feelsLikeC)}°C")
                                WeatherDetailRow("Humidity", "${viewModel.weather.humidity}%")
                                WeatherDetailRow("Wind",
                                    "${String.format("%.1f", viewModel.weather.windSpeedMs)} m/s")
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(viewModel.weather.energyImpact,
                                        color = Color.White, fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // ── ContextEngine State Card ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = contextColor)
            ) {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (viewModel.contextState) {
                            "Warning", "Critical" -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = "Context State",
                        tint = Color.White, modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Energy Status: ${viewModel.contextState}",
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text(viewModel.contextTip,
                            color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
            }

            // ── Live Energy Gauge Row ─────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Usage", fontSize = 12.sp, color = Color.Gray)
                        Text("${viewModel.currentEnergyKwh} kWh", fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(viewModel.currentTariffTier, fontSize = 12.sp,
                            color = when (viewModel.currentTariffTier) {
                                "Peak"     -> Color(0xFFB71C1C)
                                "Shoulder" -> Color(0xFFF57F17)
                                else       -> Color(0xFF2E7D32)
                            })
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Column(modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tariff Rate", fontSize = 12.sp, color = Color.Gray)
                        Text("$${viewModel.currentTariff}/kWh", fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        Text("AUD", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // ── Budget Progress Card ──────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Budget", fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF33691E))
                        Text("${viewModel.dailyCumulativeKwh} / ${viewModel.budgetGoal} kWh",
                            fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.budgetProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = progressColor, trackColor = Color(0xFFE0E0E0),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${(viewModel.budgetProgress * 100).toInt()}% of daily budget used",
                        fontSize = 12.sp, color = Color.Gray)
                    if (!viewModel.isOwner) {
                        Text("Budget controls available to Household Owner only",
                            fontSize = 11.sp, color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // ── Occupancy / Room Temp / CO2 row ──────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Occupancy", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "${viewModel.occupancyCount} ${if (viewModel.occupancyCount == 1) "person" else "people"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.roomTempC > 30)
                        Color(0xFFFFEBEE) else Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Room Temp", fontSize = 12.sp, color = Color.Gray)
                        Text("${viewModel.roomTempC}°C", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.roomTempC > 30)
                                Color(0xFFB71C1C) else Color(0xFF1565C0))
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                    Column(modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CO₂ Today", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("%.2f kg", co2Today), fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                    }
                }
            }

            // ── Smart Tip Card (weather + sensor context-aware) ───────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(36.dp)
                        .background(Color(0xFF00897B), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Info, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Smart Tip", fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C), fontSize = 14.sp)
                        Text(
                            text = when {
                                viewModel.weather.outdoorTempC > 35 ->
                                    "Heat wave detected (${String.format("%.1f", viewModel.weather.outdoorTempC)}°C outdoor) — pre-cool before peak tariff starts."
                                viewModel.weather.outdoorTempC < 10 ->
                                    "Cold outside (${String.format("%.1f", viewModel.weather.outdoorTempC)}°C) — pre-heat before off-peak ends at ${viewModel.offPeakHours}."
                                viewModel.currentTariff >= 0.20 ->
                                    "Peak tariff active — defer washing machine or dishwasher to off-peak hours."
                                viewModel.occupancyCount == 0 && viewModel.currentEnergyKwh > 0.1 ->
                                    "No one is home but energy is active — check for standby waste."
                                viewModel.roomTempC > 32 ->
                                    "Room temperature is high — optimise AC settings for efficiency."
                                viewModel.budgetProgress >= 0.8f ->
                                    "You've used ${(viewModel.budgetProgress * 100).toInt()}% of your daily budget — switch to low-energy activities."
                                else -> viewModel.contextTip
                            },
                            color = Color(0xFF004D40), fontSize = 13.sp
                        )
                    }
                }
            }

            // Next Hour Forecast Card — TFLite on-device prediction
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF283593), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔮", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Next Hour Forecast",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF283593),
                            fontSize = 14.sp
                        )
                        Text(
                            "Predicted: ${String.format("%.2f", viewModel.nextHourForecastKwh)} kWh",
                            fontSize = 13.sp,
                            color = Color(0xFF1A237E)
                        )
                        Text(
                            "Est. cost: $${String.format("%.4f", viewModel.nextHourForecastKwh * viewModel.currentTariff)}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ── Household ID  ──────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Row(modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Household ID", color = Color.Gray, fontSize = 12.sp)
                        Text(viewModel.householdId, fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20), fontSize = 14.sp)
                    }
//                    IconButton(onClick = onNavigateToMessaging) {
//                        BadgedBox(badge = {
//                            if (alertCount > 0) {
//                                Badge(containerColor = Color(0xFFE53935)) {
//                                    Text("$alertCount", color = Color.White, fontSize = 9.sp)
//                                }
//                            }
//                        }) {
//                            Icon(Icons.Default.Forum, contentDescription = "Open Household Chat",
//                                tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
//                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }


@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}