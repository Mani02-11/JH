# Implementation Plan - JustHire ("Find Skills. Hire Instantly. Get Things Done.")

Completing the JustHire Android application into a fully functional end-to-end peer-to-peer service hiring and real-time chat platform using Cloud Firestore, Firebase Authentication, and Firebase Cloud Messaging (FCM), without using Firebase Storage.

---

## Initial Project Inspection Analysis

### A. What is Already Working?
1. **Firebase Authentication (`AuthScreen.kt`):**
   - Working Sign-In and Registration flows with Email/Password.
   - Initial user document creation in Cloud Firestore (`users/{uid}`) with basic fields (`uid`, `name`, `email`, `department`, `phone`, `rating`).
   - Profile update call setting `displayName`.
2. **Navigation Shell (`MainScreen.kt`, `Screen.kt`):**
   - Glassmorphism bottom navigation bar for `Home`, `Discover`, `Requests`, `Chat`, `Profile`.
   - Navigation graph routes for detail screens (`service_detail/{serviceId}`, `user_detail/{userId}`, `chat_detail/{userId}`, `request_form/{serviceId}/{providerUid}`, `post_skill`, `edit_profile`, `notifications`).
3. **No Firebase Storage Dependency:**
   - Avatar styling uses `ProfileAvatar` (initials with dynamic background colors) instead of image uploads.
4. **Basic UI Screen Layouts:**
   - Existing Compose screens with JustHire design system (Space Dark glassmorphism overlay, purple accents, white rounded cards).

---

### B. What Files Should Remain Unchanged?
- `AuthScreen.kt`: The login UI, input fields, Firebase Auth logic, and registration form remain 100% intact as requested.
- `google-services.json`: Firebase configuration.
- `build.gradle.kts` (Project level) & `settings.gradle.kts`.

---

### C. What Files Need Modification?
- `gradle/libs.versions.toml`: Add FCM dependency definition `firebase-messaging`.
- `app/build.gradle.kts`: Add FCM dependency `implementation(libs.firebase.messaging)`.
- `AndroidManifest.xml`: Declare `JustHireMessagingService` and POST_NOTIFICATIONS permission.
- `com/project/jh/ui/screens/Data.kt`: Update and enrich data models (`UserMetadata`, `ServiceData`, `RequestData`, `ChatData`, `MessageData`, `ReviewData`, `NotificationData`) with required fields.
- `com/project/jh/ui/screens/HomeScreen.kt`: Connect to real Firestore data, display personalized user greeting, popular services, recommended students, and search bar.
- `com/project/jh/ui/screens/DiscoverScreen.kt`: Live service search by title, category, provider name, and skills.
- `com/project/jh/ui/screens/ServiceScreens.kt`:
  - `OfferSkillForm`: Complete fields (Title, Category, Description, Experience, Availability, Price, Estimated Time), auto-assign `providerId = auth.currentUser.uid`.
  - `ServiceDetailsScreen`: Context-aware UI. If current user is owner, show "Your Service", "Edit", "Manage Requests", and delete option; if not owner, show "Request Service".
  - `RequestServiceForm`: Send request saving `requesterId = auth.currentUser.uid` and target `providerId`.
- `com/project/jh/ui/screens/RequestsScreen.kt`: Real-time tabs for Received & Sent requests. Owner Accept/Reject controls. When accepted, automatically create deterministic chat `chats/{chatId}` and send notification.
- `com/project/jh/ui/screens/ChatScreens.kt`:
  - `ChatListScreen`: Show only chats where user is in `participantIds`.
  - `ChatDetailScreen`: Real-time chat using `chats/{chatId}/messages` subcollection ordered by timestamp. Send text messages and update lastMessage metadata.
- `com/project/jh/ui/screens/ProfileScreen.kt` & `EditProfileScreen.kt`: Manage profile fields (`college`, `course`, `year`, `bio`, `skills`, `availability`, `rating`, `reviewCount`, `completedServices`).
- `com/project/jh/ui/screens/ReviewScreens.kt`: Submit service rating (1-5 stars) and comment; recalculate provider rating.
- `com/project/jh/ui/MainScreen.kt`: Add route for `MyServicesScreen` and ensure seamless navigation flow.

---

### D. What New Files Will Be Created?
- `com/project/jh/fcm/JustHireMessagingService.kt`: Handles incoming FCM push notifications and updates tokens in Firestore.
- `com/project/jh/ui/screens/MyServicesScreen.kt`: Allows providers to view, edit, enable/disable, and delete services they posted.
- `firestore.rules`: Secure Firestore security rules enforcing ownership and participant-only read/write access.

---

