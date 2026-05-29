# Plan With Friends

## Văideanu Renata - Georgia ##

Plan With Friends is a modern Android application designed to help users organize events and collaborate with their peers seamlessly. Built with an "Offline-First" approach, the app ensures that users can manage their schedules, groups, and events without an active internet connection, automatically synchronizing data with a cloud server once the network is restored.

### Repository Structure
The project is structured into distinct architectural layers following the Clean Architecture principles:
* **UI Layer (`ui/`)**: The presentation layer built entirely with modern Android paradigms (Kotlin, Jetpack Compose, MVVM).
* **Data Layer (`data/`)**: The core business logic, subdivided into the database (Room SQLite for local persistence) and network (Retrofit API calls to RestDB).

### System Architecture
The application follows the official Android recommended architecture, utilizing a Unidirectional Data Flow (UDF) pattern. 

The UI layer observes state exposed by ViewModels (`StateFlow`), which collect data from the Repository layer. The Repositories acts as the Single Source of Truth, aggregating data from the local SQLite database (via Room DAO) and the remote cloud server (RestDB via Retrofit). 

When a user performs an action (e.g., creating an event), the Repository immediately updates the local database so the UI reflects the change instantly. Asynchronously, a Coroutine dispatches the network request to sync the data with the cloud.

### Course Requirements Fulfillment
This project was specifically tailored to meet and exceed the requirements of the Android Course Project Assignment:
* **Kotlin & Jetpack Compose:** Built 100% using Kotlin and declarative Compose UI.
* **Screens & Jetpack Navigation:** Features multiple interconnected screens (Calendar, Group Details, Groups List, Settings) managed by `NavHost`.
* **Recommended Architecture:** Strict adherence to MVVM and Repository patterns.
* **Online API Server:** Integrated with RestDB (an online NoSQL database with a RESTful API) using Retrofit.

#### Bonus Points Achieved:
* **Database (0.5p):** Utilizes Room SQLite for robust offline data persistence.
* **Input Sanitization (0.5p):** Room automatically uses parameterized queries to prevent SQL injection. Network models strictly enforce data types.
* **Settings Screen (0.5p):** A dedicated settings screen allows users to customize seasonal color themes and switch between 4 languages dynamically.
* **Coroutines / Dispatcher (0.5p):** Heavy utilization of `viewModelScope.launch` and `suspend` functions for non-blocking database and network operations.
* **Code Readability & Modularization (0.5p):** Clean package hierarchy (`ui/screens`, `data/network`, `data/database`).
* **Unit Testing (0.5p):** Logic validations and repository tests implemented.

### Key Technologies
* **Jetpack Compose:** Declarative UI toolkit utilizing Material Design 3 guidelines.
* **Room Database:** Abstraction layer over SQLite for offline-first caching.
* **Retrofit2 & Gson:** Type-safe HTTP client for API requests and JSON parsing.
* **Coroutines and Kotlin Flow:** Asynchronous programming and reactive state collection.
* **RestDB.io:** Cloud NoSQL database functioning as the primary backend API.

### Core Features
* **Secure Authentication (Login/Register):** Users can create accounts securely. The app checks the cloud API to validate unique usernames and fetches the user's groups automatically upon login.
* **Collaborative Groups:** Users can create or join planning groups using unique 6-character shortcodes (e.g., `X7B9K2`).
* **Smart Leave/Delete Logic:** When a user leaves a group, it is removed from their device and the server's member count drops. If the last person leaves, the group is completely deleted from the cloud server.
* **True Offline-First Synchronization:** Add events or groups without Wi-Fi. The changes are cached locally and pushed to the cloud automatically when a connection is established.
* **Seasonal Theming & Dark Mode:** The app dynamically changes its color palette based on the seasons (Spring, Summer, Autumn, Winter). This can be tied to the current month or overridden manually by the user.
* **Per-App Language Switching:** Fully localized in English, Romanian, French, and Spanish. Changes are applied instantly without restarting the app.

### Data Schema Overview (RestDB & Room)

| Component | Local (Room Entity) | Remote (RestDB Collection) | Purpose |
| :--- | :--- | :--- | :--- |
| **Users** | `UserEntity` | `users` | Stores `username`, `password`, and auto-generated `_id`. |
| **Groups** | `GroupEntity` | `groups` | Stores the `groupId` (shortcode), `name`, and `memberCount`. |
| **Events** | `EventEntity` | `events` | Links to a `groupId`. Stores `title`, `time`, and `date`. |

---

### Getting Started

#### 1. Cloud Backend Setup (RestDB)
1. Create a free account on [RestDB.io](https://restdb.io/).
2. Create a new database (e.g., `planwithfriends-db`).
3. Navigate to **Settings -> API Keys** and generate a new key.
4. Create 3 collections: `users`, `groups`, `events`.
5. Ensure fields match the NetworkModels precisely (e.g., `username`, `password`, `groupId`, `title`).

#### 2. Android Application
1. Download and install Android Studio.
2. Open the `PlanWithFriends` project folder.
3. Open `data/network/RetrofitClient.kt`.
4. Replace `BASE_URL` with your RestDB endpoint and `API_KEY` with the key generated in Step 1.
5. Allow Gradle to sync and download all dependencies (Compose BOM, Room, Retrofit, Coroutines).
6. Connect an Android device or Emulator.
7. Click **Run** to build and install the app.

### Usage Guide
* **Authentication:** Launch the app and register a new user. The data will be pushed to the RestDB users collection. Log in to access the main dashboard.
* **Groups Dashboard:** Tap the **+** button to create a new group. A unique 6-character code will be generated. Share this code with friends so they can use the "Join Group" feature.
* **Event Planning:** Tap on any group to view its details. Use the Floating Action Button to add an event. The event will appear instantly on your screen and will be pushed to the cloud for other group members to see.
* **Settings & Personalization:** Navigate to the Settings tab to change your language (e.g., switch to Romanian) or force a seasonal theme (e.g., Winter Theme). Use the Logout button to clear local data and switch accounts.
