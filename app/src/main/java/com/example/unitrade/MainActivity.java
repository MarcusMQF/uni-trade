package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --------------------------------------------------
        // Ensure user session exists
        // --------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User currentUser = documentSnapshot.toObject(User.class);
                            UserSession.set(currentUser); // store in session
                            Log.d("MainActivity", "Logged-in user: " + currentUser.getUsername());
                        } else {
                            Log.e("MainActivity", "User not found in Firestore");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error fetching user", e);
                    });
        } else {
            Log.e("MainActivity", "No Firebase user logged in");
        }

        // --------------------------------------------------
        // Toolbar
        // --------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.appBarMain);
        setSupportActionBar(toolbar);

        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null)
            overflow.setTint(Color.WHITE);

        // --------------------------------------------------
        // NavHost + NavController
        // --------------------------------------------------
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.NHFMain);

        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_navigation);

        // Top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_chat,
                R.id.nav_sell,
                R.id.nav_transaction_stats,
                R.id.nav_profile).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // --------------------------------------------------
        // Handle intent navigation
        // --------------------------------------------------
        handleIntent(getIntent());



        getAndSaveFCMToken();

    }

    // ==================================================
    // Handle navigation from external Activities
    // ==================================================
    private void handleIntent(Intent intent) {

        if (intent == null)
            return;

        // --------------------------------------------------
        // 1. Select bottom navigation tab
        // --------------------------------------------------
        String selectTab = intent.getStringExtra("selectTab");
        if (selectTab != null) {
            switch (selectTab) {
                case "profile":
                    bottomNav.setSelectedItemId(R.id.nav_profile);
                    break;
                case "home":
                    bottomNav.setSelectedItemId(R.id.nav_home);
                    break;
                case "chat":
                    bottomNav.setSelectedItemId(R.id.nav_chat);
                    break;
                case "sell":
                    bottomNav.setSelectedItemId(R.id.nav_sell);
                    break;
            }
            intent.removeExtra("selectTab");
        }

        // --------------------------------------------------
        // 2. Go to Home
        // --------------------------------------------------
        if (intent.getBooleanExtra("goToHome", false)) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            navController.navigate(R.id.nav_home);
            intent.removeExtra("goToHome");
            return;
        }

        // --------------------------------------------------
        // 3. Open Chat
        // --------------------------------------------------
        if (intent.getBooleanExtra("openChatFragment", false)) {
            bottomNav.setSelectedItemId(R.id.nav_chat);
            navController.navigate(R.id.nav_chat);
            intent.removeExtra("openChatFragment");
            return;
        }

        // --------------------------------------------------
        // 4. Open SellFragment (NEW or EDIT)
        // --------------------------------------------------
        if (intent.getBooleanExtra("openSellFragment", false)) {

            Bundle args = new Bundle();

            // Origin (product_detail / my_listings / my_history / new_listing)
            String origin = intent.getStringExtra("origin");
            if (origin != null) {
                args.putString("origin", origin);
            }

            boolean editMode = intent.getBooleanExtra("editMode", false);
            String productId = intent.getStringExtra("product_id");

            if (editMode && productId != null) {
                args.putBoolean("editMode", true);
                args.putString("product_id", productId);
            }

            bottomNav.setSelectedItemId(R.id.nav_sell);
            navController.navigate(R.id.nav_sell, args);

            // Prevent re-trigger
            intent.removeExtra("openSellFragment");
            intent.removeExtra("editMode");
            intent.removeExtra("product_id");
            intent.removeExtra("origin");

            return;
        }
    }

    // ==================================================
    // Receive new intents (e.g. returning from checkout)
    // ==================================================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    // ==================================================
    // Navigation Up
    // ==================================================
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // ==================================================
    // Toolbar menu
    // ==================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.action_logout) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAndSaveFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();    // MainActivity.java inside getAndSaveFCMToken()
                    Log.d("FCM_DEBUG", "Current Token: " + token);
                    String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

                    if (currentUserId != null) {
                        // Save it to the user's document in Firestore
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(currentUserId)
                                .update("fcmToken", token)
                                .addOnFailureListener(e -> {
                                    // If update fails (e.g. document doesn't exist yet), use set with merge
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("fcmToken", token);
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(currentUserId)
                                            .set(data, com.google.firebase.firestore.SetOptions.merge());
                                });
                    }
                });
    }

}
