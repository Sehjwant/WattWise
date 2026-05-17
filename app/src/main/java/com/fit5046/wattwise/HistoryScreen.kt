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