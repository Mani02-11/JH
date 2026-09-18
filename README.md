# JustHire - "Find Skills. Hire Instantly. Get Things Done."

**JustHire** is a peer-to-peer student skill hiring and real-time messaging Android application. Built specifically for college campuses, it allows students to offer their unique skills, discover services provided by their peers, and seamlessly hire each other for specific tasks.

## 🚀 Features

*   **Secure Authentication:** Powered by Firebase Authentication (Email/Password).
*   **Dynamic User Profiles:** Students can customize their profiles with their college, course, year, bio, skills, and availability. 
*   **Offer & Discover Skills:** Users can easily post services they offer (e.g., Programming, Design, Tutoring) and browse an active marketplace of peer services.
*   **Service Requests:** A streamlined workflow allowing users to request a service and providers to Accept or Reject incoming requests.
*   **Real-Time Messaging:** Fully integrated real-time chat unlocks automatically once a service request is accepted.
*   **Push Notifications:** Firebase Cloud Messaging (FCM) ensures users are instantly notified of new requests, acceptances, and messages.
*   **Ratings & Reviews:** Built-in review system to build trust and credibility within the student community.
*   **Modern UI:** Developed using Jetpack Compose with a sleek, dark glassmorphism aesthetic (the "JustHire Space" theme).

## 🛠️ Technology Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Backend as a Service (BaaS):** Firebase
    *   *Authentication:* User login and secure access.
    *   *Cloud Firestore:* NoSQL real-time database for storing users, services, requests, chats, and reviews.
    *   *Cloud Messaging (FCM):* For reliable push notifications.

## 📱 Application Flow

1.  **Login/Register:** Secure entry into the platform.
2.  **Home/Discover:** Browse popular services, search by category or skill, and view recommended students.
3.  **Service Details:** View the provider's details, rating, and service specifics.
4.  **Request Service:** Send a hiring request to the service provider.
5.  **Owner Acceptance:** The service owner reviews the request and clicks 'Accept'.
6.  **Real-Time Chat:** A secure, deterministic chat room is automatically created for the two parties to communicate.
7.  **Completion & Review:** The provider marks the service as completed, prompting the requester to leave a 1-5 star rating and feedback.

## 🔒 Security

*   **Firestore Rules:** Strict security rules guarantee that users can only modify their own profiles and services, and can only access chats they are participants in.
*   **No Firebase Storage:** The application is optimized to run entirely without Firebase Storage, utilizing dynamic UI-generated avatars (Initials) to reduce overhead and costs.

## 📥 Setup & Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle dependencies.
4. Add your `google-services.json` file from your Firebase console to the `app/` directory.
5. Ensure your Firebase Firestore database is set up and the provided `firestore.rules` are published in the Firebase Console.
6. Build and run on an Android emulator or physical device.

---
*Developed for peer-to-peer campus collaboration.*