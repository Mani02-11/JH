package com.project.jh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHTextField
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser
    val context = LocalContext.current
    
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var college by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name")?.ifBlank { name } ?: name
                        college = document.getString("college") ?: ""
                        course = document.getString("course") ?: document.getString("department") ?: ""
                        year = document.getString("year") ?: ""
                        bio = document.getString("bio") ?: document.getString("about") ?: ""
                        availability = document.getString("availability") ?: "Available"
                        val skillsList = document.get("skills") as? List<*>
                        skills = skillsList?.joinToString(", ") ?: ""
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            JHTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person
            )

            JHTextField(
                value = college,
                onValueChange = { college = it },
                label = "College / University",
                leadingIcon = Icons.Default.School
            )

            JHTextField(
                value = course,
                onValueChange = { course = it },
                label = "Course / Branch",
                leadingIcon = Icons.Default.Book
            )

            JHTextField(
                value = year,
                onValueChange = { year = it },
                label = "Year of Study (e.g., 3rd Year)",
                leadingIcon = Icons.Default.DateRange
            )

            JHTextField(
                value = availability,
                onValueChange = { availability = it },
                label = "Availability (e.g. Weekends, Evenings)",
                leadingIcon = Icons.Default.Schedule
            )

            JHTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio / About Me",
                singleLine = false
            )

            JHTextField(
                value = skills,
                onValueChange = { skills = it },
                label = "Skills (comma separated, e.g. Kotlin, Figma, Python)"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(color = JHPrimary)
            } else {
                JHButton(
                    text = "Save Changes",
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }
                        
                        val uid = user?.uid ?: ""
                        if (uid.isEmpty()) {
                            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }

                        isLoading = true
                        val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val now = System.currentTimeMillis()

                        val userData = mapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to (user?.email ?: ""),
                            "college" to college,
                            "course" to course,
                            "department" to course,
                            "year" to year,
                            "bio" to bio,
                            "about" to bio,
                            "skills" to skillsList,
                            "availability" to availability,
                            "updatedAt" to now,
                            "lastActive" to now
                        )

                        // 1. Write to Firestore local cache immediately
                        db.collection("users").document(uid).set(userData, SetOptions.merge())

                        // 2. Update Firebase Auth Profile
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user?.updateProfile(profileUpdates)

                        // 3. Complete immediately without blocking UI on network ACK
                        isLoading = false
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
