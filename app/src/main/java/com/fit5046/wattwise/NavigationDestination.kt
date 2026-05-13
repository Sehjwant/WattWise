package com.fit5046.wattwise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

// ── Bottom Bar Navigation Destinations ────────────────────────────────────────
// Each enum entry maps a route to a label and a semantically correct icon.
//
// Previous issue: Monitor used DateRange (calendar), History used Build (wrench),
// Appliances used Search (magnifying glass) — all semantically wrong and visually
// confusing in screenshots. Fixed below.
// ─────────────────────────────────────────────────────────────────────────────

enum class NavigationDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home            // House icon — correct for dashboard
    ),
    MONITOR(
        route = "monitor",
        label = "Monitor",
        icon = Icons.Default.ShowChart       // Line-chart icon — correct for live sensor feed
    ),
    HISTORY(
        route = "history",
        label = "History",
        icon = Icons.Default.BarChart        // Bar-chart icon — correct for historical charts
    ),
    APPLIANCES(
        route = "appliances",
        label = "Appliances",
        icon = Icons.Default.Power           // Power/plug icon — correct for appliance management
    ),
    PROFILE(
        route = "profile",
        label = "Profile",
        icon = Icons.Default.Person          // Person icon — correct for profile/settings
    )
}
