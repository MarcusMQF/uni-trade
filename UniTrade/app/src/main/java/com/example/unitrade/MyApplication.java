package com.example.unitrade;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContext(base));
    }

    private Context updateBaseContext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE);
        String language = prefs.getString("app_language", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}