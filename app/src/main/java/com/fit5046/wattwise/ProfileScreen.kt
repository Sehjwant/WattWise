package com.fit5046.wattwise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: WattWiseViewModel) {

    val context = androidx.compose.ui.platform.LocalContext.current
    var budgetError by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 23, initialMinute = 0)
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var suburbError by remember { mutableStateOf<String?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var memberToRemoveIndex by remember { mutableStateOf(-1) }
    var memberToRemoveName by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Profile completion calculation
    val completionFields = listOf(
        viewModel.fullName.isNotBlank(),
        viewModel.suburb.isNotBlank(),
        viewModel.budgetGoal.isNotBlank(),
        viewModel.billingType.isNotBlank(),
        viewModel.offPeakHours.isNotBlank()
    )
    val completionFraction = completionFields.count { it } / completionFields.size.toFloat()

    // Remove member dialog
    if (showRemoveDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Member", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C)) },
            text = {
                Text("Are you sure you want to remove $memberToRemoveName from your household? " +
                        "They will lose access to all shared energy data and alerts.", fontSize = 14.sp)
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showRemoveDialog = false
                    viewModel.removeMember(memberToRemoveIndex)
                    android.widget.Toast.makeText(context,
                        "$memberToRemoveName has been removed from the household",
                        android.widget.Toast.LENGTH_SHORT).show()
                }) { Text("Remove", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel", color = Color(0xFF2E7D32))
                }
            }
        )
    }

    // Sign out dialog
    if (showLogoutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C)) },
            text = { Text("Are you sure you want to sign out of WattWise?", fontSize = 14.sp) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) { Text("Sign Out", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF2E7D32))
                }
            }
        )
    }

    // TimePicker dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Off-Peak Start Time", fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(8.dp))
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        TextButton(onClick = {
                            showTimePicker = false
                            val h = timePickerState.hour
                            val m = timePickerState.minute
                            val amPm = if (h < 12) "AM" else "PM"
                            val h12 = if (h % 12 == 0) 12 else h % 12
                            viewModel.offPeakHours = String.format("%02d:%02d %s", h12, m, amPm)
                        }) { Text("OK", color = Color(0xFF2E7D32)) }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = Color(0xFF69F0AE), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile & Settings", fontWeight = FontWeight.Bold, color = Color.White)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Profile Avatar with Completion Ring
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { completionFraction },
                        modifier = Modifier.size(96.dp),
                        color = Color(0xFF2E7D32),
                        trackColor = Color(0xFFE0E0E0),
                        strokeWidth = 6.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .background(
                                if (viewModel.isOwner) Color(0xFF1B5E20) else Color(0xFF1565C0),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.fullName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (completionFraction < 1f) {
                Text(
                    text = "Profile ${(completionFraction * 100).toInt()}% complete",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LaunchedEffect(Unit) {
                    android.widget.Toast.makeText(
                        context,
                        "Profile 100% complete",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Role Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .background(
                            if (viewModel.isOwner) Color(0xFF1B5E20) else Color(0xFF1565C0),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (viewModel.isOwner) " Household Owner" else " Member",
                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            // SECTION 1: Account Information
            SectionHeader(title = "Account Information")

            if (!isEditingProfile) {
                // Display mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Name", fontSize = 12.sp, color = Color.Gray)
                            Text(viewModel.fullName.ifBlank { "Not set" },
                                fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Suburb", fontSize = 12.sp, color = Color.Gray)
                            Text(viewModel.suburb.ifBlank { "Not set" },
                                fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Household ID", fontSize = 12.sp, color = Color.Gray)
                            Text(viewModel.householdId, fontSize = 14.sp,
                                fontWeight = FontWeight.Medium, color = Color(0xFF1B5E20))
                        }
                    }
                }
                Button(
                    onClick = { isEditingProfile = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Edit Profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Edit mode
                OutlinedTextField(
                    value = viewModel.fullName,
                    onValueChange = {
                        viewModel.fullName = it
                        fullNameError = if (it.trim().isEmpty()) "Full name cannot be empty" else null
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. John Smith") },
                    isError = fullNameError != null,
                    supportingText = {
                        if (fullNameError != null)
                            Text(fullNameError!!, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )
                if (viewModel.isOwner) {
                    OutlinedTextField(
                        value = viewModel.suburb,
                        onValueChange = {
                            viewModel.suburb = it
                            suburbError = if (it.trim().isEmpty()) "Suburb cannot be empty" else null
                        },
                        label = { Text("Suburb") },
                        placeholder = { Text("e.g. Clayton, VIC") },
                        isError = suburbError != null,
                        supportingText = {
                            if (suburbError != null)
                                Text(suburbError!!, color = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = viewModel.suburb,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Suburb") },
                        supportingText = {
                            Text("Only the Household Owner can change the suburb",
                                color = Color.Gray, fontSize = 11.sp)
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Locked",
                                tint = Color.Gray, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }
                OutlinedTextField(
                    value = viewModel.householdId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Household ID") },
                    trailingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Read only",
                            tint = Color.Gray, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                // Save/Cancel buttons
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isEditingProfile = false },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) { Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            if (viewModel.fullName.trim().isEmpty()) {
                                fullNameError = "Full name cannot be empty"
                            }
                            if (viewModel.suburb.trim().isEmpty()) {
                                suburbError = "Suburb cannot be empty"
                            }
                            if (viewModel.fullName.trim().isNotEmpty() &&
                                viewModel.suburb.trim().isNotEmpty()) {
                                isEditingProfile = false
                                android.widget.Toast.makeText(context,
                                    "Settings saved successfully",
                                    android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Save", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                }
            }


            // Household Size
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Household Size", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "${viewModel.householdMembers.size} person${if (viewModel.householdMembers.size != 1) "s" else ""}",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = "Auto-calculated",
                            tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Auto-calculated", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //  SECTION 2: Energy Budget
            SectionHeader(title = "Energy Budget")

            OutlinedTextField(
                value = viewModel.budgetGoal,
                onValueChange = { input ->
                    if (viewModel.isOwner) {
                        viewModel.budgetGoal = input
                        viewModel.has80PercentAlertFired = false
                        viewModel.has100PercentAlertFired = false
                        val v = input.toFloatOrNull()
                        budgetError = when {
                            input.isEmpty() -> "Budget is required"
                            v == null -> "Please enter a valid number"
                            v < 1 || v > 9999 -> "Please enter a monthly budget between \$1 and \$9,999"
                            else -> null
                        }
                    }
                },
                label = { Text("Daily Budget Goal (kWh)") },
                placeholder = { Text("e.g. 20.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                readOnly = !viewModel.isOwner,
                enabled = viewModel.isOwner,
                isError = budgetError != null,
                supportingText = {
                    when {
                        budgetError != null -> Text(budgetError!!, color = MaterialTheme.colorScheme.error)
                        !viewModel.isOwner -> Text("Only the Household Owner can edit the budget",
                            color = Color.Gray, fontSize = 11.sp)
                    }
                },
                trailingIcon = {
                    if (!viewModel.isOwner)
                        Icon(Icons.Default.Lock, contentDescription = "Locked",
                            tint = Color.Gray, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32),
                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledLabelColor = Color.Gray,
                    disabledTextColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 3: Billing Preferences
            SectionHeader(title = "Billing Preferences")

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Billing Type", fontSize = 13.sp, color = Color.Gray)
                if (!viewModel.isOwner) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Owner only", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            val billingOptions = listOf("Flat Rate", "Time-of-Use", "Solar Feed-In")
            billingOptions.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = viewModel.billingType == option,
                        onClick = { if (viewModel.isOwner) viewModel.billingType = option },
                        enabled = viewModel.isOwner,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF2E7D32),
                            disabledSelectedColor = Color.Gray,
                            disabledUnselectedColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(option, fontSize = 14.sp,
                            color = if (viewModel.isOwner) Color(0xFF212121) else Color.Gray)
                        Text(
                            text = when (option) {
                                "Flat Rate" -> "No tariff alerts — fixed rate all day"
                                "Time-of-Use" -> "Peak/shoulder/off-peak tariff alerts enabled"
                                "Solar Feed-In" -> "Solar production context active"
                                else -> ""
                            },
                            fontSize = 11.sp, color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.offPeakHours,
                onValueChange = {},
                readOnly = true,
                label = { Text("Off-Peak Start Time") },
                modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Time",
                        modifier = Modifier.clickable { showTimePicker = true }.size(22.dp),
                        tint = Color(0xFF2E7D32))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 4: Notification Settings
            SectionHeader(title = "Notification Settings")

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Peak-Hour Alerts", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Notify when tariff enters peak tier", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.notificationsEnabled,
                    onCheckedChange = { viewModel.notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2E7D32)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Household Sharing", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Share energy alerts with household members via Firebase",
                        fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.householdSharing,
                    onCheckedChange = { viewModel.householdSharing = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2E7D32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 5: Household Members (Owner only)
            if (viewModel.isOwner) {
                SectionHeader(title = "Household Members")
                Text("Remove members who have left the household",
                    fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                viewModel.householdMembers.forEachIndexed { index, member ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (member.isOwner) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (member.isOwner) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                    CircleShape),
                                contentAlignment = Alignment.Center) {
                                Text(member.name.first().uppercase(), color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(member.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (member.isOwner) {
                                        Box(modifier = Modifier
                                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text("Owner", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                                Text(member.email, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (!member.isOwner) {
                                IconButton(onClick = {
                                    memberToRemoveIndex = index
                                    memberToRemoveName = member.name
                                    showRemoveDialog = true
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Member",
                                        tint = Color(0xFFB71C1C), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Text("ℹ️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New members can join by entering your Household ID " +
                                "(${viewModel.householdId}) during registration.",
                            fontSize = 12.sp, color = Color(0xFF0D47A1))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
            ) {
                Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f),
            color = Color(0xFF2E7D32).copy(alpha = 0.4f), thickness = 1.dp)
    }
}