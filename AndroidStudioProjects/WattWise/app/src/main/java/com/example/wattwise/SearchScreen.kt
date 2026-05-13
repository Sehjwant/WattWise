package com.example.wattwise

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: WattWiseViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search", fontWeight = FontWeight.Bold, color = Color.White)
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
            // Search TextField
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Search appliances or categories") },
                placeholder = { Text("e.g. Air Conditioner or Cooling") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search",
                        tint = Color(0xFF2E7D32))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (viewModel.searchQuery.isEmpty())
                        "All appliances (${viewModel.appliances.size})"
                    else "${viewModel.filteredAppliances.size} result(s) for \"${viewModel.searchQuery}\"",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn — search results
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(viewModel.filteredAppliances) { index, appliance ->
                    SearchResultCard(appliance = appliance)
                    if (index < viewModel.filteredAppliances.size - 1) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(appliance: Appliance) {
    val categoryColor = when (appliance.category) {
        "Cooling" -> Color(0xFF1565C0)
        "Heating" -> Color(0xFFB71C1C)
        "Laundry" -> Color(0xFF6A1B9A)
        "Kitchen" -> Color(0xFFF57F17)
        "Lighting" -> Color(0xFFEF6C00)
        else -> Color(0xFF455A64)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (appliance.category) {
                    "Cooling" -> "❄️"
                    "Heating" -> "🔥"
                    "Laundry" -> "🫧"
                    "Kitchen" -> "🍳"
                    "Lighting" -> "💡"
                    else -> "⚡"
                },
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appliance.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(appliance.category, fontSize = 12.sp,
                        color = categoryColor, fontWeight = FontWeight.Medium)
                    Text("•", fontSize = 12.sp, color = Color.Gray)
                    Text("${appliance.wattage}W", fontSize = 12.sp, color = Color.Gray)
                }
                if (appliance.notes.isNotEmpty()) {
                    Text(appliance.notes, fontSize = 11.sp, color = Color.Gray)
                }
            }
            // Estimated hourly cost
            Column(horizontalAlignment = Alignment.End) {
                Text("~$/hr", fontSize = 10.sp, color = Color.Gray)
                Text(
                    String.format("$%.3f", appliance.wattage / 1000.0 * 0.18),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}