package com.project.jh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notifications", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    JHCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = notification.icon,
                contentDescription = null,
                tint = JHPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(notification.title, fontWeight = FontWeight.Bold)
                Text(notification.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

data class NotificationItem(val title: String, val description: String, val icon: ImageVector)
val notifications = listOf(
    NotificationItem("New Request", "Rahul Kumar sent you a service request.", Icons.Default.List),
    NotificationItem("New Message", "You received a new message from Sneha.", Icons.Default.Message),
    NotificationItem("New Review", "You received a 5-star review.", Icons.Default.Star)
)
