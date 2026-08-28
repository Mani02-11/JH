package com.project.jh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onNavigateToChat: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(chatList) { chat ->
                ChatListItem(chat, onNavigateToChat)
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatPreview, onClick: (String) -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
        headlineContent = { Text(chat.name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(chat.lastMessage, maxLines = 1, color = Color.Gray) },
        leadingContent = { 
            Box {
                ProfileAvatar(initials = chat.name.take(1), size = 50)
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(chat.time, fontSize = 12.sp, color = Color.Gray)
                if (chat.unreadCount > 0) {
                    Badge(containerColor = JHPrimary) {
                        Text(chat.unreadCount.toString(), color = Color.White)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(userId: String) {
    var messageText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileAvatar(initials = userId.take(1), size = 32)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(userId, style = MaterialTheme.typography.titleMedium)
                            Text("Online", style = MaterialTheme.typography.labelSmall, color = Color.Green)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            ChatInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = { messageText = "" }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isFromMe) JHPrimary else Color.LightGray.copy(alpha = 0.2f)
    val textColor = if (message.isFromMe) Color.White else Color.Black

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 0.dp,
                bottomEnd = if (message.isFromMe) 0.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatInput(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth().imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                colors = IconButtonDefaults.iconButtonColors(containerColor = JHPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

data class ChatPreview(val name: String, val lastMessage: String, val time: String, val unreadCount: Int, val isOnline: Boolean)
val chatList = listOf(
    ChatPreview("Rahul Kumar", "Yes, I can help with your Android project.", "8:42 PM", 2, true),
    ChatPreview("Sneha Rao", "I'll send the files tomorrow.", "Yesterday", 0, false),
    ChatPreview("Amit Patel", "Great work!", "2 days ago", 0, true)
)

data class Message(val text: String, val isFromMe: Boolean, val time: String)
val messages = listOf(
    Message("Hi Rahul, I saw your profile and wanted to discuss an Android project.", true, "8:30 PM"),
    Message("Hello! I'd love to hear more. What kind of project is it?", false, "8:35 PM"),
    Message("It's a student marketplace app called JustHire.", true, "8:40 PM"),
    Message("Yes, I can help with your Android project.", false, "8:42 PM")
)
