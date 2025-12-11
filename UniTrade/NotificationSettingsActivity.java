package com.example.unitrade;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NotificationSettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String PREF_PUSH = "push_notifications";
    private static final String PREF_EMAIL = "email_notifications";
    private static final String PREF_SOUND = "sound";
    private static final String PREF_VIBRATE = "vibrate";
    private static final String PREF_PREVIEW = "preview_message";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        Toolbar toolbar = findViewById(R.id.appBarNotificationSettings);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notification Settings");
        }

        tintToolbarOverflow(toolbar);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        // Initialize switches
        initializeSwitch(R.id.switchPushNotifications, PREF_PUSH, true);
        initializeSwitch(R.id.switchEmailNotifications, PREF_EMAIL, true);
        initializeSwitch(R.id.switchSound, PREF_SOUND, true);
        initializeSwitch(R.id.switchVibrate, PREF_VIBRATE, true);
        initializeSwitch(R.id.switchPreviewMessage, PREF_PREVIEW, true);
    }

    private void initializeSwitch(int switchId, String prefKey, boolean defaultValue) {
        Switch switchView = findViewById(switchId);
        if (switchView != null) {
            // Set initial state from SharedPreferences
            boolean isChecked = sharedPreferences.getBoolean(prefKey, defaultValue);
            switchView.setChecked(isChecked);

            // Save preference when switch is toggled
            switchView.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(prefKey, isChecked1);
                editor.apply();
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}