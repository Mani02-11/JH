package com.project.jh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    initialCategory: String? = null,
    onNavigateToService: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "All") }
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val db = remember { FirebaseFirestore.getInstance() }
    var services by remember { mutableStateOf(listOf<ServiceData>()) }

    LaunchedEffect(initialCategory) {
        if (!initialCategory.isNullOrBlank()) {
            selectedCategory = initialCategory
        }
    }

    LaunchedEffect(Unit) {
        db.collection("services")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    services = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ServiceData::class.java)?.copy(id = doc.id, serviceId = doc.id)
                        } catch (ex: Exception) {
                            ServiceData(
                                id = doc.id,
                                serviceId = doc.getString("serviceId") ?: doc.id,
                                providerId = doc.getString("providerId") ?: doc.getString("providerUid") ?: "",
                                providerName = doc.getString("providerName") ?: "Student",
                                title = doc.getString("title") ?: "",
                                category = doc.getString("category") ?: "",
                                description = doc.getString("description") ?: "",
                                experience = doc.getString("experience") ?: "",
                                availability = doc.getString("availability") ?: "Available",
                                price = doc.getString("price") ?: "",
                                estimatedTime = doc.getString("estimatedTime") ?: "",
                                rating = doc.getDouble("rating") ?: 0.0,
                                createdAt = doc.getLong("createdAt") ?: doc.getLong("timestamp") ?: 0L,
                                updatedAt = doc.getLong("updatedAt") ?: 0L,
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        }
                    }.filter { it.isActive }
                     .sortedByDescending { it.createdAt.coerceAtLeast(it.timestamp) }
                }
            }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (selectedCategory == "All") "Discover Skills" else selectedCategory,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = JHPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                placeholder = { Text("Search for a skill or service...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JHPrimary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JHPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            val filteredServices = services.filter { service ->
                val matchesCategory = selectedCategory == "All" || service.category.equals(selectedCategory, ignoreCase = true)
                val matchesQuery = searchQuery.isBlank() || 
                    service.title.contains(searchQuery, ignoreCase = true) || 
                    service.category.contains(searchQuery, ignoreCase = true) ||
                    service.description.contains(searchQuery, ignoreCase = true) ||
                    service.providerName.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }

            if (filteredServices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (services.isEmpty()) "No active services posted yet." else "No services match your search/filter.",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredServices) { service ->
                        PremiumServiceCard(service, onNavigateToService)
                    }
                }
            }
        }

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1A1A1A)
            ) {
                FilterBottomSheetContent(
                    currentCategory = selectedCategory,
                    onSelectCategory = { cat ->
                        selectedCategory = cat
                        showFilters = false
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumServiceCard(service: ServiceData, onNavigate: (String) -> Unit) {
    JHCard(onClick = { onNavigate(service.id) }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(initials = service.category.ifEmpty { "S" }.take(1), size = 50)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text("by ${service.providerName}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
                Text("${"%.1f".format(service.rating)} ★", fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (service.category.isNotBlank()) {
                    Text(text = service.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = JHPrimary)
                }
                Text("₹${service.price}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            
            if (service.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onNavigate(service.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("View Service")
                }
                JHButton(
                    text = "Request",
                    onClick = { onNavigate(service.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(currentCategory: String, onSelectCategory: (String) -> Unit) {
    val categories = listOf("All", "Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Filter by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        categories.forEach { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCategory(cat) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    colors = RadioButtonDefaults.colors(selectedColor = JHPrimary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = cat,
                    color = Color.White,
                    fontWeight = if (currentCategory == cat) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
