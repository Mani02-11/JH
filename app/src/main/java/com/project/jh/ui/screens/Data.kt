package com.project.jh.ui.screens

import com.google.firebase.firestore.DocumentId

data class UserMetadata(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val college: String = "",
    val course: String = "",
    val year: String = "",
    val bio: String = "",
    val department: String = "",
    val phone: String = "",
    val skills: List<String> = emptyList(),
    val availability: String = "Available",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val completedServices: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val lastActive: Long = 0,
    val fcmToken: String = ""
)

data class ServiceData(
    @DocumentId val id: String = "",
    val serviceId: String = id,
    val providerId: String = "",
    val providerName: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val experience: String = "",
    val availability: String = "Available",
    val price: String = "",
    val estimatedTime: String = "",
    val rating: Double = 0.0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isActive: Boolean = true
) {
    val providerUid: String
        get() = providerId.ifEmpty { id }
    val timestamp: Long
        get() = createdAt
}

data class RequestData(
    @DocumentId val id: String = "",
    val requestId: String = id,
    val requesterId: String = "",
    val requesterName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val message: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED, RATED
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val requesterUid: String
        get() = requesterId
    val providerUid: String
        get() = providerId
    val serviceTitle: String
        get() = serviceName
    val timestamp: Long
        get() = createdAt
}

data class ChatData(
    @DocumentId val id: String = "",
    val chatId: String = id,
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTime: Long = 0,
    val createdAt: Long = 0
) {
    val participants: List<String>
        get() = participantIds
    val lastTimestamp: Long
        get() = lastMessageTime
}

data class MessageData(
    @DocumentId val id: String = "",
    val messageId: String = id,
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val type: String = "text"
) {
    val senderUid: String
        get() = senderId
}

data class ReviewData(
    @DocumentId val id: String = "",
    val reviewId: String = id,
    val serviceId: String = "",
    val reviewerId: String = "",
    val revieweeId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Long = 0
)

data class NotificationData(
    @DocumentId val id: String = "",
    val notificationId: String = id,
    val recipientUid: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "info", // "request", "acceptance", "message", "completed", "review"
    val isRead: Boolean = false,
    val timestamp: Long = 0
)
