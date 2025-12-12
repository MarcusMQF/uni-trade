package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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
        if (UserSession.get() == null) {
            User defaultUser = SampleData.generateSampleUsers(this).get(0);
            UserSession.set(defaultUser);
        }

        // --------------------------------------------------
        // Toolbar
        // --------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.appBarMain);
        setSupportActionBar(toolbar);

        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);

        // --------------------------------------------------
        // NavHost + NavController
        // --------------------------------------------------
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.NHFMain);

        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_navigation);

        // Top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_chat,
                R.id.nav_sell,
                R.id.nav_transaction_stats,
                R.id.nav_profile
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // --------------------------------------------------
        // Handle intent navigation
        // --------------------------------------------------
        handleIntent(getIntent());
    }

    // ==================================================
    // Handle navigation from external Activities
    // ==================================================
    private void handleIntent(Intent intent) {

        if (intent == null) return;

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
}
