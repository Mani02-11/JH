package com.project.jh.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.jh.ui.screens.*
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JHPrimary,
                            selectedTextColor = JHPrimary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = JHPrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToService = { navController.navigate("service_detail/1") },
                    onNavigateToUser = { navController.navigate("user_detail/1") },
                    onNavigateToNotifications = { navController.navigate("notifications") }
                ) 
            }
            composable(Screen.Discover.route) { 
                DiscoverScreen(
                    onNavigateToUser = { navController.navigate("user_detail/1") }
                ) 
            }
            composable(Screen.Requests.route) { RequestsScreen() }
            composable(Screen.Chat.route) { 
                ChatListScreen(
                    onNavigateToChat = { navController.navigate("chat_detail/Rahul") }
                ) 
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) }
                ) 
            }
            
            // Detail screens
            composable("service_detail/{serviceId}") { 
                ServiceDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onRequest = { navController.navigate("request_form/1") }
                ) 
            }
            composable("user_detail/{userId}") { 
                ProfileScreen(
                    userId = it.arguments?.getString("userId"),
                    onNavigateToEdit = {}
                ) 
            }
            composable("chat_detail/{userId}") { 
                ChatDetailScreen(userId = it.arguments?.getString("userId") ?: "") 
            }
            composable(Screen.RequestForm.route) { 
                RequestServiceForm(onBack = { navController.popBackStack() }) 
            }
            composable(Screen.EditProfile.route) { 
                OfferSkillForm(onBack = { navController.popBackStack() }) 
            }
            composable("notifications") { 
                NotificationScreen() 
            }
        }
    }
}
