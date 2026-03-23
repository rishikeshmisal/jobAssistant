package com.jobassistant.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jobassistant.ui.screens.dashboard.DashboardScreen
import com.jobassistant.ui.screens.detail.AddJobScreen
import com.jobassistant.ui.screens.detail.JobDetailScreen
import com.jobassistant.ui.screens.insights.InsightsScreen
import com.jobassistant.ui.screens.onboarding.OnboardingScreen
import com.jobassistant.ui.screens.profile.ProfileScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    BottomNavItem(Screen.Insights, "Insights", Icons.Filled.Insights)
)

private val screensWithoutBottomNav = setOf(
    Screen.Onboarding.route,
    "job_detail/{jobId}"
)

@Composable
fun AppNavigation(
    isOnboardingComplete: Boolean,
    sharedImageUri: android.net.Uri? = null,
    onSharedImageConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Phase 8 (8.3): when a share-sheet imageUri arrives, automatically open AddJob
    LaunchedEffect(sharedImageUri) {
        if (sharedImageUri != null) {
            navController.navigate(Screen.AddJob.route) {
                launchSingleTop = true
            }
        }
    }

    val showBottomBar = currentRoute !in screensWithoutBottomNav &&
            currentRoute?.startsWith("job_detail/") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingComplete) Screen.Dashboard.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onJobClick = { jobId ->
                        navController.navigate(Screen.JobDetail.createRoute(jobId))
                    },
                    onAddJobClick = {
                        navController.navigate(Screen.AddJob.route)
                    }
                )
            }
            composable(
                route = Screen.JobDetail.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                JobDetailScreen(
                    jobId = jobId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddJob.route) {
                AddJobScreen(
                    onBack = { navController.popBackStack() },
                    initialImageUri = sharedImageUri,
                    onImageConsumed = onSharedImageConsumed
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
        }
    }
}
