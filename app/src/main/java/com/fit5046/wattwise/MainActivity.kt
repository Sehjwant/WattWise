package com.fit5046.wattwise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.fit5046.wattwise.ui.theme.WattWiseTheme

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            WattWiseTheme {
                WattWiseApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WattWiseApp() {
    val viewModel: WattWiseViewModel = viewModel()

    if (!viewModel.isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { viewModel.isLoggedIn = true },
            onNavigateToRegister = {},
            viewModel = viewModel
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBar = currentRoute in listOf("search", "add_appliance", "messaging") ||
            currentRoute?.startsWith("edit_appliance") == true

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startSimulator(context)
    }

    LaunchedEffect(viewModel.dailyCumulativeKwh, viewModel.budgetGoal) {
        WorkManagerScheduler.schedule(
            context       = context,
            cumulativeKwh = viewModel.dailyCumulativeKwh,
            budgetGoal    = viewModel.budgetGoal.toDoubleOrNull() ?: 20.0
        )
    }

    // Budget alert popup — shows once when threshold is crossed
    if (viewModel.showBudgetPopup != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showBudgetPopup = null },
            title = {
                Text(
                    if (viewModel.budgetProgress >= 1.0f) "Budget Exceeded!"
                    else "Budget Warning!",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (viewModel.budgetProgress >= 1.0f) Color(0xFFB71C1C)
                    else Color(0xFFE65100)
                )
            },
            text = {
                Text(viewModel.showBudgetPopup ?: "")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.showBudgetPopup = null }
                ) {
                    Text("OK", color = Color(0xFF2E7D32))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1B5E20),
                    contentColor   = Color.White
                ) {
                    NavigationDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            icon = {
                                if (destination == NavigationDestination.HOME) {
                                    BadgedBox(badge = {
                                        val alertCount = viewModel.messages.count {
                                            it.type == MessageType.ALERT
                                        }
                                        if (alertCount > 0) {
                                            Badge(containerColor = Color(0xFFE53935)) {
                                                Text("$alertCount", color = Color.White)
                                            }
                                        }
                                    }) {
                                        Icon(destination.icon, contentDescription = destination.label)
                                    }
                                } else {
                                    Icon(destination.icon, contentDescription = destination.label)
                                }
                            },
                            label    = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick  = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Color(0xFF69F0AE),
                                selectedTextColor   = Color(0xFF69F0AE),
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                                indicatorColor      = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = NavigationDestination.HOME.route,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable(NavigationDestination.HOME.route) {
                HomeScreen(
                    viewModel             = viewModel,
                    onNavigateToMessaging = { navController.navigate("messaging") }
                )
            }
            composable(NavigationDestination.MONITOR.route) {
                LiveMonitorScreen(viewModel = viewModel)
            }
            composable(NavigationDestination.HISTORY.route) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(NavigationDestination.APPLIANCES.route) {
                ApplianceManagerScreen(
                    viewModel          = viewModel,
                    onNavigateToAdd    = { navController.navigate("add_appliance") },
                    onNavigateToEdit   = { id -> navController.navigate("edit_appliance/$id") },
                    onNavigateToSearch = { navController.navigate("search") }
                )
            }
            composable(NavigationDestination.PROFILE.route) {
                ProfileScreen(viewModel = viewModel)
            }
            composable("search") {
                SearchScreen(viewModel = viewModel)
            }
            composable("add_appliance") {
                AddEditApplianceScreen(
                    viewModel   = viewModel,
                    applianceId = null,
                    onDone      = { navController.popBackStack() }
                )
            }
            composable("edit_appliance/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                AddEditApplianceScreen(
                    viewModel   = viewModel,
                    applianceId = id,
                    onDone      = { navController.popBackStack() }
                )
            }
            composable("messaging") {
                MessagingScreen(
                    viewModel = viewModel,
                    onBack    = { navController.popBackStack() }
                )
            }
        }
    }
}