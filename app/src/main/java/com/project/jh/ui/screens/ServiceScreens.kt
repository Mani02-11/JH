package com.project.jh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHTextField
import com.project.jh.ui.components.ProfileAvatar
import com.project.jh.ui.components.StatusBadge
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    serviceId: String? = null,
    onBack: () -> Unit,
    onRequest: (ServiceData) -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    var service by remember { mutableStateOf<ServiceData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(serviceId) {
        if (serviceId != null) {
            db.collection("services").document(serviceId)
                .addSnapshotListener { doc, error ->
                    if (error != null) return@addSnapshotListener
                    if (doc != null && doc.exists()) {
                        service = doc.toObject(ServiceData::class.java)?.copy(id = doc.id, serviceId = doc.id)
                    } else if (doc != null && !doc.exists()) {
                        service = null
                        if (!isLoading) {
                            Toast.makeText(context, "Service is no longer available", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                    isLoading = false
                }
        }
    }

    val isOwner = service != null && (service?.providerId == currentUid || service?.providerUid == currentUid)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {
                            service?.id?.let { id ->
                                db.collection("services").document(id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Service deleted", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (service != null) {
                Surface(tonalElevation = 8.dp, color = Color.Black.copy(alpha = 0.3f)) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Price", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                            Text("₹${service?.price}", style = MaterialTheme.typography.titleLarge, color = JHPrimary, fontWeight = FontWeight.Bold)
                        }

                        if (isOwner) {
                            Surface(
                                color = JHPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, JHPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Your Service",
                                    color = JHPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        } else {
                            JHButton(
                                text = "Request Service",
                                onClick = { onRequest(service!!) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JHPrimary)
            }
        } else if (service != null) {
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
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(JHPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = JHPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusBadge(status = if (service!!.isActive) "Active" else "Disabled")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = service?.title ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(initials = service?.providerName?.take(1) ?: "S", size = 36)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(service?.providerName ?: "Student", fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Category: ${service?.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFBC02D))
                    Text(" ${service?.rating}", fontWeight = FontWeight.Bold, color = Color.White)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.White.copy(alpha = 0.1f))

                Text("Description", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    service?.description ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )

                if (!service?.availability.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Availability", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(service?.availability ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }

                if (!service?.estimatedTime.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Estimated Completion Time", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(service?.estimatedTime ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferSkillForm(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Offer Your Skills", color = Color.White, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Showcase your skills and earn by helping other students.", color = Color.White.copy(alpha = 0.8f))

            JHTextField(value = title, onValueChange = { title = it }, label = "Service Title (e.g. Android Development)")
            
            // Category selector scrollable chips
            Text("Select Category", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            val categories = listOf("Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other")
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JHPrimary,
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = category == cat,
                            borderColor = Color.White.copy(alpha = 0.2f),
                            selectedBorderColor = JHPrimary
                        )
                    )
                }
            }

            JHTextField(value = description, onValueChange = { description = it }, label = "Description", singleLine = false)
            JHTextField(value = availability, onValueChange = { availability = it }, label = "Availability (e.g. Weekends / Evenings)")
            JHTextField(value = price, onValueChange = { price = it }, label = "Price (₹)")
            JHTextField(value = estimatedTime, onValueChange = { estimatedTime = it }, label = "Estimated Completion Time (e.g. 2 Days)")

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = JHPrimary)
            } else {
                JHButton(
                    text = "Publish Service",
                    onClick = {
                        if (title.isBlank() || price.isBlank()) {
                            Toast.makeText(context, "Please fill required title and price", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }

                        val uid = user?.uid ?: ""
                        if (uid.isEmpty()) {
                            Toast.makeText(context, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }

                        isLoading = true
                        val now = System.currentTimeMillis()
                        val providerName = user?.displayName?.ifBlank { null } 
                            ?: user?.email?.substringBefore("@") 
                            ?: "Student Provider"

                        val docRef = db.collection("services").document()
                        val serviceId = docRef.id

                        val serviceData = hashMapOf(
                            "serviceId" to serviceId,
                            "providerId" to uid,
                            "providerName" to providerName,
                            "title" to title,
                            "category" to category,
                            "description" to description,
                            "experience" to experience,
                            "availability" to availability,
                            "price" to price,
                            "estimatedTime" to estimatedTime,
                            "rating" to 0.0,
                            "createdAt" to now,
                            "timestamp" to now,
                            "updatedAt" to now,
                            "isActive" to true
                        )

                        // 1. Write to Firestore local cache immediately
                        docRef.set(serviceData)

                        // 2. Complete UI immediately without blocking on network server ACK
                        isLoading = false
                        Toast.makeText(context, "Service Published Successfully!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceForm(
    serviceId: String,
    providerUid: String,
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser
    val context = LocalContext.current

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Hi, I am interested in hiring you for this service.") }
    var isSubmitting by remember { mutableStateOf(false) }
    var serviceTitle by remember { mutableStateOf("") }
    var providerName by remember { mutableStateOf("") }

    LaunchedEffect(serviceId) {
        db.collection("services").document(serviceId).get()
            .addOnSuccessListener { doc ->
                serviceTitle = doc.getString("title") ?: ""
                providerName = doc.getString("providerName") ?: ""
            }
        
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        name = doc.getString("name") ?: name
                        email = doc.getString("email") ?: email
                        phone = doc.getString("phone") ?: ""
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Request Service", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hire for: $serviceTitle",
                style = MaterialTheme.typography.titleMedium,
                color = JHPrimary,
                fontWeight = FontWeight.Bold
            )

            JHTextField(value = name, onValueChange = { name = it }, label = "Full Name", leadingIcon = Icons.Default.Person)
            JHTextField(value = email, onValueChange = { email = it }, label = "Email Address", leadingIcon = Icons.Default.Email)
            JHTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", leadingIcon = Icons.Default.Phone)
            JHTextField(value = message, onValueChange = { message = it }, label = "Tell ${providerName.ifEmpty { "the provider" }} what you need", singleLine = false)

            Spacer(modifier = Modifier.height(24.dp))

            if (isSubmitting) {
                CircularProgressIndicator(color = JHPrimary)
            } else {
                JHButton(
                    text = "Send Request",
                    onClick = {
                        if (name.isBlank() || email.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }

                        isSubmitting = true
                        val docRef = db.collection("requests").document()
                        val requestId = docRef.id
                        val now = System.currentTimeMillis()

                        val requestData = RequestData(
                            id = requestId,
                            requestId = requestId,
                            requesterId = user?.uid ?: "",
                            requesterName = name,
                            providerId = providerUid,
                            providerName = providerName,
                            serviceId = serviceId,
                            serviceName = serviceTitle,
                            message = message,
                            status = "PENDING",
                            createdAt = now,
                            updatedAt = now
                        )

                        // Write request & notification to Firestore local cache immediately
                        docRef.set(requestData)

                        val notificationId = db.collection("notifications").document().id
                        val notification = NotificationData(
                            id = notificationId,
                            notificationId = notificationId,
                            recipientUid = providerUid,
                            title = "New Service Request",
                            description = "$name wants to hire you for '$serviceTitle'.",
                            type = "request",
                            timestamp = now
                        )
                        db.collection("notifications").document(notificationId).set(notification)

                        // Complete UI immediately
                        isSubmitting = false
                        Toast.makeText(context, "Service Request Sent Successfully!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
