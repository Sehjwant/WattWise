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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

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

    // Member was removed from household
    if (viewModel.memberStatus == "removed") {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Removed from Household",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFFB71C1C)
                )
            },
            text = {
                Text("You have been removed from this household by the owner. Please contact the household owner or register with a different household.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.logout() }
                ) {
                    Text("OK", color = Color(0xFFB71C1C), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        )
        return
    }

    // Member is pending approval
    if (viewModel.memberStatus == "pending") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "⏳",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Waiting for Approval",
                        fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your request to join household ${viewModel.householdId} is pending. The household owner will review your request.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Text("Sign Out", fontSize = 16.sp)
                    }
                }
            }
        }
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
    // Google Sign-In welcome/registration dialog
    if (viewModel.googleSignInType == "new") {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Account Registered!",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            },
            text = {
                Text("Welcome ${viewModel.googleDisplayName}! Your Google account has been registered with WattWise. Your household dashboard is ready.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.googleSignInType = null }
                ) {
                    Text("Continue", color = Color(0xFF2E7D32), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        )
    }

    if (viewModel.googleSignInType == "existing") {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Welcome Back!",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            },
            text = {
                Text("Signed in as ${viewModel.googleDisplayName}. Your household dashboard is ready.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.googleSignInType = null }
                ) {
                    Text("Continue", color = Color(0xFF2E7D32), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
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
                                    Icon(destination.icon, contentDescription = destination.label)
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