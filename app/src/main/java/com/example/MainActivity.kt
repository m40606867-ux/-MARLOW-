package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BreathingScreen
import com.example.ui.screens.MarlowHomeScreen
import com.example.ui.screens.MarlowHabitScreen
import com.example.ui.screens.MarlowPrayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MarlowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppGatekeeper()
            }
        }
    }
}

@Composable
fun AppGatekeeper() {
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn) {
        MainLayout(authViewModel = authViewModel)
    } else {
        AuthScreen(authViewModel = authViewModel)
    }
}

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object MarlowDesk : Screen(
        route = "marlow_desk",
        title = "Marlow Desk",
        icon = {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Marlow Companion",
                modifier = Modifier.size(24.dp)
            )
        }
    )

    object HabitShield : Screen(
        route = "habit_shield",
        title = "Habit Shield",
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Active Opponent Shield",
                modifier = Modifier.size(24.dp)
            )
        }
    )

    object Prayers : Screen(
        route = "prayer_tracker",
        title = "Prayers",
        icon = {
            Icon(
                imageVector = Icons.Default.Mosque,
                contentDescription = "Prayer Accountability",
                modifier = Modifier.size(24.dp)
            )
        }
    )

    object CalmSpace : Screen(
        route = "calm_space",
        title = "Calm",
        icon = {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = "Calm Breathing",
                modifier = Modifier.size(24.dp)
            )
        }
    )
}

@Composable
fun MainLayout(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val viewModel: MarlowViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.MarlowDesk,
        Screen.HabitShield,
        Screen.Prayers,
        Screen.CalmSpace
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navigationItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = screen.icon,
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF0F172A),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MarlowDesk.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MarlowDesk.route) {
                MarlowHomeScreen(
                    viewModel = viewModel,
                    onLogout = { authViewModel.logout() }
                )
            }
            composable(Screen.HabitShield.route) {
                MarlowHabitScreen(viewModel = viewModel)
            }
            composable(Screen.Prayers.route) {
                MarlowPrayerScreen(viewModel = viewModel)
            }
            composable(Screen.CalmSpace.route) {
                BreathingScreen()
            }
        }
    }
}
