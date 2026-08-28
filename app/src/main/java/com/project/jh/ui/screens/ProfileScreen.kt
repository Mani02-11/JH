package com.project.jh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    onNavigateToEdit: () -> Unit = {}
) {
    val isSelf = userId == null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelf) "My Profile" else "Student Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isSelf) {
                        IconButton(onClick = onNavigateToEdit) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ProfileHeader(isSelf)
            }
            
            item {
                StatsRow()
            }
            
            item {
                BioSection()
            }
            
            item {
                SkillsSection()
            }
            
            item {
                ServicesSection()
            }
            
            if (!isSelf) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Message")
                        }
                        JHButton(
                            text = "Request Service",
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(isSelf: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            ProfileAvatar(initials = "JD", size = 100)
            if (!isSelf) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("John Doe", style = MaterialTheme.typography.headlineMedium)
        Text("LPU • Computer Science • 2nd Year", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFBC02D)
            )
            Text(" 4.8", fontWeight = FontWeight.Bold)
            Text(" (24 reviews)", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(label = "Reviews", value = "24")
        StatItem(label = "Services", value = "18")
        StatItem(label = "Rating", value = "4.8")
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = JHPrimary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}

@Composable
fun BioSection() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("About Me", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Android developer passionate about building useful applications and helping students with technical projects.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun SkillsSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Skills", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Kotlin", "Java", "Firebase", "Android", "UI/UX").forEach {
                SkillChip(it)
            }
        }
    }
}

@Composable
fun ServicesSection() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Services Offered", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        repeat(2) {
            JHCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Android App Development", fontWeight = FontWeight.Bold)
                        Text("₹500 – ₹1500", color = JHSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Text("4.8 ★", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
