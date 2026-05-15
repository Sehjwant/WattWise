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
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    // Callback wired in MainActivity to navigate to MessagingScreen
    onNavigateToMessaging: () -> Unit = {}
) {
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

    val co2Today = viewModel.dailyCumulativeKwh * 0.79

    // Count unread alert messages for the badge
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
                // ── Chat icon — entry point to Firebase MessagingScreen ────────
                // Badge shows number of automated ContextEngine / WorkManager alerts
                // so users know there are energy notifications waiting in the chat.
                actions = {
                    IconButton(onClick = onNavigateToMessaging) {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(containerColor = Color(0xFFE53935)) {
                                        Text("$alertCount", color = Color.White, fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = "Household Chat",
                                tint = Color(0xFF69F0AE)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20)
                )
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Text(
                text = if (viewModel.isWeekend) "Weekend — expect higher usage patterns"
                else if (viewModel.isHoliday) "Public holiday — home usage likely elevated"
                else "Today is a regular weekday",
                fontSize = 13.sp,
                color = Color.Gray
            )

            // ContextEngine State Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = contextColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (viewModel.contextState) {
                            "Warning", "Critical" -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = "Context State",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Energy Status: ${viewModel.contextState}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            viewModel.contextTip,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Live Energy Gauge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Usage", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${viewModel.currentEnergyKwh} kWh",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text("Tariff: ${viewModel.currentTariffTier}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.currentTariff >= 0.20)
                            Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tariff Rate", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$${viewModel.currentTariff}/kWh",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.currentTariff >= 0.20)
                                Color(0xFFB71C1C) else Color(0xFF2E7D32)
                        )
                        Text(viewModel.currentTariffTier, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Daily Budget Progress — LinearProgressIndicator (required component)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Daily Budget",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF33691E)
                        )
                        Text(
                            "${viewModel.dailyCumulativeKwh} / ${viewModel.budgetGoal} kWh",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.budgetProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = progressColor,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${(viewModel.budgetProgress * 100).toInt()}% of daily budget used",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (!viewModel.isOwner) {
                        Text(
                            "Budget controls available to Household Owner only",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Occupancy, Temperature, CO2 row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Occupancy", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "${viewModel.occupancyCount} persons",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.roomTempC > 30)
                            Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Room Temp", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "${viewModel.roomTempC}°C",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (viewModel.roomTempC > 30)
                                Color(0xFFB71C1C) else Color(0xFF1565C0)
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CO₂ Today", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            String.format("%.2f kg", co2Today),
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }
            }

            // Context-aware tip card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF00897B), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Smart Tip",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C),
                            fontSize = 14.sp
                        )
                        Text(
                            text = when {
                                viewModel.currentTariff >= 0.20 ->
                                    "Peak tariff active — consider deferring the washing machine or dishwasher to off-peak hours."
                                viewModel.occupancyCount == 0 && viewModel.currentEnergyKwh > 0.1 ->
                                    "No one is home but energy is active — check for standby waste."
                                viewModel.roomTempC > 32 ->
                                    "Room temperature is high — optimise AC settings for efficiency."
                                viewModel.budgetProgress >= 0.8f ->
                                    "You've used 80% of your daily budget — switch to low-energy activities."
                                else -> viewModel.contextTip
                            },
                            color = Color(0xFF004D40),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Household ID + Chat shortcut card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Household ID", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            viewModel.householdId,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            fontSize = 14.sp
                        )
                    }
                    // Secondary entry point to messaging — shows member count
                    IconButton(onClick = onNavigateToMessaging) {
                        BadgedBox(badge = {
                            if (alertCount > 0) {
                                Badge(containerColor = Color(0xFFE53935)) {
                                    Text("$alertCount", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = "Open Household Chat",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
