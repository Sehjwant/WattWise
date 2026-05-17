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
private enum class HistoryTab(val label: String, val emoji: String) {
    DAILY("Daily Usage", "⚡"),
    BREAKDOWN("Breakdown", "📊"),
    CARBON("Carbon", "🌿"),
    TRENDS("Trends", "📈")
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
private fun InsightCard(icon: String, title: String, body: String, tint: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.07f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Text(icon, fontSize = 24.sp)
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

