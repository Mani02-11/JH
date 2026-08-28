package com.project.jh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavigateToUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Skills", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search for a skill or service...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(talents) { talent ->
                    PremiumStudentCard(talent, onNavigateToUser)
                }
            }
        }

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState
            ) {
                FilterBottomSheetContent(onApply = { showFilters = false })
            }
        }
    }
}

@Composable
fun PremiumStudentCard(talent: Talent, onNavigate: (String) -> Unit) {
    JHCard(onClick = { onNavigate("user_${talent.name}") }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(initials = talent.name.take(1), size = 60)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(talent.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Computer Science • 2nd Year", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(talent.rating, fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillChip("Kotlin")
                SkillChip("Firebase")
                SkillChip("Android")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Experienced Android developer with a focus on clean architecture and UI/UX.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onNavigate("user_${talent.name}") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Profile")
                }
                JHButton(
                    text = "Hire",
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(onApply: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Category", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        // Simplified category chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("Development") })
            FilterChip(selected = false, onClick = {}, label = { Text("Design") })
            FilterChip(selected = false, onClick = {}, label = { Text("Writing") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Availability", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("Available Now") })
            FilterChip(selected = false, onClick = {}, label = { Text("This Week") })
        }

        Spacer(modifier = Modifier.height(32.dp))
        JHButton(text = "Apply Filters", onClick = onApply, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
    }
}
