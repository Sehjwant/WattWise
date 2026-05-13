package com.fit5046.wattwise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home
    ),
    MONITOR(
        route = "monitor",
        label = "Monitor",
        icon = Icons.Default.ShowChart
    ),
    HISTORY(
        route = "history",
        label = "History",
        icon = Icons.Default.BarChart
    ),
    APPLIANCES(
        route = "appliances",
        label = "Appliances",
        icon = Icons.Default.Power
    ),
    PROFILE(
        route = "profile",
        label = "Profile",
        icon = Icons.Default.Person
    )
}
