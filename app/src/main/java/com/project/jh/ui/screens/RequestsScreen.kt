package com.project.jh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.StatusBadge
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(onNavigateToChat: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Received", "Sent")
    
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    
    var receivedRequests by remember { mutableStateOf(listOf<RequestData>()) }
    var sentRequests by remember { mutableStateOf(listOf<RequestData>()) }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            // Listen for Received Requests where current user is provider
            db.collection("requests")
                .whereEqualTo("providerId", currentUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        receivedRequests = snapshot.documents.mapNotNull { doc ->
                            RequestData(
                                id = doc.id,
                                requestId = doc.getString("requestId") ?: doc.id,
                                requesterId = doc.getString("requesterId") ?: doc.getString("requesterUid") ?: "",
                                requesterName = doc.getString("requesterName") ?: "",
                                providerId = doc.getString("providerId") ?: doc.getString("providerUid") ?: "",
                                providerName = doc.getString("providerName") ?: "",
                                serviceId = doc.getString("serviceId") ?: "",
                                serviceName = doc.getString("serviceName") ?: doc.getString("serviceTitle") ?: "",
                                message = doc.getString("message") ?: "",
                                status = doc.getString("status") ?: "PENDING",
                                createdAt = doc.getLong("createdAt") ?: doc.getLong("timestamp") ?: 0L,
                                updatedAt = doc.getLong("updatedAt") ?: 0L
                            )
                        }.sortedByDescending { it.createdAt.coerceAtLeast(it.timestamp) }
                    }
                }
                
            // Listen for Sent Requests where current user is requester
            db.collection("requests")
                .whereEqualTo("requesterId", currentUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        sentRequests = snapshot.documents.mapNotNull { doc ->
                            RequestData(
                                id = doc.id,
                                requestId = doc.getString("requestId") ?: doc.id,
                                requesterId = doc.getString("requesterId") ?: doc.getString("requesterUid") ?: "",
                                requesterName = doc.getString("requesterName") ?: "",
                                providerId = doc.getString("providerId") ?: doc.getString("providerUid") ?: "",
                                providerName = doc.getString("providerName") ?: "",
                                serviceId = doc.getString("serviceId") ?: "",
                                serviceName = doc.getString("serviceName") ?: doc.getString("serviceTitle") ?: "",
                                message = doc.getString("message") ?: "",
                                status = doc.getString("status") ?: "PENDING",
                                createdAt = doc.getLong("createdAt") ?: doc.getLong("timestamp") ?: 0L,
                                updatedAt = doc.getLong("updatedAt") ?: 0L
                            )
                        }.sortedByDescending { it.createdAt.coerceAtLeast(it.timestamp) }
                    }
                }
        }
    }

    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedRequestForReview by remember { mutableStateOf<RequestData?>(null) }

    if (showReviewDialog && selectedRequestForReview != null) {
        ReviewDialog(
            requestId = selectedRequestForReview!!.id,
            providerUid = selectedRequestForReview!!.providerId,
            serviceTitle = selectedRequestForReview!!.serviceName,
            onDismiss = { showReviewDialog = false },
            onSuccess = { showReviewDialog = false }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Service Requests", fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = JHPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = JHPrimary
                        )
                    },
                    divider = { /* HorizontalDivider hidden */ }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) JHPrimary else Color.Gray,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val requests = if (selectedTab == 0) receivedRequests else sentRequests
        
        if (requests.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == 0) "No service requests received yet." else "No requests sent yet.",
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    RequestCard(
                        request = request, 
                        isReceived = selectedTab == 0,
                        onMessage = {
                            val otherUid = if (selectedTab == 0) request.requesterId else request.providerId
                            onNavigateToChat(otherUid)
                        },
                        onRate = {
                            selectedRequestForReview = request
                            showReviewDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: RequestData, 
    isReceived: Boolean,
    onMessage: () -> Unit,
    onRate: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var otherUserName by remember { mutableStateOf(if (isReceived) request.requesterName else request.providerName) }
    
    LaunchedEffect(request) {
        val otherUid = if (isReceived) request.requesterId else request.providerId
        if (otherUid.isNotEmpty()) {
            db.collection("users").document(otherUid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        otherUserName = doc.getString("name") ?: otherUserName
                    }
                }
        }
    }

    val statusUpper = request.status.uppercase()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(initials = otherUserName.ifEmpty { "U" }.take(1))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherUserName.ifEmpty { "User" }, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Service: ${request.serviceName}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                StatusBadge(status = statusUpper)
            }

            if (request.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "\"${request.message}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray.copy(alpha = 0.9f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isReceived && statusUpper == "PENDING") {
                    OutlinedButton(
                        onClick = { 
                            val now = System.currentTimeMillis()
                            db.collection("requests").document(request.id).update(
                                "status", "REJECTED",
                                "updatedAt", now
                            ).addOnSuccessListener {
                                val notifId = db.collection("notifications").document().id
                                val notif = NotificationData(
                                    id = notifId,
                                    notificationId = notifId,
                                    recipientUid = request.requesterId,
                                    title = "Request Declined",
                                    description = "Your request for '${request.serviceName}' was declined.",
                                    type = "request",
                                    timestamp = now
                                )
                                db.collection("notifications").document(notifId).set(notif)
                                Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Reject")
                    }

                    Button(
                        onClick = { 
                            val now = System.currentTimeMillis()
                            db.collection("requests").document(request.id).update(
                                "status", "ACCEPTED",
                                "updatedAt", now
                            ).addOnSuccessListener {
                                // Deterministic Chat ID creation
                                val requester = request.requesterId
                                val provider = request.providerId
                                val chatId = listOf(requester, provider).sorted().joinToString("_")

                                val chatData = mapOf(
                                    "chatId" to chatId,
                                    "participantIds" to listOf(requester, provider),
                                    "participantNames" to mapOf(
                                        requester to request.requesterName,
                                        provider to request.providerName
                                    ),
                                    "lastMessage" to "Request Accepted for ${request.serviceName}",
                                    "lastMessageSenderId" to provider,
                                    "lastMessageTime" to now,
                                    "createdAt" to now
                                )
                                db.collection("chats").document(chatId).set(chatData)

                                // Send notification to requester
                                val notifId = db.collection("notifications").document().id
                                val notif = NotificationData(
                                    id = notifId,
                                    notificationId = notifId,
                                    recipientUid = requester,
                                    title = "Request Accepted! 🎉",
                                    description = "Great news! Your request for '${request.serviceName}' was accepted.",
                                    type = "acceptance",
                                    timestamp = now
                                )
                                db.collection("notifications").document(notifId).set(notif)
                                Toast.makeText(context, "Request Accepted! Chat is now open.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JHPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Accept")
                    }
                }

                if (statusUpper == "ACCEPTED") {
                    Button(
                        onClick = onMessage,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JHPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Chat")
                    }
                    
                    if (isReceived) {
                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                db.collection("requests").document(request.id).update(
                                    "status", "COMPLETED",
                                    "updatedAt", now
                                ).addOnSuccessListener {
                                    val notifId = db.collection("notifications").document().id
                                    val notif = NotificationData(
                                        id = notifId,
                                        notificationId = notifId,
                                        recipientUid = request.requesterId,
                                        title = "Service Completed",
                                        description = "Your service '${request.serviceName}' has been marked completed. Please leave a review!",
                                        type = "completed",
                                        timestamp = now
                                    )
                                    db.collection("notifications").document(notifId).set(notif)
                                    Toast.makeText(context, "Service Marked as Completed!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete")
                        }
                    }
                }

                if (statusUpper == "COMPLETED" && !isReceived) {
                    Button(
                        onClick = onRate,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rate Service")
                    }
                }
            }
        }
    }
}
