package com.project.jh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.jh.R
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToDiscoverCategory: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPostService: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HomeTopBar(onNavigateToNotifications)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPostService,
                containerColor = JHPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp) // Push FAB higher
            ) {
                Icon(Icons.Default.Add, contentDescription = "Offer Skill")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeHeader()
            }

            item {
                HomeSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            item {
                FeaturedServicesSection(searchQuery, onNavigateToService)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onNavigate: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("notifications")
                .whereEqualTo("recipientUid", currentUid)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, _ ->
                    unreadCount = snapshot?.size() ?: 0
                }
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "JustHire",
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.justhire_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(36.dp) // Increased for 2% zoom effect
                    .clip(CircleShape)
            )
        },
        actions = {
            IconButton(onClick = onNavigate) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = JHPrimary,
                                contentColor = Color.White
                            ) {
                                Text(unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Notifications, 
                        contentDescription = "Notifications", 
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun WelcomeHeader() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser
    val currentUid = user?.uid ?: ""
    var userName by remember { mutableStateOf(user?.displayName?.ifBlank { null } ?: user?.email?.substringBefore("@") ?: "Student") }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("users").document(currentUid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        userName = doc.getString("name")?.ifBlank { userName } ?: userName
                    }
                }
        }
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileAvatar(initials = userName.ifEmpty { "S" }.take(1), size = 50)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Hi, $userName 👋",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "What would you like to get done today?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search skills, services or people...", color = Color.White.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JHPrimary) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JHPrimary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.12f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun CategorySection(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        "Programming" to "💻", "Design" to "🎨", "Tutoring" to "📚",
        "Photography" to "📷", "Video Editing" to "🎬", "Web Dev" to "🌐", "Other" to "✨"
    )

    Column {
        Text(
            text = "Quick Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (name, icon) ->
                SuggestionChip(
                    onClick = { onCategoryClick(name) },
                    label = { 
                        Text(
                            text = "$icon $name",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun FeaturedServicesSection(searchQuery: String, onNavigate: (String) -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var services by remember { mutableStateOf(listOf<ServiceData>()) }

    LaunchedEffect(Unit) {
        db.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
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
                }
            }
    }

    val filtered = if (searchQuery.isBlank()) services else {
        services.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column {
        Text(
            text = "Popular Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            Text("No services available yet.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 12.dp))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(filtered) { service ->
                    ServiceCard(
                    service = service,
                    rating = "%.1f".format(service.rating),
                    price = service.price,
                    onClick = { onNavigate(service.id) }
                )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceData, rating: String, price: String, onClick: () -> Unit) {
    JHCard(
        modifier = Modifier.width(260.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(JHPrimary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = service.category.ifEmpty { "Service" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = service.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "by ${service.providerName}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFBC02D))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = rating,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            // Spacer(modifier = Modifier.height(8.dp))
            Text(text = service.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = JHPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "₹$price", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        }
    }
}
