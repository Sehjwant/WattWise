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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMonitorScreen(viewModel: WattWiseViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Monitor", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Status bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SmartMeter", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF69F0AE), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Streaming", color = Color(0xFF69F0AE),
                                fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Interval", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("3 sec / row", color = Color.White,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Status", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(viewModel.contextState, color = when (viewModel.contextState) {
                            "Warning" -> Color(0xFFFFD54F)
                            "Critical" -> Color(0xFFEF9A9A)
                            else -> Color(0xFF69F0AE)
                        }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Appliance", modifier = Modifier.weight(2f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text("kWh", modifier = Modifier.weight(1f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text("Tariff", modifier = Modifier.weight(1.2f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text("Temp", modifier = Modifier.weight(1f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text("Occ", modifier = Modifier.weight(0.7f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(4.dp))

            // LazyColumn — real-time SmartMeterSimulator feed (skeleton data)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(viewModel.liveReadings) { reading ->
                    SensorReadingCard(reading = reading)
                }
            }
        }
    }
}

@Composable
fun SensorReadingCard(reading: SensorReading) {
    val tariffColor = when (reading.tariffTier) {
        "Peak" -> Color(0xFFB71C1C)
        "Shoulder" -> Color(0xFFF57F17)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Appliance name
                Text(
                    text = reading.applianceName,
                    modifier = Modifier.weight(2f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                // Energy kWh
                Text(
                    text = String.format("%.2f", reading.energyKwh),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = Color(0xFF1B5E20)
                )
                // Tariff tier badge
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(
                            tariffColor.copy(alpha = 0.12f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reading.tariffTier,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tariffColor
                    )
                }
                // Room temperature
                Text(
                    text = "${reading.roomTempC}°",
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = if (reading.roomTempC > 30) Color(0xFFB71C1C) else Color(0xFF424242)
                )
                // Occupancy
                Text(
                    text = "${reading.occupancy}",
                    modifier = Modifier.weight(0.7f),
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }
            // Cost per session
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Cost this session:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = String.format("$%.4f", reading.costPerSession),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B5E20)
                )
            }
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
        }
    }
}
