package com.fit5046.wattwise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

// ── Category colors matching ApplianceManagerScreen ───────────────────────────
private fun categoryColor(category: String): Color = when (category) {
    "Cooling"  -> Color(0xFF1565C0)
    "Heating"  -> Color(0xFFB71C1C)
    "Laundry"  -> Color(0xFF6A1B9A)
    "Kitchen"  -> Color(0xFFE65100)
    "Lighting" -> Color(0xFFF9A825)
    else       -> Color(0xFF455A64)
}

// ── Tab definitions ───────────────────────────────────────────────────────────
private enum class HistoryTab(val label: String) {
    DAILY("Daily Usage"),
    BREAKDOWN("Breakdown"),
    CARBON("Carbon"),
    TRENDS("Trends")
}

// ── Reusable data class ───────────────────────────────────────────────────────
private data class StatItem(val label: String, val value: String, val color: Color)

// ── Reusable composables ──────────────────────────────────────────────────────
@Composable
private fun SummaryStatsRow(vararg stats: StatItem) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { stat ->
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stat.label, fontSize = 10.sp, color = Color.Gray,
                        fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stat.value, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = stat.color)
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 15.sp)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, body: String, tint: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.07f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = tint,
                modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = tint, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

// ── Tab 1 — Daily Usage (Bar Chart) connected to Room ─────────────────────────
@Composable
private fun DailyUsageTab(viewModel: WattWiseViewModel) {
    val dailyTotals = viewModel.dailyTotals
    val totalKwh    = dailyTotals.sumOf { it.totalKwh }
    val avgKwh      = if (dailyTotals.isNotEmpty()) totalKwh / dailyTotals.size else 0.0
    val peakDay     = dailyTotals.maxByOrNull { it.totalKwh }

    SummaryStatsRow(
        StatItem("Period Total", String.format("%.1f kWh", totalKwh), Color(0xFF1B5E20)),
        StatItem("Daily Avg", String.format("%.1f kWh", avgKwh), Color(0xFF388E3C)),
        StatItem("Peak Day", peakDay?.let {
            "${it.date.takeLast(5)} ${String.format("%.1f", it.totalKwh)}"
        } ?: "N/A", Color(0xFFB71C1C))
    )

    ChartCard(
        title = "Daily Usage (kWh)",
        subtitle = "Data from Room database — ${dailyTotals.size} days shown"
    ) {
        if (dailyTotals.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center) {
                Text("No data for selected range", color = Color.Gray)
            }
        } else {
            val maxVal = dailyTotals.maxOf { it.totalKwh }.toFloat()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyTotals.forEach { daily ->
                    val value  = daily.totalKwh.toFloat()
                    val label  = daily.date.takeLast(5)
                    val isPeak = daily == peakDay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(String.format("%.0f", value), fontSize = 9.sp,
                            color = if (isPeak) Color(0xFFB71C1C) else Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(((value / maxVal) * 110).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(when {
                                    isPeak      -> Color(0xFFB71C1C)
                                    value > 15f -> Color(0xFFF57F17)
                                    else        -> Color(0xFF2E7D32)
                                })
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 8.sp,
                            color = if (isPeak) Color(0xFFB71C1C) else Color(0xFF424242))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(Color(0xFF2E7D32), "Normal")
                LegendItem(Color(0xFFF57F17), "> 15 kWh")
                LegendItem(Color(0xFFB71C1C), "Peak day")
            }
        }
    }

    InsightCard(
        icon = Icons.Default.Info,
        title = "Daily Usage Insight",
        body = if (avgKwh > 15)
            "Your average daily usage is ${String.format("%.1f", avgKwh)} kWh — above the typical household average. Consider shifting appliances to off-peak hours."
        else
            "Your average daily usage is ${String.format("%.1f", avgKwh)} kWh — within a healthy range. Keep monitoring to stay on budget.",
        tint = Color(0xFF1B5E20)
    )
}

