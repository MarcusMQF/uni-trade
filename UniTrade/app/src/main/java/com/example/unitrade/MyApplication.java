package com.example.unitrade;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_DEFAULT_CURRENCY_SET = "DefaultCurrencySet_v2";

    @Override
    public void onCreate() {
        super.onCreate();

        // This logic ensures that RM is set as the default currency one time.
        // It corrects any previously saved incorrect defaults.
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_DEFAULT_CURRENCY_SET, false)) {
            AppSettings.setCurrency(this, "RM");
            prefs.edit().putBoolean(KEY_DEFAULT_CURRENCY_SET, true).apply();
        }
    }
}
