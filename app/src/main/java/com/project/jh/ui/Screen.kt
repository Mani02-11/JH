package com.project.jh.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Discover : Screen("discover", "Discover", Icons.Default.Search)
    object Requests : Screen("requests", "Requests", Icons.AutoMirrored.Filled.List)
    object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    
    // Detail screens
    object ServiceDetail : Screen("service_detail/{serviceId}", "Service")
    object UserDetail : Screen("user_detail/{userId}", "Student")
    object PostSkill : Screen("post_skill", "Offer Skill")
    object RequestForm : Screen("request_form/{serviceId}", "Request Service")
    object EditProfile : Screen("edit_profile", "Edit Profile")
}

val navItems = listOf(
    Screen.Home,
    Screen.Discover,
    Screen.Requests,
    Screen.Chat,
    Screen.Profile
)
