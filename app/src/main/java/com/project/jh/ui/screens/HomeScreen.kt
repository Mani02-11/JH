package com.project.jh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.SkillChip
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToService: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(onNavigateToNotifications)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                WelcomeHeader()
            }

            item {
                HomeSearchBar()
            }

            item {
                CategorySection()
            }

            item {
                FeaturedServicesSection(onNavigateToService)
            }

            item {
                RecommendedStudentsSection(onNavigateToUser)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onNavigate: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("JustHire", fontWeight = FontWeight.ExtraBold, color = JHPrimary)
        },
        actions = {
            IconButton(onClick = onNavigate) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun WelcomeHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileAvatar(initials = "JD", size = 50)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Hi, John 👋",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "What would you like to get done today?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HomeSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search skills, services or people...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = JHPrimary,
            unfocusedIndicatorColor = Color.LightGray
        ),
        singleLine = true
    )
}

@Composable
fun CategorySection() {
    val categories = listOf(
        "Programming" to "💻", "Design" to "🎨", "Tutoring" to "📚",
        "Photography" to "📷", "Video Editing" to "🎬", "Web Dev" to "🌐"
    )

    Column {
        Text("Quick Categories", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (name, icon) ->
                SuggestionChip(
                    onClick = { },
                    label = { Text("$icon $name") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedServicesSection(onNavigate: (String) -> Unit) {
    Column {
        Text("Popular Services", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) { index ->
                ServiceCard(
                    title = "Android App Development",
                    provider = "Arun Kumar",
                    rating = "4.8",
                    jobs = "12",
                    onClick = { onNavigate("service_$index") }
                )
            }
        }
    }
}

@Composable
fun RecommendedStudentsSection(onNavigate: (String) -> Unit) {
    Column {
        Text("Students You May Need", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        repeat(3) { index ->
            StudentCard(
                name = "Rahul Sharma",
                course = "Computer Science • 3rd Year",
                skill = "Full Stack Developer",
                rating = "4.9",
                onClick = { onNavigate("user_$index") }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ServiceCard(title: String, provider: String, rating: String, jobs: String, onClick: () -> Unit) {
    JHCard(
        modifier = Modifier.width(260.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JHPrimary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = JHPrimary, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = provider, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFFBC02D)
                )
                Text(text = rating, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$jobs services completed", style = MaterialTheme.typography.labelSmall, color = JHSecondary)
        }
    }
}

@Composable
fun StudentCard(name: String, course: String, skill: String, rating: String, onClick: () -> Unit) {
    JHCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(initials = name.take(1), size = 60)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text(course, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                SkillChip(skill)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFBC02D)
                    )
                    Text(rating, fontWeight = FontWeight.Bold)
                }
                Text("Available", color = JHSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