### E. What Firebase Collections Will Be Used?
1. `users/{uid}`: Profile details, skills, availability, ratings, FCM token.
2. `services/{serviceId}`: Posted service offerings.
3. `requests/{requestId}`: Service hire requests (`PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `RATED`).
4. `chats/{chatId}`: Active chat rooms (`chatId = sorted(userA, userB).joinToString("_")`).
5. `chats/{chatId}/messages/{messageId}`: Real-time message subcollection.
6. `reviews/{reviewId}`: Service ratings and reviews.
7. `notifications/{notificationId}`: User notification log.

---

## Proposed Changes & Phased Implementation

### Phase 1: Dependencies & FCM Setup
- Add `firebase-messaging` to `libs.versions.toml` and `app/build.gradle.kts`.
- Add `JustHireMessagingService` and notification permissions in `AndroidManifest.xml`.

### Phase 2: Core Data Models (`Data.kt`)
- Standardize data classes with `@DocumentId` and explicit default constructors required for Firestore serialization.

### Phase 3: Profile & User Management (`ProfileScreen.kt`, `EditProfileScreen.kt`)
- Support editing `name`, `college`, `course`, `year`, `bio`, `skills`, `availability`.
- Update `lastActive` timestamp upon application launch/usage.

### Phase 4: Offer Skill & My Services (`ServiceScreens.kt`, `MyServicesScreen.kt`)
- Complete `OfferSkillForm` with all required fields. Set `providerId = FirebaseAuth.getInstance().currentUser.uid`.
- Create `MyServicesScreen` allowing owners to edit, toggle `isActive`, or delete their services.

### Phase 5: Service Details & Discover (`DiscoverScreen.kt`, `ServiceScreens.kt`)
- `ServiceDetailsScreen`: If `service.providerId == currentUid`, show owner management UI ("Your Service", Edit, Manage Requests); otherwise show "Request Service".
- Real-time search in `DiscoverScreen` filtering services by query across title, category, and description.

### Phase 6: Service Requests & Automatic Chat Creation (`RequestsScreen.kt`)
- Real-time listener for Received and Sent requests.
- When service owner clicks "ACCEPT":
  - Update request status to `ACCEPTED`.
  - Calculate deterministic `chatId = sorted(requesterId, providerId).joinToString("_")`.
  - Create/Update `chats/{chatId}` document with `participantIds = [requesterId, providerId]`.
  - Trigger notification to requester.

### Phase 7: Real-time Chat (`ChatScreens.kt`)
- Restrict chat access to users listed in `participantIds`.
- Real-time message list listener (`chats/{chatId}/messages`).
- Send messages in real-time updating `lastMessage`, `lastMessageSenderId`, `lastMessageTime`.

### Phase 8: Service Completion & Ratings/Reviews (`ReviewScreens.kt`, `RequestsScreen.kt`)
- Provider marks request as `COMPLETED`.
- Requester receives option to rate service (1-5 stars + review comment).
- Stores review in `reviews/{reviewId}` and updates provider average rating and review count.

### Phase 9: Firestore Security Rules (`firestore.rules`)
- Write granular rules enforcing that users can only modify their own profile/services, can only access chats they participate in, and cannot forge requester/provider UIDs.

---

## Verification Plan

### Automated Build Verification
- Run `gradle_build("app:assembleDebug")` after each implementation phase to ensure zero compilation or resource errors.

### Manual & Two-Phone Testing Scenario
1. **Phone A (User A - "Sam"):**
   - Logs in using Firebase Auth.
   - Offers a service: Title = "Android App Development", Category = "Programming", Price = "1500".
   - Verifies the service appears under "My Services" with "Your Service" tag.
2. **Phone B (User B - "Nayan"):**
   - Logs in using Firebase Auth.
   - Opens "Discover", finds Sam's service "Android App Development".
   - Opens Service Details -> sees `[REQUEST SERVICE]`.
   - Clicks "Request Service", inputs message: "I need help with my Android project." and sends request.
3. **Phone A (Sam):**
   - Immediately sees a new request in "Requests" -> "Received" tab in real-time (no app restart).
   - Receives push notification / alert.
   - Clicks `[ACCEPT]`.
4. **Phone B (Nayan):**
   - Immediately sees status change to `ACCEPTED` in real-time.
   - `[Open Chat]` button becomes available.
5. **Both Phones (Chat Verification):**
   - Both Sam and Nayan open Chat.
   - Sam types: "Hi Nayan, how can I help?" -> Nayan receives instantly in real-time.
   - Nayan replies: "Hi Sam, I need help with my project." -> Sam receives instantly in real-time.
6. **Completion & Review:**
   - Sam marks request as `COMPLETED`.
   - Nayan sees `[Rate Service]`, submits a 5-star review.
   - Sam's rating on profile updates accordingly.
