package com.project.jh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import com.project.jh.ui.components.JHButton
import com.project.jh.ui.components.JHTextField
import com.project.jh.ui.theme.JHPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    requestId: String,
    providerUid: String,
    serviceTitle: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    val context = LocalContext.current
    
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text("Rate Service", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "How was your experience with '$serviceTitle'?",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        val currentStar = index + 1
                        IconButton(onClick = { rating = currentStar }) {
                            Icon(
                                imageVector = if (currentStar <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = if (currentStar <= rating) Color(0xFFFBC02D) else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                
                JHTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "Your feedback (Optional)",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            if (isSubmitting) {
                CircularProgressIndicator(color = JHPrimary)
            } else {
                JHButton(
                    text = "Submit Review",
                    onClick = {
                        isSubmitting = true
                        val docRef = db.collection("reviews").document()
                        val reviewId = docRef.id
                        val now = System.currentTimeMillis()

                        val reviewData = hashMapOf(
                            "reviewId" to reviewId,
                            "requestId" to requestId,
                            "reviewerId" to currentUid,
                            "revieweeId" to providerUid,
                            "rating" to rating.toDouble(),
                            "comment" to comment,
                            "createdAt" to now
                        )
                        
                        docRef.set(reviewData)
                            .addOnSuccessListener {
                                // Mark request as RATED
                                db.collection("requests").document(requestId).update(
                                    "status", "RATED",
                                    "updatedAt", now
                                )
                                
                                // Send notification to provider
                                val notifId = db.collection("notifications").document().id
                                val notif = NotificationData(
                                    id = notifId,
                                    notificationId = notifId,
                                    recipientUid = providerUid,
                                    title = "New Review Received! ⭐",
                                    description = "You received a $rating-star rating for '$serviceTitle'.",
                                    type = "review",
                                    timestamp = now
                                )
                                db.collection("notifications").document(notifId).set(notif)

                                // Recalculate average rating & review count for provider
                                recalculateProviderRating(db, providerUid)
                                
                                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                            .addOnFailureListener { err ->
                                isSubmitting = false
                                Toast.makeText(context, "Failed to submit review: ${err.message}", Toast.LENGTH_SHORT).show()
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

private fun recalculateProviderRating(db: FirebaseFirestore, providerUid: String) {
    if (providerUid.isEmpty()) return
    db.collection("reviews")
        .whereEqualTo("revieweeId", providerUid)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                val ratings = snapshot.documents.mapNotNull { it.getDouble("rating") }
                if (ratings.isNotEmpty()) {
                    val average = ratings.average()
                    val count = ratings.size
                    db.collection("users").document(providerUid).update(
                        "rating", average,
                        "reviewCount", count,
                        "completedServices", count
                    )
                }
            }
        }
}
