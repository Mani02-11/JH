package com.project.jh.fcm

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import com.project.jh.R
import com.project.jh.ui.screens.NotificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FcmNotificationHelper {

    private const val TAG = "FcmNotificationHelper"

    fun sendNotificationToUser(
        context: Context,
        recipientUid: String,
        title: String,
        description: String,
        type: String,
        senderUid: String = "",
        extraData: Map<String, String> = emptyMap(),
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (recipientUid.isBlank()) {
            Log.w(TAG, "Recipient UID is blank, skipping notification dispatch.")
            onComplete?.invoke(false)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val timestamp = System.currentTimeMillis()

        // 1. Create document in Firestore notifications collection (In-app notification tab)
        val notifId = db.collection("notifications").document().id
        val notificationData = NotificationData(
            id = notifId,
            notificationId = notifId,
            recipientUid = recipientUid,
            title = title,
            description = description,
            type = type,
            isRead = false,
            timestamp = timestamp
        )

        db.collection("notifications").document(notifId).set(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification document saved to Firestore for recipient: $recipientUid")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save notification document", e)
            }

        // 2. Fetch Recipient's FCM token from Firestore users collection
        db.collection("users").document(recipientUid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc != null && userDoc.exists()) {
                    val fcmToken = userDoc.getString("fcmToken") ?: userDoc.getString("token") ?: ""
                    if (fcmToken.isNotBlank()) {
                        Log.d(TAG, "Recipient $recipientUid has valid FCM Token: $fcmToken")
                        
                        // Launch a coroutine to do network request off the main thread
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val success = dispatchFcmMessageDirectly(
                                    context = context,
                                    deviceToken = fcmToken,
                                    title = title,
                                    body = description,
                                    type = type,
                                    senderUid = senderUid,
                                    notificationId = notifId,
                                    extraData = extraData
                                )
                                if (success) {
                                    Log.d(TAG, "Successfully dispatched direct FCM to $recipientUid")
                                } else {
                                    Log.w(TAG, "Failed to dispatch direct FCM to $recipientUid")
                                }
                                onComplete?.invoke(success)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in FCM dispatch coroutine", e)
                                onComplete?.invoke(false)
                            }
                        }
                    } else {
                        Log.w(TAG, "User $recipientUid does not have an FCM token saved.")
                        onComplete?.invoke(false)
                    }
                } else {
                    Log.w(TAG, "User document for $recipientUid does not exist.")
                    onComplete?.invoke(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch recipient user doc for $recipientUid", e)
                onComplete?.invoke(false)
            }
    }

    private fun dispatchFcmMessageDirectly(
        context: Context,
        deviceToken: String,
        title: String,
        body: String,
        type: String,
        senderUid: String,
        notificationId: String,
        extraData: Map<String, String>
    ): Boolean {
        return try {
            // Read service account from raw resources
            val inputStream = context.resources.openRawResource(R.raw.service_account)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val serviceAccountJson = JSONObject(jsonString)
            val projectId = serviceAccountJson.getString("project_id")
            
            // Get credentials token
            val credentialsStream = jsonString.byteInputStream()
            val credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            val accessToken = credentials.accessToken.tokenValue

            val fcmUrl = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
            
            // Construct Payload
            val payload = JSONObject()
            val message = JSONObject()
            message.put("token", deviceToken)
            
            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", body)
            message.put("notification", notification)
            
            val data = JSONObject()
            data.put("type", type)
            data.put("senderUid", senderUid)
            data.put("notificationId", notificationId)
            for ((key, value) in extraData) {
                data.put(key, value)
            }
            message.put("data", data)
            
            payload.put("message", message)

            // Make HTTP POST
            val url = URL(fcmUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Content-Type", "application/json; UTF-8")
            connection.doOutput = true

            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                Log.d(TAG, "FCM Push Success: $responseCode")
                true
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.readText() ?: ""
                Log.e(TAG, "FCM Push Failed with code $responseCode: $errorMsg")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during FCM Push dispatch", e)
            false
        }
    }
}
