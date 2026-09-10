package com.project.jh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.project.jh.R
import com.project.jh.ui.screens.*
import com.project.jh.ui.theme.JHPrimary

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "main" else "auth"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth") {
            AuthScreen(onLoginSuccess = { 
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainContent(navController, onLogout = {
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            })
        }
        
        // Detail routes
        composable("service_detail/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId")
            ServiceDetailsScreen(
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onRequest = { service ->
                    val providerId = service.providerId.ifEmpty { service.providerUid }
                    if (service.id.isNotEmpty() && providerId.isNotEmpty()) {
                        navController.navigate("request_form/${service.id}/$providerId")
                    }
                }
            ) 
        }
        composable("user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId,
                onNavigateToEdit = {}
            ) 
        }
        composable("chat_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChatDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            ) 
        }
        composable(Screen.RequestForm.route) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val providerUid = backStackEntry.arguments?.getString("providerUid") ?: ""
            RequestServiceForm(
                serviceId = serviceId,
                providerUid = providerUid,
                onBack = { navController.popBackStack() }
            ) 
        }
        composable(Screen.PostSkill.route) { 
            OfferSkillForm(onBack = { navController.popBackStack() }) 
        }
        composable(Screen.MyServices.route) {
            MyServicesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPostService = { navController.navigate(Screen.PostSkill.route) }
            )
        }
        composable(Screen.EditProfile.route) { 
            EditProfileScreen(onBack = { navController.popBackStack() }) 
        }
        composable("notifications") { 
            NotificationScreen() 
        }
    }
}

@Composable
fun MainContent(
    parentNavController: androidx.navigation.NavHostController,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ui),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    shadowElevation = 0.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        navItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = null) },
                                label = { Text(screen.label, color = Color.White) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    bottomNavController.navigate(screen.route) {
                                        popUpTo(bottomNavController.graph.findStartDestination().id) {
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
                                    indicatorColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            ) {
                NavHost(
                    navController = bottomNavController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(bottom = 24.dp) // Extra clearance for the floating navigation bar
                ) {
                    composable(Screen.Home.route) { 
                        HomeScreen(
                            onNavigateToService = { serviceId -> 
                                if (serviceId.isNotEmpty()) parentNavController.navigate("service_detail/$serviceId") 
                            },
                            onNavigateToDiscoverCategory = { _ ->
                                bottomNavController.navigate(Screen.Discover.route)
                            },
                            onNavigateToNotifications = { parentNavController.navigate("notifications") },
                            onNavigateToPostService = { parentNavController.navigate(Screen.PostSkill.route) }
                        ) 
                    }
                    composable(Screen.Discover.route) { 
                        DiscoverScreen(
                            onNavigateToService = { serviceId ->
                                if (serviceId.isNotEmpty()) parentNavController.navigate("service_detail/$serviceId")
                            }
                        ) 
                    }
                    composable(Screen.Requests.route) { 
                        RequestsScreen(
                            onNavigateToChat = { userId ->
                                if (userId.isNotEmpty()) parentNavController.navigate("chat_detail/$userId")
                            }
                        ) 
                    }
                    composable(Screen.Chat.route) { 
                        ChatListScreen(
                            onNavigateToChat = { userId ->
                                if (userId.isNotEmpty()) parentNavController.navigate("chat_detail/$userId")
                            }
                        ) 
                    }
                    composable(Screen.Profile.route) { 
                        ProfileScreen(
                            onNavigateToEdit = { 
                                parentNavController.navigate(Screen.EditProfile.route)
                            },
                            onLogout = onLogout
                        ) 
                    }
                }
            }
        }
    }
}
