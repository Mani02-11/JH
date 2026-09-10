# JustHire ("Find Skills. Hire Instantly. Get Things Done.") - Project Completion Walkthrough

The JustHire Android application has been fully completed into an end-to-end peer-to-peer student skill hiring and real-time messaging application using **Cloud Firestore**, **Firebase Authentication**, and **Firebase Cloud Messaging (FCM)**, without using Firebase Storage.

---

## Key Feature Accomplishments

### 1. Login & Authentication (Preserved 100%)
- Existing Firebase Authentication (`AuthScreen.kt`) is fully preserved and untouched.
- Authenticated user's `uid` serves as the single source of truth across all operations.

### 2. Firestore User Profiles & Initials UI
- Profile data stored at `users/{uid}` with fields: `uid`, `name`, `email`, `college`, `course`, `year`, `bio`, `skills`, `availability`, `rating`, `reviewCount`, `completedServices`, `createdAt`, `updatedAt`, `lastActive`, `fcmToken`.
- No Firebase Storage dependency used—avatars are dynamically rendered using `ProfileAvatar` (colored initial avatars).
- Real-time `lastActive` tracking upon app usage.

### 3. Service Posting & Management ("Offer Your Skill" & "My Services")
- Users can post services with Title, Category, Description, Experience, Availability, Price, and Estimated Completion Time.
- Service provider ID is strictly set to `auth.currentUser.uid`.
- **My Services Screen (`MyServicesScreen.kt`)**: Enables providers to edit, toggle `isActive` (enable/disable), or delete their own posted services.

### 4. Real-time Service Discovery & Context-Aware Details
- **Home & Discover (`HomeScreen.kt`, `DiscoverScreen.kt`)**: Displays live active services and recommended students from Firestore, with live search across title, category, description, and skills.
- **Context-Aware Service Details (`ServiceDetailsScreen`)**:
  - **Other Users**: See service details and a prominent `[REQUEST SERVICE]` button.
  - **Service Owner**: Sees a "Your Service" badge along with management options (`[Edit]`, `[Delete]`).

### 5. Service Request & Real-time Acceptance Workflow
- Requester sends a request stored at `requests/{requestId}` with message and status `PENDING`.
- **Service Owner Requests Screen (`RequestsScreen.kt`)**:
  - Real-time tabs (`Received` and `Sent`).
  - Owner receives request instantly via Firestore real-time snapshot listeners.
  - Owner can click `[ACCEPT]` or `[REJECT]`.

### 6. Automatic Deterministic Chat Room Creation & Messaging
- **Chat Unlock**: Chat is **strictly unavailable** until the service owner accepts the request.
- **Deterministic Chat ID**: Calculated as `chatId = sorted(listOf(requesterId, providerId)).joinToString("_")` preventing duplicate rooms.
- **Real-Time Chat (`ChatScreens.kt`)**: Messages written to `chats/{chatId}/messages/{messageId}` and rendered instantly for both users using Firestore listeners (`whereArrayContains("participantIds", currentUid)`).

### 7. Service Completion & Ratings/Reviews
- Service owner marks request as `COMPLETED`.
- Requester receives option to rate the service (1–5 stars + written feedback).
- Stores review in `reviews/{reviewId}` and automatically recalculates average `rating` and `reviewCount` on the provider's profile.

### 8. Push Notifications (FCM)
- FCM integration via `JustHireMessagingService`. Updates `fcmToken` in `users/{uid}` and logs user notifications in `notifications/{notificationId}`.

### 9. Secure Firestore Security Rules (`firestore.rules`)
- Production-ready security rules prohibiting anonymous or unauthorized impersonation and restricting chat access to verified room participants.

---

## Files Created & Modified

