package com.example.unitrade;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class LoginSecurityActivity extends BaseActivity {

    private SwitchMaterial switch2fa;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_security);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.appBarLoginSecurity);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Login & Security");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        tintToolbarOverflow(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Change Password
        setupActionItem(R.id.item_change_password, v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Two-Factor Authentication
        switch2fa = findViewById(R.id.switch_2fa);
        if (switch2fa != null) {
            switch2fa.setChecked(sharedPreferences.getBoolean("2fa_enabled", false));
            switch2fa.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("2fa_enabled", isChecked).apply();
                showToast("Two-Factor Authentication " + (isChecked ? "Enabled" : "Disabled"));
            });
        }

        View item2fa = findViewById(R.id.item_2fa);
        if (item2fa != null) {
            item2fa.setOnClickListener(v -> {
                if (switch2fa != null) {
                    switch2fa.toggle();
                }
            });
        }

        // Manage Devices
        setupActionItem(R.id.item_manage_devices, v -> {
            Intent intent = new Intent(this, ManageDevicesActivity.class);
            startActivity(intent);
        });

        // Login History
        setupActionItem(R.id.item_login_history, v -> {
            Intent intent = new Intent(this, LoginHistoryActivity.class);
            startActivity(intent);
        });

        // Log Out from all devices
        setupActionItem(R.id.item_logout_all, v -> showLogoutAllConfirmation());
    }

    private void setupActionItem(int viewId, View.OnClickListener listener) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLogoutAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Log out from all devices?")
                .setMessage("You will be logged out from all sessions except this one. Are you sure?")
                .setPositiveButton("Log Out All", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear user session/preferences here if applicable
        
        // Navigate to Login Screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();
        
        showToast("Logged out from all devices");
    }
}
