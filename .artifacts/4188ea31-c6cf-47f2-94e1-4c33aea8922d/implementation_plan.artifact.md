# Implementation Plan - Profile Data Overwrite Issue

## Problem
The user's Firestore `users/{uid}` document contains only `fcmToken` and `lastActive`. Crucial profile fields (`name`, `email`, etc.) are missing. The user suspects that code writing *only* these two fields is overwriting the entire document instead of merging.

## Investigation Results
I searched the codebase for writes to the `users` collection.
1.  **`AuthScreen.kt`:** Uses `.set(userData)` during registration. This creates the initial profile and is correct.
2.  **`EditProfileScreen.kt`:** Uses `.set(userData, SetOptions.merge())`. This merges data and is safe.
3.  **`ProfileScreen.kt`:** Uses `.set(mapOf("lastActive" to System.currentTimeMillis()), SetOptions.merge())`. This merges and is safe.
4.  **`fcm/JustHireMessagingService.kt`:**
    ```kotlin
    fun updateFcmTokenInFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(
                mapOf(
                    "fcmToken" to token,
                    "lastActive" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }
    ```
    This function was previously updated (in a prior task) to use `SetOptions.merge()`.

## Root Cause
The code that *was* causing the overwrite was in `JustHireMessagingService.kt`. Previously, it was using `.set(...)` or `.update(...)` incorrectly, which caused the document to be replaced entirely or, in some cases, created a new document with only those two fields if the document didn't exist when the FCM service started.

**I have already fixed this exact issue in a previous turn.** The code currently in the repository uses `SetOptions.merge()` in all places where partial updates are performed on the `users` collection (`JustHireMessagingService` and `ProfileScreen`).

The reason the user is *still* seeing only `fcmToken` and `lastActive` in their Firestore database is because **the data was already overwritten by the old, buggy code before the fix was applied.** The database currently holds the stubs created by the old code.

## Verification of Data Retrieval in Chat
The user asked to verify that `ChatListScreen` and `ChatDetailScreen` retrieve `users/{otherUid}.name`.
*   **`ChatScreens.kt`:** Both screens correctly read the name using the document snapshot listener:
    ```kotlin
    db.collection("users").document(userId) // or otherUid
        .addSnapshotListener { doc, _ ->
            // ...
            val fetchedName = doc.getString("name")?.ifBlank { null }
            // ...
        }
    ```
    This confirms the UI relies correctly on `users/{uid}.name`.

## Action Plan
Since the codebase has already been corrected to use `SetOptions.merge()` for these operations, no further code changes are necessary to prevent *future* overwrites.

I need to explain to the user:
1.  The root cause was the old version of the FCM token update code.
2.  The code has already been fixed.
3.  How to restore the missing data for existing users (by editing their profile in the app).