### Created Files
- [JustHireMessagingService.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/fcm/JustHireMessagingService.kt) - Firebase Cloud Messaging service for push notifications and token syncing.
- [MyServicesScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/MyServicesScreen.kt) - Screen for managing services posted by the current user.
- [firestore.rules](file:///C:/Users/Mani/AndroidStudioProjects/JH/firestore.rules) - Security rules for Firestore.

### Modified Files
- [Data.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/Data.kt) - Aligned all data models with full Firestore schema requirements.
- [HomeScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/HomeScreen.kt) - Integrated personalized header, real-time Firestore popular services, and recommended students.
- [DiscoverScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/DiscoverScreen.kt) - Added live search across services and category filters.
- [ServiceScreens.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/ServiceScreens.kt) - Added context-aware Service Details, complete 7-field Offer Skill form, and request form.
- [RequestsScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/RequestsScreen.kt) - Real-time request tabs, Accept/Reject handling, automatic chat creation, and completion workflow.
- [ChatScreens.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/ChatScreens.kt) - Real-time chat list and message bubbles with auto-scroll and timestamp formatting.
- [ProfileScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/ProfileScreen.kt) - Displays full user profile stats, bio, availability, skills, and received reviews.
- [EditProfileScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/EditProfileScreen.kt) - Complete profile editing form for name, college, course, year, bio, skills, and availability.
- [ReviewScreens.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/screens/ReviewScreens.kt) - Rating dialog (1–5 stars + feedback) and automatic average rating recalculation.
- [MainScreen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/MainScreen.kt) & [Screen.kt](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/java/com/project/jh/ui/Screen.kt) - Navigation graph routing updates.
- [AndroidManifest.xml](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/src/main/AndroidManifest.xml) - Added notification permissions and FCM service registration.
- [app/build.gradle.kts](file:///C:/Users/Mani/AndroidStudioProjects/JH/app/build.gradle.kts) & [libs.versions.toml](file:///C:/Users/Mani/AndroidStudioProjects/JH/gradle/libs.versions.toml) - Added `firebase-messaging` dependency.

---

## Firestore Database Schema Summary

```
users/{uid}
├── name, email, college, course, year, bio, skills, availability
├── rating, reviewCount, completedServices, createdAt, updatedAt, lastActive, fcmToken

services/{serviceId}
├── serviceId, providerId, providerName, title, category, description
├── experience, availability, price, estimatedTime, rating, createdAt, updatedAt, isActive

requests/{requestId}
├── requestId, requesterId, requesterName, providerId, providerName
├── serviceId, serviceName, message, status (PENDING/ACCEPTED/REJECTED/COMPLETED/RATED), createdAt, updatedAt

chats/{chatId}
├── chatId (e.g. "uidA_uidB"), participantIds [uidA, uidB]
├── lastMessage, lastMessageSenderId, lastMessageTime, createdAt
└── messages/{messageId}
    └── messageId, senderId, receiverId, text, timestamp, seen, type

reviews/{reviewId}
├── reviewId, requestId, serviceId, reviewerId, revieweeId, rating, comment, createdAt

notifications/{notificationId}
└── notificationId, recipientUid, title, description, type, isRead, timestamp
```

---

## Two-Phone Testing Procedure (Verification)

To test the application on two physical Android phones:

### Step 1: Install Same APK on Both Phones
- Build the APK using `./gradlew assembleDebug` or Android Studio **Build > Build APK**.
- Install the exact same APK on **Phone A** and **Phone B**.

### Step 2: User A Setup ("Sam")
1. Open JustHire on **Phone A**.
2. Log in or Register as **Sam** (`sam@gmail.com`).
3. Click `+` or navigate to **Offer Skill**.
4. Input details:
   - **Title**: `Android App Development`
   - **Category**: `Programming`
   - **Description**: `I can help build Android applications using Kotlin & Firebase.`
   - **Price**: `1500`
5. Click **Publish Service**.

### Step 3: User B Discovery & Request ("Nayan")
1. Open JustHire on **Phone B**.
2. Log in or Register as **Nayan** (`nayan@gmail.com`).
3. Open **Discover** or **Home**.
4. See Sam's service `"Android App Development"`.
5. Tap on the service to open **Service Details**.
6. Notice **Phone B** sees the button `[REQUEST SERVICE]`.
7. Click `[REQUEST SERVICE]`, type message `"I need help with my Android project."`, and click **Send Request**.

### Step 4: Real-time Request Acceptance (Phone A)
1. On **Phone A (Sam)**, open the **Requests** tab.
2. Under **Received Requests**, Sam immediately sees Nayan's request for `"Android App Development"` without restarting the app.
3. Click `[ACCEPT]`.

### Step 5: Real-time Chat Execution
1. On **Phone B (Nayan)**, status changes to `ACCEPTED` in real time.
2. `[Open Chat]` button appears for both Sam and Nayan.
3. Both users tap `[Open Chat]`.
4. On **Phone A**, Sam sends: `"Hi Nayan, how can I help with your project?"`.
5. On **Phone B**, Nayan receives the message instantly in real-time.
6. On **Phone B**, Nayan replies: `"Hi Sam, I need help integrating Firebase."`.
7. On **Phone A**, Sam receives Nayan's reply instantly.

### Step 6: Completion & Review
1. On **Phone A**, Sam opens **Requests** and clicks `[Mark Completed]`.
2. On **Phone B**, Nayan sees `[Rate Service]`.
3. Nayan taps `[Rate Service]`, gives **5 Stars**, types `"Excellent help!"`, and submits.
4. On **Phone A**, Sam's profile rating updates to **5.0 ★**.
