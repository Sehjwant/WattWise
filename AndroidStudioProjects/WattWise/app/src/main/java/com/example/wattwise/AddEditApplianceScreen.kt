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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditApplianceScreen(
    viewModel: WattWiseViewModel,
    applianceId: Int?,
    onDone: () -> Unit
) {
    val isEdit = applianceId != null
    val existing = viewModel.appliances.find { it.id == applianceId }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var wattage by remember { mutableStateOf(existing?.wattage?.toString() ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var wattageError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Track if user made any changes (for unsaved changes warning)
    val originalName = existing?.name ?: ""
    val originalWattage = existing?.wattage?.toString() ?: ""
    val originalNotes = existing?.notes ?: ""
    val originalCategory = existing?.category ?: "Heating"

    // Expanded Dropdown for category — required Android component
    val categories = listOf("Heating", "Cooling", "Laundry", "Lighting", "Kitchen", "Other")
    var selectedCategory by remember { mutableStateOf(existing?.category ?: categories[0]) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // Issue 3 fix: unsaved changes warning dialog
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Check if user has made any changes
    fun hasUnsavedChanges(): Boolean {
        return name != originalName ||
                wattage != originalWattage ||
                notes != originalNotes ||
                selectedCategory != originalCategory
    }

    // Unsaved changes dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to go back without saving?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onDone()
                    }
                ) {
                    Text("Discard", color = Color(0xFFB71C1C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Keep Editing", color = Color(0xFF2E7D32))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Appliance" else "Add Appliance",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Issue 3 fix: warn if there are unsaved changes
                        if (hasUnsavedChanges()) {
                            showUnsavedDialog = true
                        } else {
                            onDone()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEdit) "Update appliance details"
                else "Add a new appliance to your household",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Section 1: Appliance Details ──────────────────────────────
            // Guideline 3: Grouped fields with visually distinct labelled sections
            FormSectionHeader(title = "Appliance Details")
            Spacer(modifier = Modifier.height(8.dp))

            // Appliance Name
            // Guideline 5: Descriptive placeholder text specifying expected format
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isEmpty()) "Appliance name is required" else null
                },
                label = { Text("Appliance Name") },
                // Guideline 5: placeholder shows expected input format
                placeholder = { Text("e.g. Samsung Washing Machine") },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null)
                        Text(nameError!!, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category Dropdown — Required Android component: Expanded Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = isCategoryExpanded,
                onExpandedChange = { isCategoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .focusProperties { canFocus = false },
                    readOnly = true,
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )
                ExposedDropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        val style = getCategoryStyle(category)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = style.icon,
                                        contentDescription = category,
                                        tint = style.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(category)
                                }
                            },
                            onClick = {
                                selectedCategory = category
                                isCategoryExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

// Guideline 3: Section header with bold green label + Divider
// Named differently from ProfileScreen's SectionHeader to avoid conflict
@Composable
fun FormSectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.size(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF2E7D32).copy(alpha = 0.3f)
        )
    }
}