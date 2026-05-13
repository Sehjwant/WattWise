package com.example.wattwise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

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

    // Track if user made any changes (for unsaved changes warning)
    val originalName = existing?.name ?: ""
    val originalWattage = existing?.wattage?.toString() ?: ""
    val originalNotes = existing?.notes ?: ""
    val originalCategory = existing?.category ?: "Heating"

    // Expanded Dropdown for category — required Android component
    val categories = listOf("Heating", "Cooling", "Laundry", "Lighting", "Kitchen", "Other")
    var selectedCategory by remember { mutableStateOf(existing?.category ?: categories[0]) }

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
    ) { _ -> }
}