package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -----------------------------
        // Toolbar
        // -----------------------------
        MaterialToolbar toolbar = findViewById(R.id.appBarMain);
        setSupportActionBar(toolbar);

        // -----------------------------
        // Navigation
        // -----------------------------
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.NHFMain);
        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_navigation);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_chat,
                R.id.nav_sell,
                R.id.nav_transaction_stats,
                R.id.nav_profile
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Tint overflow menu icon
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);

        handleIntent(getIntent());
    }


    // ======================================================
    // Handle all external navigation cases
    // ======================================================
    private void handleIntent(Intent intent) {

        boolean openSell = intent.getBooleanExtra("openSellFragment", false);
        boolean goToHome = intent.getBooleanExtra("goToHome", false);
        boolean fromExternal = intent.getBooleanExtra("fromExternal", false);
        boolean editMode = intent.getBooleanExtra("editMode", false);
        boolean openChat = intent.getBooleanExtra("openChatFragment", false);
        Product productToEdit = intent.getParcelableExtra("product_to_edit");

        // ---------------------------------------------
        // CASE 1: Open ChatFragment from Checkout FAB
        // ---------------------------------------------
        if (openChat) {
            Bundle b = new Bundle();
            b.putBoolean("fromExternal", true);

            bottomNav.setSelectedItemId(R.id.nav_chat);
            navController.navigate(R.id.nav_chat, b);
            return;
        }

        // ---------------------------------------------
        // CASE 2: Go Home (Shop Now from History)
        // ---------------------------------------------
        if (goToHome) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            navController.navigate(R.id.nav_home);
            return;
        }

        // ---------------------------------------------
        // CASE 3: Open Sell Fragment OR Edit Item
        // ---------------------------------------------
        if (openSell) {
            Bundle b = new Bundle();
            b.putBoolean("fromExternal", fromExternal);

            if (editMode && productToEdit != null) {
                b.putBoolean("editMode", true);
                b.putParcelable("product_to_edit", productToEdit);
            }

            bottomNav.setSelectedItemId(R.id.nav_sell);
            navController.navigate(R.id.nav_sell, b);
            return;
        }
    }


    // ======================================================
    // Handle NEW intents (e.g., coming back from Checkout)
    // ======================================================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }


    // ======================================================
    // Navigation Up / Back
    // ======================================================
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // ======================================================
    // Toolbar menu
    // ======================================================
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
