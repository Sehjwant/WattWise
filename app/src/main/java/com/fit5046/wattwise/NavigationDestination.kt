package com.fit5046.wattwise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
    PROFILE(
        route = "profile",
        label = "Profile",
        icon = Icons.Default.Person
    )
}
