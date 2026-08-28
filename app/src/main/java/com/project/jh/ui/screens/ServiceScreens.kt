package com.project.jh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(onBack: () -> Unit, onRequest: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Starting from", style = MaterialTheme.typography.labelMedium)
                        Text("₹500", style = MaterialTheme.typography.titleLarge, color = JHPrimary, fontWeight = FontWeight.Bold)
                    }
                    JHButton(text = "Request Service", onClick = onRequest, modifier = Modifier.width(200.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JHPrimary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Service Preview Image", color = JHPrimary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Android App Development", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(initials = "AK", size = 32)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Arun Kumar", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFBC02D))
                Text(" 4.8", fontWeight = FontWeight.Bold)
            }
            
            Divider(modifier = Modifier.padding(vertical = 24.dp))
            
            Text("Description", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "I will build a high-quality Android application for your college project or startup using Kotlin and Jetpack Compose. Includes Firebase integration and UI/UX design.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Experience", style = MaterialTheme.typography.titleMedium)
            Text("2+ years in Android Development", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferSkillForm(onBack: () -> Unit) {
    var serviceName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer Your Skills") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Showcase what you can do and help other students.", color = Color.Gray)
            
            OutlinedTextField(value = serviceName, onValueChange = { serviceName = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = "", onValueChange = { }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(150.dp))
            OutlinedTextField(value = "", onValueChange = { }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            JHButton(text = "Publish Service", onClick = { }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceForm(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Requesting Service from Rahul Sharma", fontWeight = FontWeight.SemiBold)
            
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Tell them what you need...") },
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            
            OutlinedTextField(value = "", onValueChange = { }, label = { Text("Deadline (optional)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.weight(1f))
            JHButton(text = "Send Request", onClick = { }, modifier = Modifier.fillMaxWidth())
        }
    }
}