// ── Tab 2 — Category Breakdown connected to Room ──────────────────────────────
@Composable
private fun BreakdownTab(viewModel: WattWiseViewModel) {
    val breakdown      = viewModel.categoryBreakdown
    val totalKwh       = breakdown.sumOf { it.totalKwh }
    val topCategory    = breakdown.maxByOrNull { it.totalKwh }
    val lowestCategory = breakdown.minByOrNull { it.totalKwh }

    SummaryStatsRow(
        StatItem("Top User", topCategory?.let {
            "${it.category} ${String.format("%.0f", if (totalKwh > 0) it.totalKwh / totalKwh * 100 else 0.0)}%"
        } ?: "N/A", Color(0xFF1565C0)),
        StatItem("Most Efficient", lowestCategory?.category ?: "N/A", Color(0xFF2E7D32)),
        StatItem("Categories", "${breakdown.size}", Color(0xFF455A64))
    )

    ChartCard(
        title = "Usage by Category",
        subtitle = "Based on appliance categories from Room database"
    ) {
        if (breakdown.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center) {
                Text("No data for selected range", color = Color.Gray)
            }
        } else {
            breakdown.sortedByDescending { it.totalKwh }.forEach { cat ->
                val fraction = if (totalKwh > 0) (cat.totalKwh / totalKwh).toFloat() else 0f
                val color    = categoryColor(cat.category)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp)
                        .background(color, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cat.category, modifier = Modifier.width(64.dp),
                        fontSize = 13.sp, color = Color(0xFF212121))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.weight(1f).height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFFEEEEEE))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(fraction).height(18.dp)
                                .clip(RoundedCornerShape(9.dp)).background(color)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${(fraction * 100).toInt()}%",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = color, modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }

    InsightCard(
        icon = Icons.Default.Lightbulb,
        title = "Saving Opportunity",
        body = topCategory?.let {
            val pct = if (totalKwh > 0) (it.totalKwh / totalKwh * 100).toInt() else 0
            "${it.category} accounts for $pct% of household usage. " +
                    "Optimising ${it.category.lowercase()} appliances could significantly reduce your bill."
        } ?: "Add appliances to see category breakdown.",
        tint = Color(0xFF1565C0)
    )
}


// ── Tab 3 — Carbon Emissions connected to Room ────────────────────────────────
@Composable
private fun CarbonTab(viewModel: WattWiseViewModel) {
    val dailyTotals = viewModel.dailyTotals
    val co2Data     = dailyTotals.map { it.date.takeLast(5) to (it.totalKwh * 0.79).toFloat() }
    val totalCo2    = co2Data.sumOf { it.second.toDouble() }
    val avgCo2      = if (co2Data.isNotEmpty()) totalCo2 / co2Data.size else 0.0

    SummaryStatsRow(
        StatItem("Period CO2", String.format("%.1f kg", totalCo2), Color(0xFF2E7D32)),
        StatItem("Daily Avg", String.format("%.1f kg", avgCo2), Color(0xFF388E3C)),
        StatItem("Grid Factor", "0.79 kg/kWh", Color(0xFF455A64))
    )

    ChartCard(
        title = "Daily CO2 Emissions (kg)",
        subtitle = "Australia grid emission factor: 0.79 kg CO2/kWh"
    ) {
        if (co2Data.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center) {
                Text("No data for selected range", color = Color.Gray)
            }
        } else {
            val maxCo2 = co2Data.maxOf { it.second }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                co2Data.forEach { (day, co2) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(String.format("%.1f", co2), fontSize = 9.sp,
                            color = Color(0xFF6A1B9A))
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(((co2 / maxCo2) * 110).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(when {
                                    co2 > 15f -> Color(0xFF6A1B9A)
                                    co2 > 12f -> Color(0xFFAB47BC)
                                    else      -> Color(0xFFCE93D8)
                                })
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(day, fontSize = 8.sp, color = Color(0xFF424242))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(Color(0xFFCE93D8), "< 12 kg")
                LegendItem(Color(0xFFAB47BC), "12-15 kg")
                LegendItem(Color(0xFF6A1B9A), "> 15 kg")
            }
        }
    }

    InsightCard(
        icon = Icons.Default.Eco,
        title = "Your Carbon Footprint",
        body = "This period your household emitted ${String.format("%.1f", totalCo2)} kg of CO2 " +
                "equivalent to driving a petrol car roughly ${String.format("%.0f", totalCo2 * 4.2)} km. " +
                "Shifting peak-hour appliance use to off-peak can reduce this by 10-15%.",
        tint = Color(0xFF2E7D32)
    )
}

