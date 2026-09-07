package com.project.jh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    onNavigateToEdit: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    val targetUid = if (userId.isNullOrEmpty()) currentUid else userId
    val isSelf = targetUid == currentUid
    
    var userProfile by remember { mutableStateOf<UserMetadata?>(null) }
    var reviews by remember { mutableStateOf(listOf<ReviewData>()) }
    var userServicesCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetUid) {
        if (targetUid.isNotEmpty()) {
            if (isSelf) {
                db.collection("users").document(currentUid)
                    .set(mapOf("lastActive" to System.currentTimeMillis()), SetOptions.merge())
            }

            db.collection("users").document(targetUid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        try {
                            userProfile = doc.toObject(UserMetadata::class.java)?.copy(uid = doc.id)
                        } catch (ex: Exception) {
                            userProfile = UserMetadata(
                                uid = doc.id,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                college = doc.getString("college") ?: "",
                                course = doc.getString("course") ?: doc.getString("department") ?: "",
                                year = doc.getString("year") ?: "",
                                bio = doc.getString("bio") ?: doc.getString("about") ?: "",
                                department = doc.getString("department") ?: "",
                                phone = doc.getString("phone") ?: "",
                                skills = (doc.get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                availability = doc.getString("availability") ?: "Available",
                                rating = doc.getDouble("rating") ?: 0.0,
                                reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                                completedServices = doc.getLong("completedServices")?.toInt() ?: 0
                            )
                        }
                    }
                }

            db.collection("reviews")
                .whereEqualTo("revieweeId", targetUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        reviews = snapshot.documents.mapNotNull { d ->
                            try {
                                d.toObject(ReviewData::class.java)?.copy(id = d.id, reviewId = d.id)
                            } catch (ex: Exception) {
                                ReviewData(
                                    id = d.id,
                                    reviewId = d.getString("reviewId") ?: d.id,
                                    serviceId = d.getString("serviceId") ?: "",
                                    reviewerId = d.getString("reviewerId") ?: "",
                                    revieweeId = d.getString("revieweeId") ?: "",
                                    rating = d.getDouble("rating") ?: 0.0,
                                    comment = d.getString("comment") ?: "",
                                    createdAt = d.getLong("createdAt") ?: d.getLong("timestamp") ?: 0L
                                )
                            }
                        }.sortedByDescending { it.createdAt }
                    }
                }

            db.collection("services")
                .whereEqualTo("providerId", targetUid)
                .addSnapshotListener { snapshot, _ ->
                    userServicesCount = snapshot?.size() ?: 0
                }
        }
    }

    val resolvedName = userProfile?.name?.ifBlank { null } 
        ?: if (isSelf) auth.currentUser?.displayName?.ifBlank { null } ?: auth.currentUser?.email?.substringBefore("@") ?: "Student" 
        else "Student"

    val college = userProfile?.college ?: ""
    val course = userProfile?.course?.ifEmpty { userProfile?.department } ?: ""
    val year = userProfile?.year ?: ""
    val bio = userProfile?.bio?.ifEmpty { null } ?: "Passionate student developer."
    val rating = userProfile?.rating ?: 0.0
    val reviewCount = reviews.size
    val skills = userProfile?.skills ?: emptyList()
    val availability = userProfile?.availability ?: "Available"

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isSelf) "My Profile" else "Student Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                actions = {
                    if (isSelf) {
                        IconButton(onClick = onNavigateToEdit) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = JHPrimary)
                        }
                        IconButton(onClick = {
                            auth.signOut()
                            onLogout()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ProfileHeader(resolvedName, college, course, year, availability, rating, reviewCount)
            }
            
            item {
                StatsRow(rating, userServicesCount, reviewCount)
            }
            
            item {
                BioSection(bio)
            }
            
            item {
                SkillsSection(skills)
            }
            
            item {
                ReviewsSection(reviews)
            }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String, 
    college: String, 
    course: String, 
    year: String, 
    availability: String,
    rating: Double,
    reviewCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            ProfileAvatar(initials = name.ifEmpty { "S" }.take(1), size = 100)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        
        val subtitle = listOfNotNull(
            course.ifEmpty { null },
            year.ifEmpty { null },
            college.ifEmpty { null }
        ).joinToString(" • ")
        
        Text(
            text = subtitle.ifEmpty { "Student" },
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Availability: $availability",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFBC02D)
            )
            Text(" ${"%.1f".format(rating)}", fontWeight = FontWeight.Bold, color = Color.White)
            Text(" ($reviewCount reviews)", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
fun StatsRow(rating: Double, servicesCount: Int, reviewsCount: Int) {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Services", value = servicesCount.toString())
            StatItem(label = "Reviews", value = reviewsCount.toString())
            StatItem(label = "Rating", value = "%.1f".format(rating))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = JHPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun BioSection(bio: String) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun SkillsSection(skills: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (skills.isEmpty()) {
                Text("No skills added yet", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            } else {
                skills.forEach {
                    SkillChip(it)
                }
            }
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<ReviewData>) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        if (reviews.isEmpty()) {
            Text("No reviews received yet", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
        } else {
            reviews.forEach { review ->
                Surface(
                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rating: ${review.rating} ★", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFBC02D))
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
                        }
                    }
                }
            }
        }
    }
}
