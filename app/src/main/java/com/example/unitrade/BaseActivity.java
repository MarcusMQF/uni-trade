package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {//Called every time Activity becomes visible
        super.onResume();
        // Apply translation to all views in the current activity
        translateUI();
    }

    private void translateUI() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String targetLanguage = prefs.getString("app_language", "en");
//Retrieves the saved language preference
        // No translation needed for English
        if ("en".equals(targetLanguage)) {
            return;
        }

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        translateAllTextViews(rootView, targetLanguage);
        /*
        getWindow() → Gets the window
        getDecorView() → Gets the decorated view (includes toolbar, content, etc.)
        getRootView() → Gets the topmost parent
        (ViewGroup) → Casts it to ViewGroup type
        */

        // Translate the toolbar title
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            TranslatorUtil.getInstance().translate(getSupportActionBar().getTitle(), targetLanguage, title -> {
                getSupportActionBar().setTitle(title);
            });
            //getSupportActionBar() → Gets the toolbar
            //Lambda title -> { getSupportActionBar().setTitle(title); } → When translation is done, updates the title

        }
    }
//ViewGroup is a container that holds and manages other views (UI components).
    private void translateAllTextViews(ViewGroup viewGroup, String targetLanguage) {
        if (viewGroup == null) {
            return;
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            //Loop through all child views in the viewGroup
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                //If the child is a TextView (text element)
                TranslatorUtil.getInstance().translate((TextView) child, targetLanguage);
                //Calls TranslatorUtil to translate it
            } else if (child instanceof ViewGroup) {
                translateAllTextViews((ViewGroup) child, targetLanguage);
            }
        }
    }

    // Call this in child activities AFTER setContentView()
    protected void setupToolbar(int toolbarId) {
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        //It is used to convert a menu XML file into actual menu items in your app (usually in the toolbar or action bar).
        // Translate menu items
        //convert xml to menu objects
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String targetLanguage = prefs.getString("app_language", "en");
        if (!"en".equals(targetLanguage)) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                TranslatorUtil.getInstance().translate(item.getTitle(), targetLanguage, item::setTitle);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed(); // ← BACK BUTTON
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
