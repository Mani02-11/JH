package com.project.jh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHCard
import com.project.jh.ui.components.JHTextField
import com.project.jh.ui.components.StatusBadge
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyServicesScreen(
    onBack: () -> Unit,
    onNavigateToPostService: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var myServices by remember { mutableStateOf(listOf<ServiceData>()) }
    var isLoading by remember { mutableStateOf(true) }

    var editingService by remember { mutableStateOf<ServiceData?>(null) }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("services")
                .whereEqualTo("providerId", currentUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        myServices = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ServiceData::class.java)?.copy(id = doc.id, serviceId = doc.id)
                        }
                    }
                    isLoading = false
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("My Services", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPostService) {
                        Icon(Icons.Default.Add, contentDescription = "Add Service", tint = JHPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JHPrimary)
            }
        } else if (myServices.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You haven't posted any services yet.", color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))
                    JHButton(
                        text = "Offer Your Skill",
                        onClick = onNavigateToPostService
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(myServices) { service ->
                    MyServiceCard(
                        service = service,
                        onToggleActive = {
                            val newStatus = !service.isActive
                            db.collection("services").document(service.id)
                                .update("isActive", newStatus, "updatedAt", System.currentTimeMillis())
                                .addOnSuccessListener {
                                    Toast.makeText(context, if (newStatus) "Service enabled" else "Service disabled", Toast.LENGTH_SHORT).show()
                                }
                        },
                        onEdit = { editingService = service },
                        onDelete = {
                            db.collection("services").document(service.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Service deleted", Toast.LENGTH_SHORT).show()
                                }
                        }
                    )
                }
            }
        }

        if (editingService != null) {
            EditServiceDialog(
                service = editingService!!,
                onDismiss = { editingService = null },
                onSaved = { editingService = null }
            )
        }
    }
}

@Composable
fun MyServiceCard(
    service: ServiceData,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    JHCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = service.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = JHPrimary
                    )
                }
                StatusBadge(status = if (service.isActive) "Active" else "Disabled")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = service.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray.copy(alpha = 0.8f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Price: ₹${service.price}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Availability: ${service.availability}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleActive,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(if (service.isActive) "Disable" else "Enable")
                }

                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceDialog(
    service: ServiceData,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current

    var title by remember { mutableStateOf(service.title) }
    var category by remember { mutableStateOf(service.category) }
    var description by remember { mutableStateOf(service.description) }
    var experience by remember { mutableStateOf(service.experience) }
    var availability by remember { mutableStateOf(service.availability) }
    var price by remember { mutableStateOf(service.price) }
    var estimatedTime by remember { mutableStateOf(service.estimatedTime) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text("Edit Service", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                JHTextField(value = title, onValueChange = { title = it }, label = "Service Title")
                JHTextField(value = category, onValueChange = { category = it }, label = "Category")
                JHTextField(value = description, onValueChange = { description = it }, label = "Description", singleLine = false)
                JHTextField(value = experience, onValueChange = { experience = it }, label = "Experience")
                JHTextField(value = availability, onValueChange = { availability = it }, label = "Availability")
                JHTextField(value = price, onValueChange = { price = it }, label = "Price (₹)")
                JHTextField(value = estimatedTime, onValueChange = { estimatedTime = it }, label = "Estimated Time")
            }
        },
        confirmButton = {
            if (isSaving) {
                CircularProgressIndicator(color = JHPrimary)
            } else {
                JHButton(
                    text = "Save",
                    onClick = {
                        if (title.isBlank() || price.isBlank()) {
                            Toast.makeText(context, "Title and price are required", Toast.LENGTH_SHORT).show()
                            return@JHButton
                        }
                        isSaving = true
                        val updates = mapOf(
                            "title" to title,
                            "category" to category,
                            "description" to description,
                            "experience" to experience,
                            "availability" to availability,
                            "price" to price,
                            "estimatedTime" to estimatedTime,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        db.collection("services").document(service.id).update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Service updated", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                            .addOnFailureListener {
                                isSaving = false
                                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
