package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(); // Load language preference at the start of every activity
    }


        private void loadLocale() {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            // Default to English if no language is saved
            String language = prefs.getString("app_language", "en");
            setLocale(language);
        }

        private void setLocale(String lang) {
            if (lang == null || lang.isEmpty()) {
                return;
            }
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        @Override
        protected void onResume() {
            super.onResume();
            // It's also a good practice to reload the locale onResume
            // in case the activity was paused and settings were changed elsewhere.
            loadLocale();
        }

    // Call this in child activities AFTER setContentView()
    protected void setupToolbar(int toolbarId) {
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();   // ← BACK BUTTON
            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void tintToolbarOverflow(Toolbar toolbar) {
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) {
            overflow.setTint(Color.WHITE);
        }
    }


}
