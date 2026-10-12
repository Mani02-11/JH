package com.project.jh.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.jh.MainActivity
import com.project.jh.R

class JustHireMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
        updateFcmTokenInFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        val data = remoteMessage.data
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: "JustHire Notification"
        
        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["description"]
            ?: "You have a new update."

        val type = data["type"] ?: "info"
        val senderId = data["senderId"] ?: ""

        showNotification(title, body, type, senderId)
    }

    private fun showNotification(title: String, body: String, type: String = "info", senderId: String = "") {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JustHire Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for service requests and messages"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
            putExtra("sender_id", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.justhire_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "justhire_notifications"
        private const val TAG = "JustHireFCM"

        fun updateFcmTokenInFirestore(token: String) {
            if (token.isBlank()) return
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            
            val tokenData = mapOf(
                "fcmToken" to token,
                "token" to token,
                "lastActive" to System.currentTimeMillis()
            )
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(tokenData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token updated successfully for user $uid")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update FCM token for user $uid", e)
                }
        }
    }
}