// ── Tab 4 — Trends connected to Room ─────────────────────────────────────────
@Composable
private fun TrendsTab(viewModel: WattWiseViewModel) {
    val dailyTotals = viewModel.dailyTotals

    val weeklyTotals = dailyTotals
        .chunked(7)
        .mapIndexed { index, week ->
            "W${index + 1}" to week.sumOf { it.totalKwh }.toFloat()
        }

    val monthTotal = weeklyTotals.sumOf { it.second.toDouble() }
    val bestWeek   = weeklyTotals.minByOrNull { it.second }
    val worstWeek  = weeklyTotals.maxByOrNull { it.second }

    SummaryStatsRow(
        StatItem("Period Total", String.format("%.0f kWh", monthTotal), Color(0xFF0D47A1)),
        StatItem("Best Week", bestWeek?.let {
            "${it.first} ${String.format("%.0f", it.second)}"
        } ?: "N/A", Color(0xFF2E7D32)),
        StatItem("Worst Week", worstWeek?.let {
            "${it.first} ${String.format("%.0f", it.second)}"
        } ?: "N/A", Color(0xFFB71C1C))
    )

    ChartCard(
        title = "Weekly Usage Trend (kWh)",
        subtitle = "Aggregated from daily Room data"
    ) {
        if (weeklyTotals.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center) {
                Text("No data for selected range", color = Color.Gray)
            }
        } else {
            val maxVal = weeklyTotals.maxOf { it.second }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyTotals.forEach { (week, value) ->
                    val isBest  = week == bestWeek?.first
                    val isWorst = week == worstWeek?.first
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            String.format("%.0f", value),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = when {
                                isWorst -> Color(0xFFB71C1C)
                                isBest  -> Color(0xFF2E7D32)
                                else    -> Color.Gray
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(((value / maxVal) * 100).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(when {
                                    isWorst -> Color(0xFFEF9A9A)
                                    isBest  -> Color(0xFFA5D6A7)
                                    else    -> Color(0xFF64B5F6)
                                })
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(week, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242))
                        if (isBest)  Text("best", fontSize = 9.sp, color = Color(0xFF2E7D32))
                        if (isWorst) Text("high", fontSize = 9.sp, color = Color(0xFFB71C1C))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Period total:", fontSize = 13.sp, color = Color.Gray)
                Text(
                    String.format("%.0f kWh  AUD %.0f", monthTotal, monthTotal * 0.18),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0D47A1)
                )
            }
        }
    }

    InsightCard(
        icon = Icons.Default.TrendingUp,
        title = "Trend Analysis",
        body = worstWeek?.let {
            "Your usage peaked in ${it.first} (${String.format("%.0f", it.second)} kWh). " +
                    "Setting a WorkManager alert at 80% of your weekly budget can help prevent overage."
        } ?: "Select a date range to see trend analysis.",
        tint = Color(0xFF0D47A1)
    )
}

// ── Main HistoryScreen ────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: WattWiseViewModel) {
    val formatter   = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var fromDate    by remember { mutableStateOf("") }
    var toDate      by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }
    var selectedTab    by remember { mutableStateOf(HistoryTab.DAILY) }

    val fromPickerState = rememberDatePickerState(Instant.now().toEpochMilli())
    val toPickerState   = rememberDatePickerState(Instant.now().toEpochMilli())

    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showFromPicker = false
                    fromPickerState.selectedDateMillis?.let {
                        fromDate = formatter.format(Date(it))
                        viewModel.selectedFromDate = dbFormatter.format(Date(it))
                    }
                }) { Text("OK", color = Color(0xFF2E7D32)) }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) { DatePicker(state = fromPickerState) }
    }

    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showToPicker = false
                    toPickerState.selectedDateMillis?.let {
                        toDate = formatter.format(Date(it))
                        viewModel.selectedToDate = dbFormatter.format(Date(it))
                    }
                }) { Text("OK", color = Color(0xFF2E7D32)) }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) { DatePicker(state = toPickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("History & Charts", fontWeight = FontWeight.Bold, color = Color.White)
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
        ) {
            // ── Date Range Filter ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromDate.ifEmpty { "From" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From", fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f).clickable { showFromPicker = true },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, null,
                                modifier = Modifier.clickable { showFromPicker = true }.size(18.dp),
                                tint = Color(0xFF69F0AE))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedBorderColor = Color(0xFF69F0AE),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFF69F0AE)
                        )
                    )
                    OutlinedTextField(
                        value = toDate.ifEmpty { "To" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To", fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f).clickable { showToPicker = true },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, null,
                                modifier = Modifier.clickable { showToPicker = true }.size(18.dp),
                                tint = Color(0xFF69F0AE))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedBorderColor = Color(0xFF69F0AE),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFF69F0AE)
                        )
                    )
                    Button(
                        onClick = {
                            if (viewModel.selectedFromDate.isNotEmpty() &&
                                viewModel.selectedToDate.isNotEmpty()) {
                                viewModel.loadHistoryForDateRange(
                                    viewModel.selectedFromDate,
                                    viewModel.selectedToDate
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF69F0AE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Apply", color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // ── Tab Row ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Color(0xFF69F0AE)
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF1B5E20) else Color.White
                        )
                    }
                }
            }

            // ── Animated tab content ──────────────────────────────────────────
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "history_tab_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7F5))
            ) { tab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (tab) {
                        HistoryTab.DAILY     -> DailyUsageTab(viewModel)
                        HistoryTab.BREAKDOWN -> BreakdownTab(viewModel)
                        HistoryTab.CARBON    -> CarbonTab(viewModel)
                        HistoryTab.TRENDS    -> TrendsTab(viewModel)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

