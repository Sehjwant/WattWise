package com.fit5046.wattwise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class CategoryStyle(
    val color: Color,
    val backgroundColor: Color,
    val icon: ImageVector
)

fun getCategoryStyle(category: String): CategoryStyle = when (category) {
    "Cooling"  -> CategoryStyle(Color(0xFF1565C0), Color(0xFFE3F2FD), Icons.Default.AcUnit)
    "Heating"  -> CategoryStyle(Color(0xFFB71C1C), Color(0xFFFFEBEE), Icons.Default.Fireplace)
    "Laundry"  -> CategoryStyle(Color(0xFF6A1B9A), Color(0xFFF3E5F5), Icons.Default.LocalLaundryService)
    "Kitchen"  -> CategoryStyle(Color(0xFFE65100), Color(0xFFFFF3E0), Icons.Default.Kitchen)
    "Lighting" -> CategoryStyle(Color(0xFFF9A825), Color(0xFFFFFDE7), Icons.Default.LightMode)
    else       -> CategoryStyle(Color(0xFF455A64), Color(0xFFECEFF1), Icons.Default.Power)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplianceManagerScreen(
    viewModel: WattWiseViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToSearch: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appliance Manager", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search Appliances", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20), titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            if (viewModel.isOwner) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Appliance")
                }
            }
        }
    ) { innerPadding ->Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${viewModel.appliances.size} appliances", fontSize = 14.sp, color = Color.Gray)
            Box(
                modifier = Modifier
                    .background(if (viewModel.isOwner) Color(0xFF2E7D32) else Color(0xFF1565C0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (viewModel.isOwner) "Owner" else "Member (read-only)",
                    fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium
                )
            }
        }

        if (viewModel.appliances.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Power, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("No appliances added yet", color = Color.Gray)
                    if (viewModel.isOwner) Text("Tap + to add your first appliance", fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(viewModel.appliances) { index, appliance ->
                    ApplianceCard(
                        appliance = appliance,
                        isOwner = viewModel.isOwner,
                        onEdit = { onNavigateToEdit(appliance.id) },
                        onDelete = { viewModel.deleteAppliance(appliance.id) }
                    )
                    if (index < viewModel.appliances.size - 1) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
    }
}
@Composable
fun ApplianceCard(
    appliance: Appliance,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val style = getCategoryStyle(appliance.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(style.backgroundColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = style.icon, contentDescription = appliance.category, tint = style.color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appliance.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF212121))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.background(style.backgroundColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(appliance.category, fontSize = 11.sp, color = style.color, fontWeight = FontWeight.Medium)
                    }
                    Text("·", fontSize = 12.sp, color = Color.Gray)
                    Text("${appliance.wattage}W", fontSize = 12.sp, color = Color.Gray)
                }
                if (appliance.notes.isNotEmpty()) Text(appliance.notes, fontSize = 11.sp, color = Color.Gray)
            }
            if (isOwner) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFB71C1C), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
