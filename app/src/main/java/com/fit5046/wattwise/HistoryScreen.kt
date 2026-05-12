package com.fit5046.wattwise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

private val weeklyData = listOf(
    "Mon" to 14.2f, "Tue" to 11.8f, "Wed" to 16.5f,
    "Thu" to 13.1f, "Fri" to 18.3f, "Sat" to 21.0f, "Sun" to 19.4f
)
private val applianceBreakdown = listOf(
    Triple("Air Con",  0.35f, Color(0xFF1565C0)),
    Triple("Oven",     0.22f, Color(0xFFF57F17)),
    Triple("Washer",   0.15f, Color(0xFF6A1B9A)),
    Triple("Lights",   0.12f, Color(0xFFEF6C00)),
    Triple("Other",    0.16f, Color(0xFF455A64))
)
private val monthlyTrend = listOf(
    "W1" to 98.4f, "W2" to 87.2f, "W3" to 104.6f, "W4" to 94.3f
)

private enum class HistoryTab(val label: String, val emoji: String) {
    DAILY("Daily Usage", "⚡"),
    BREAKDOWN("Breakdown", "🥧"),
    CARBON("Carbon", "🌿"),
    TRENDS("Trends", "📈")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: WattWiseViewModel) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(HistoryTab.DAILY) }

    val fromPickerState = rememberDatePickerState(Instant.now().toEpochMilli())
    val toPickerState = rememberDatePickerState(Instant.now().toEpochMilli())

    // From DatePicker dialog
    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showFromPicker = false
                    fromPickerState.selectedDateMillis?.let {
                        fromDate = formatter.format(Date(it))
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

    // To DatePicker dialog
    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showToPicker = false
                    toPickerState.selectedDateMillis?.let {
                        toDate = formatter.format(Date(it))
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
                        Icon(
                            Icons.Default.DateRange, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "History & Charts",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
        ) {
            // Date Range Filter Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // From date field
                    OutlinedTextField(
                        value = fromDate.ifEmpty { "From" },
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                "From", fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showFromPicker = true },
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange, null,
                                modifier = Modifier
                                    .clickable { showFromPicker = true }
                                    .size(18.dp),
                                tint = Color(0xFF69F0AE)
                            )
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
                    // To date field
                    OutlinedTextField(
                        value = toDate.ifEmpty { "To" },
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                "To", fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showToPicker = true },
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange, null,
                                modifier = Modifier
                                    .clickable { showToPicker = true }
                                    .size(18.dp),
                                tint = Color(0xFF69F0AE)
                            )
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
                    // Apply button
                    Button(
                        onClick = { /* filter Room data in A4 */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF69F0AE)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            "Apply",
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        }
    }
}