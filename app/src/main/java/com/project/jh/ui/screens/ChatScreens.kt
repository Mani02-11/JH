package com.project.jh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.google.firebase.firestore.Query
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onNavigateToChat: (String) -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    
    var chats by remember { mutableStateOf(listOf<ChatData>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("chats")
                .whereArrayContains("participantIds", currentUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        chats = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ChatData::class.java)?.copy(id = doc.id, chatId = doc.id)
                        }.sortedByDescending { it.lastMessageTime }
                    }
                    isLoading = false
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JHPrimary)
            }
        } else if (chats.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "No active conversations.\nConversations appear after a service request is accepted.",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chats) { chat ->
                    val otherUid = chat.participantIds.firstOrNull { it != currentUid } 
                        ?: chat.participants.firstOrNull { it != currentUid } 
                        ?: ""
                    ChatListItem(otherUid, chat, onNavigateToChat)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(otherUid: String, chat: ChatData, onClick: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var otherName by remember { mutableStateOf("") }

    LaunchedEffect(otherUid) {
        if (otherUid.isNotEmpty()) {
            db.collection("users").document(otherUid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        otherName = doc.getString("name")?.ifBlank { null } 
                            ?: doc.getString("email")?.substringBefore("@") 
                            ?: "Student"
                    }
                }
        }
    }

    Surface(
        onClick = { onClick(otherUid) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(initials = otherName.ifEmpty { "U" }.take(1), size = 50)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = otherName.ifEmpty { "Loading..." }, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.lastMessage.ifEmpty { "Tap to open conversation" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            if (chat.lastMessageTime > 0) {
                Text(
                    text = formatTimestamp(chat.lastMessageTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(userId: String, onBack: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    val chatId = remember(userId, currentUid) {
        listOf(currentUid, userId).sorted().joinToString("_")
    }

    var messages by remember { mutableStateOf(listOf<MessageData>()) }
    var otherName by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        otherName = doc.getString("name")?.ifBlank { null } 
                            ?: doc.getString("email")?.substringBefore("@") 
                            ?: "Student"
                    }
                }
        }

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MessageData::class.java)?.copy(id = doc.id, messageId = doc.id)
                    }
                }
            }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileAvatar(initials = otherName.ifEmpty { "U" }.take(1), size = 36)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(otherName.ifEmpty { "Loading..." }, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank()) {
                                sendMessage(db, chatId, currentUid, userId, textToSend)
                                inputText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = JHPrimary),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start your conversation with $otherName", 
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val isMine = message.senderId == currentUid || message.senderUid == currentUid
                    MessageBubble(message, isMine)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageData, isMine: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isMine) JHPrimary else Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(message.text, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End),
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun sendMessage(db: FirebaseFirestore, chatId: String, senderId: String, receiverId: String, text: String) {
    val timestamp = System.currentTimeMillis()
    val messageId = db.collection("chats").document(chatId).collection("messages").document().id
    val message = MessageData(
        id = messageId,
        messageId = messageId,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        timestamp = timestamp,
        seen = false,
        type = "text"
    )
    
    db.collection("chats").document(chatId).collection("messages").document(messageId).set(message)
    
    val chatUpdate = mapOf(
        "chatId" to chatId,
        "lastMessage" to text,
        "lastMessageSenderId" to senderId,
        "lastMessageTime" to timestamp,
        "participantIds" to listOf(senderId, receiverId),
        "participants" to listOf(senderId, receiverId)
    )
    db.collection("chats").document(chatId).set(chatUpdate)

    // Send Notification
    val notifId = db.collection("notifications").document().id
    val notif = NotificationData(
        id = notifId,
        notificationId = notifId,
        recipientUid = receiverId,
        title = "New Message",
        description = text,
        type = "message",
        timestamp = timestamp
    )
    db.collection("notifications").document(notifId).set(notif)
}
