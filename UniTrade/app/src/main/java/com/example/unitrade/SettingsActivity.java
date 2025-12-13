package com.example.unitrade;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity {

    private ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Save the updated user info
                    if (result.getData() != null) {
                        User updatedUser = result.getData().getParcelableExtra("updated_user");
                        if (updatedUser != null) {
                            SampleData.updateUser(this, updatedUser);
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private ActivityResultLauncher<Intent> currencyActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    recreate(); // Recreate the activity to apply changes
                }
            });

    private ActivityResultLauncher<Intent> languageActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    recreate(); // Recreate the activity to apply language changes
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.appBarSettings);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Add a click listener to the toolbar's navigation icon
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        tintToolbarOverflow(toolbar);

        // Initialize all settings items
        setupSettingItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This will help refresh any state if needed when returning to the settings screen.
    }

    private void setupSettingItems() {

        findViewById(R.id.setting_edit_profile).setOnClickListener(v -> {

            String userId = UserSession.get().getId();   // ✅ ONLY ID

            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("user_id", userId);

            editProfileLauncher.launch(intent);
        });

        // Account
        findViewById(R.id.setting_login_security)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, LoginSecurityActivity.class)));

        // Preferences
        setupSettingItem(R.id.setting_notifications, NotificationSettingsActivity.class);
        setupSettingItem(R.id.setting_privacy, PrivacyPolicyActivity.class);

        findViewById(R.id.setting_language).setOnClickListener(v ->
                languageActivityLauncher.launch(new Intent(this, LanguageActivity.class)));

        findViewById(R.id.setting_currency).setOnClickListener(v ->
                currencyActivityLauncher.launch(new Intent(this, CurrencyActivity.class)));

        // Support
        setupSettingItem(R.id.setting_help_support, HelpSupportActivity.class);

        findViewById(R.id.setting_report_problem)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, ReportProblemActivity.class)));

        // Legal
        setupSettingItem(R.id.setting_terms_of_service, TermsOfServiceActivity.class);

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutConfirmation());
    }

    private void setupSettingItem(int viewId, Class<?> activityClass) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                if (activityClass != null) {
                    startActivity(new Intent(this, activityClass));
                } else {
                    showToast("This feature is not yet implemented.");
                }
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
