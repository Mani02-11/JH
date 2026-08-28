package com.project.jh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.jh.ui.components.EmptyStateView
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Received", "Sent")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Service Requests", fontWeight = FontWeight.Bold) }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (selectedTab == 0) {
            ReceivedRequests(modifier = Modifier.padding(padding))
        } else {
            SentRequests(modifier = Modifier.padding(padding))
        }
    }
}

@Composable
fun ReceivedRequests(modifier: Modifier) {
    // Placeholder for data
    val requests = listOf(
        RequestItem("Arun Kumar", "Android App Development", "Pending", "28 Aug"),
        RequestItem("Sneha Rao", "UI Design", "Accepted", "27 Aug")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(requests) { request ->
            RequestCard(request, isReceived = true)
        }
    }
}

@Composable
fun SentRequests(modifier: Modifier) {
    EmptyStateView(
        title = "No requests sent yet",
        subtitle = "When you request a service, it will appear here.",
        icon = Icons.Default.List
    )
}

@Composable
fun RequestCard(request: RequestItem, isReceived: Boolean) {
    JHCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                ProfileAvatar(initials = request.name.take(1))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.name, fontWeight = FontWeight.Bold)
                    Text(request.service, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                StatusBadge(status = request.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "I need help with my college project. Can we discuss the timeline?",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            
            if (isReceived && request.status == "Pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

data class RequestItem(val name: String, val service: String, val status: String, val date: String)
