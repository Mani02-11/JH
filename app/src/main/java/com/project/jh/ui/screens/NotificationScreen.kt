package com.project.jh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    var notificationList by remember { mutableStateOf(listOf<NotificationData>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("notifications")
                .whereEqualTo("recipientUid", currentUid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        notificationList = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(NotificationData::class.java)?.copy(id = doc.id, notificationId = doc.id)
                            } catch (ex: Exception) {
                                NotificationData(
                                    id = doc.id,
                                    notificationId = doc.getString("notificationId") ?: doc.id,
                                    recipientUid = doc.getString("recipientUid") ?: "",
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    type = doc.getString("type") ?: "info",
                                    isRead = doc.getBoolean("isRead") ?: false,
                                    timestamp = doc.getLong("timestamp") ?: 0L
                                )
                            }
                        }.sortedByDescending { it.timestamp }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JHPrimary)
            }
        } else if (notificationList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No new notifications", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notificationList) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationData) {
    val db = FirebaseFirestore.getInstance()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                db.collection("notifications").document(notification.id).update("isRead", true)
            },
        shape = RoundedCornerShape(24.dp),
        color = if (notification.type == "acceptance") JHPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        val contentColor = Color.White
        
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(JHPrimary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notification.description,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = contentColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val timeText = formatTimestamp(notification.timestamp)
                    Text(timeText, fontSize = 12.sp, color = contentColor.copy(alpha = 0.6f))
                }
                if (!notification.isRead) {
                    Text(
                        text = "New",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JHPrimary
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
