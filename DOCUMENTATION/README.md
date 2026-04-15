# UniTrade Project - Complete Documentation

## Project Overview
**UniTrade** is an Android-based peer-to-peer marketplace application built with Firebase backend. It allows users to buy, sell, and trade items, with features including chat messaging, ratings, reviews, and comprehensive user management.

---

## Table of Contents
1. [Project Architecture](#project-architecture)
2. [Directory Structure](#directory-structure)
3. [Technology Stack](#technology-stack)
4. [Core Components](#core-components)
5. [File Categories](#file-categories)

---

## Project Architecture

### Architecture Layers
```
Presentation Layer (Activities, Fragments, Adapters)
    ↓
Business Logic Layer (Managers, Repositories, Utils)
    ↓
Data Layer (Models, Backend Services, Firestore)
    ↓
Server Layer (Firebase - Firestore, Authentication, Storage)
```

### Key Design Patterns Used
- **MVC Pattern**: Activities/Fragments as Views, Managers as Controllers, Models as Data
- **Adapter Pattern**: RecyclerView adapters for View binding
- **Singleton Pattern**: Firebase instances, UserSession, AppSettings
- **Repository Pattern**: UserRepository, ProductRepository for data access
- **Observer Pattern**: Firebase listeners for real-time updates

---

## Directory Structure

```
/workspaces/uni-trade/
├── app/src/main/
│   ├── java/com/example/unitrade/
│   │   ├── ** Models (Data classes)
│   │   ├── **Activity.java (UI screens)
│   │   ├── **Fragment.java (UI fragments)
│   │   ├── **Adapter.java (RecyclerView adapters)
│   │   ├── **Manager.java (Business logic)
│   │   ├── backend/ (Backend/filtering logic)
│   │   ├── model/ (Statistical models)
│   │   ├── base/ (Base classes)
│   │   └── utils/ (Utility classes)
│   ├── res/
│   │   ├── layout/ (XML layouts for screens)
│   │   ├── drawable/ (XML drawables, shapes, gradients)
│   │   ├── values/ (Colors, strings, dimens, themes)
│   │   ├── menu/ (Menu definitions)
│   │   └── navigation/ (Navigation graph)
│   └── AndroidManifest.xml (App configuration)
├── gradle/ (Build system config)
└── DOCUMENTATION/ (This comprehensive guide)
```

---

## Technology Stack

### Frontend
- **Language**: Java
- **UI Framework**: Android Native (API Level 26+)
- **Layout System**: ConstraintLayout, LinearLayout, RecyclerView
- **Navigation**: Jetpack Navigation Component

### Backend
- **Database**: Firebase Firestore (NoSQL)
- **Authentication**: Firebase Authentication
- **Cloud Storage**: Firebase Cloud Storage
- **Messaging**: Firebase Cloud Messaging (FCM)
- **Real-time**: Firestore Listeners

### Libraries & Dependencies (from gradle files)
- AndroidX & Material Design Components
- Firebase (Auth, Firestore, Storage, Messaging)
- Glide (Image Loading)
- RetroFit/OkHttp (HTTP Client)
- Gson (JSON parsing)
- Lottie (Animations)

---

## Core Components

### 1. User Management
**Related Files**: 
- `User.java` - User data model
- `UserRepository.java` - User data access
- `UserSession.java` - Current session management
- `LoginActivity.java`, `SignupActivity.java` - Authentication UI
- `EditProfileActivity.java`, `UserProfileActivity.java` - Profile management

**Key Features**:
- User registration and login
- Profile management
- Seller and buyer ratings
- Device management
- Login history

### 2. Product Management
**Related Files**:
- `Product.java` - Product model
- `ProductRepository.java` - Product data access
- `SellFragment.java` - Sell product UI
- `ProductDetailActivity.java` - View product details
- `ItemAdapter.java`, `ProfileProductAdapter.java` - Product display

**Product Status States**:
- `SOLD` - Product has been sold
- `BOUGHT` - User purchased this product
- `DONATED` - Product was donated

### 3. Shopping & Checkout
**Related Files**:
- `CartManager.java` - Shopping cart operations
- `ShoppingCartActivity.java` - Cart display UI
- `CheckoutActivity.java` - Checkout process
- `Purchase.java` - Purchase model
- `PurchaseManager.java` - Purchase operations

**Checkout Flow**:
1. Add items to cart
2. Select delivery address
3. Choose delivery method
4. Review and confirm payment
5. Create purchase record

### 4. Messaging System
**Related Files**:
- `Chat.java` - Conversation model
- `Message.java` - Individual message model
- `ChatFragment.java`, `ChatAdapter.java` - Chat list UI
- `ConversationActivity.java` - Active chat UI
- `MessageAdapter.java` - Message display

**Features**:
- Real-time messaging
- Typing indicators
- Message timestamps
- Chat bookmarking

### 5. Filtering & Search
**Related Files**:
- `backend/Filter.java` - Firestore filtering logic
- `backend/Sorting.java` - Sort products
- `backend/RecommendationManager.java` - Product recommendations

**Filter Types**:
- Price range
- Product condition (New/Used)
- Listing date
- Category
- Location

### 6. Ratings & Reviews
**Related Files**:
- `Review.java` - Review model
- `RatingReviewsActivity.java` - View reviews
- `RateUserActivity.java` - Submit rating
- `ReviewAdapter.java`, `ReviewListFragment.java` - Review display

**Rating Types**:
- Seller rating (for sellers)
- Buyer/User rating (for buyers)
- Product ratings

### 7. Help & Support
**Related Files**:
- `Ticket.java` - Support ticket model
- `TicketHistoryManager.java` - Ticket operations
- `AddTicketActivity.java` - Create new ticket
- `TicketHistoryActivity.java` - View tickets
- `Report.java` - Problem reports
- `ReportProblemActivity.java` - Report UI

**Supported Features**:
- File attachments
- Color-coded status
- Ticket tracking

### 8. Settings & Configuration
**Related Files**:
- `SettingsActivity.java` - Main settings UI
- `AppSettings.java` - App-wide settings
- `NotificationSettingsActivity.java` - Notification prefs
- `LanguageActivity.java` - Language selection
- `CurrencyActivity.java` - Currency selection

---

## File Categories

### Models (Data Classes)
Files that define data structures. Located in `/java/com/example/unitrade/` and `/model/`:
- `User.java` - User profile and authentication data
- `Product.java` - Product listing information
- `Review.java` - User review/rating
- `Chat.java` - Chat conversation
- `Message.java` - Individual chat message
- `Address.java` - User address for delivery
- `Currency.java` - Currency configuration
- `Category.java` - Product category
- `Purchase.java` - Purchase transaction
- `Transaction.java` - Transaction history
- `Device.java` - User device info
- `Ticket.java` - Support ticket
- `Report.java` - Problem report
- `LoginHistoryItem.java` - Login attempt record

**Model Characteristics**:
- Implement `Parcelable` for inter-activity data passing
- Include Firebase `@PropertyName` and `@Exclude` annotations
- Have getters/setters for all properties
- Include empty constructor (required by Firebase)

### Activities (UI Screens)
Main screens/Activities in the app:
- `MainActivity.java` - Main dashboard with bottom navigation
- `LoginActivity.java`, `SignupActivity.java` - Authentication screens
- `UserProfileActivity.java` - View other user profiles
- `EditProfileActivity.java` - Edit own profile
- `ProductDetailActivity.java` - View product details
- `CheckoutActivity.java` - Purchase checkout
- `ShoppingCartActivity.java` - Shopping cart
- `ConversationActivity.java` - Active chat screen
- `RatingReviewsActivity.java` - View ratings
- `SettingsActivity.java` - App settings
- Various others for support, language, currency selection

**Activity Lifecycle**:
```
onCreate() → onStart() → onResume() → onPause() → onStop() → onDestroy()
```

### Fragments (UI Components)
Reusable UI fragments displayed within Activities:
- `HomeFragment.java` - Home feed/dashboard
- `SellFragment.java` - Sell product form
- `ChatFragment.java` - Chat list
- `MyProfileFragment.java` - User profile display
- `ReviewListFragment.java` - Reviews display
- `TransactionStatisticsFragment.java` - Statistics dashboard

### Adapters (RecyclerView Binding)
Adapters for displaying lists/grids:
- `ItemAdapter.java` - Products grid
- `CategoryAdapter.java` - Category list
- `AddressAdapter.java` - Address selection
- `ChatAdapter.java` - Chat list
- `MessageAdapter.java` - Messages in conversation
- `ProductAdapter.java` - Products in results
- `ReviewAdapter.java` - Review list
- `TransactionAdapter.java` - Transaction history
- Many others...

**Adapter Pattern**:
```
Adapter extends RecyclerView.Adapter
- onCreateViewHolder() - Create view layout
- onBindViewHolder() - Bind data to views
- getItemCount() - Return list size
```

### Managers (Business Logic)
Classes managing complex operations:
- `CartManager.java` - Shopping cart operations
- `PurchaseManager.java` - Purchase processing
- `TicketHistoryManager.java` - Support ticket management
- `RecommendationManager.java` - Product recommendations

**Typical Manager Pattern**:
```
Public manager methods that:
1. Validate input
2. Query Firestore
3. Process results
4. Return via callback/listener
```

### Repositories (Data Access)
Classes managing data access from Firestore:
- `UserRepository.java` - User data CRUD operations
- `ProductRepository.java` - Product data access
- Backend services for filtering, sorting

**Repository Responsibilities**:
- Query Firestore collections
- Map Firestore documents to Java objects
- Handle data transformation
- Provide callbacks for UI updates

### Backend Logic
Advanced processing and server-side operations:
- `backend/Filter.java` - Product filtering (price, condition, date)
- `backend/Sorting.java` - Product sorting
- `backend/RecommendationManager.java` - ML-like recommendations
- `backend/FetchProductId.java` - ID resolution

### Utilities & Helpers
Helper classes for common tasks:
- `ImageStorageUtil.java` - Firebase Storage image operations
- `TranslatorUtil.java` - Language translation
- `UserSession.java` - Current user session management
- `AppSettings.java` - App configuration
- `PieChartDataGenerator.java` - Chart data generation
- `MyFirebaseMessagingService.java` - Push notifications

---

## Common Patterns

### Firebase Query with Callback
```java
db.collection("products")
    .whereEqualTo("category", "Electronics")
    .get()
    .addOnSuccessListener(queryDocumentSnapshots -> {
        List<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            products.add(doc.toObject(Product.class));
        }
        callback.onSuccess(products); // Return results
    })
    .addOnFailureListener(e -> {
        Log.e("FireStore", "Error", e);
    });
```

### RecyclerView Setup
```java
RecyclerView recyclerView = findViewById(R.id.recyclerView);
recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
ProductAdapter adapter = new ProductAdapter(products);
recyclerView.setAdapter(adapter);
```

### Activity to Activity Navigation with Data
```java
Intent intent = new Intent(CurrentActivity.this, NextActivity.class);
intent.putExtra("user", userObject); // Parcelable
startActivity(intent);

// In NextActivity
User user = getIntent().getParcelableExtra("user");
```

---

## Important Annotations

### Firebase
- `@PropertyName("fieldName")` - Map Java field to Firestore field
- `@Exclude` - Exclude field from Firestore serialization

### Android
- `@Override` - Method override from parent class
- `@SuppressWarnings` - Suppress compiler warnings
- `@Deprecated` - Mark method as deprecated

---

## File Documentation Index

This documentation includes detailed explanations for:

### Models (with line-by-line comments)
- `User.java` - See: `/models/1_User_Documented.java`
- `Product.java` - See: `/models/2_Product_Documented.java`
- `Review.java` - See: `/models/3_Review_Documented.java`

### Backend/Business Logic
- `Filter.java` - See: `/backend/1_Filter_Documented.java`
- `CartManager.java` - See: `/managers/1_CartManager_Documented.java`
- `UserRepository.java` - See: `/repositories/1_UserRepository_Documented.java`

### Activities (Key screens)
- `LoginActivity.java` - See: `/activities/1_LoginActivity_Documented.java`
- `MainActivity.java` - See: `/activities/2_MainActivity_Documented.java`

### Adapters
- `ItemAdapter.java` - See: `/adapters/1_ItemAdapter_Documented.java`

### XML Files
- Layout files - See: `/xml_files/LAYOUTS.md`
- Drawable files (shapes, gradients) - See: `/xml_files/DRAWABLES.md`
- Colors and themes - See: `/xml_files/COLORS_AND_THEMES.md`

---

## How to Use This Documentation

1. **For Understanding Architecture**: Read this README first
2. **For Specific Components**: Navigate to the relevant folder (models/, adapters/, etc.)
3. **For Detailed Code**: Open the `*_Documented.java` files with full line-by-line comments
4. **For XML Resources**: See the XML section for layout and drawable documentation

---

## Build & Run

### Build System: Gradle

**Key Build Files**:
- `build.gradle.kts` (Project level)
- `app/build.gradle.kts` (App level)
- `gradle.properties` (Global properties)

**Build Command**:
```bash
./gradlew build
```

**Run Android App**:
```bash
./gradlew installDebug
```

---

## Firebase Configuration

The app uses Firebase with these services:
- **Firestore**: Primary database for all data
- **Authentication**: User login/signup
- **Cloud Storage**: Image storage
- **Cloud Messaging**: Push notifications

**Collections**:
- `users` - User profiles
- `products` - Product listings
- `chats` - Conversations
- `messages` - Chat messages
- `reviews` - User reviews
- `purchases` - Purchase records
- `transactions` - Transaction history
- `tickets` - Support tickets
- And others...

---

## Security & Authentication

- Users authenticate via Firebase Authentication
- User session managed by `UserSession.java` singleton
- Firestore security rules restrict data access
- Images stored in Firebase Cloud Storage

---

## Future Documentation to Add
- [ ] API endpoints and REST calls
- [ ] Database schema (collections, documents, fields)
- [ ] Complete XML file documentation
- [ ] Test cases and debugging guide
- [ ] Deployment instructions

---

**Last Updated**: April 2026  
**Version**: Complete Project Documentation v1.0
