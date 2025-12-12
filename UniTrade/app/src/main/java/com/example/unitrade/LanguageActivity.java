package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class LanguageActivity extends AppCompatActivity {

    private RadioGroup rgLanguages;
    private RadioButton rbEnglish, rbChinese, rbMalay;
    private Button btnSave;

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "app_language";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        MaterialToolbar toolbar = findViewById(R.id.appBarLanguage);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Language");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rgLanguages = findViewById(R.id.rgLanguages);
        rbEnglish = findViewById(R.id.rbEnglish);
        rbChinese = findViewById(R.id.rbChinese);
        rbMalay = findViewById(R.id.rbMalay);
        btnSave = findViewById(R.id.btnSaveLanguage);

        // Load current language
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentLang = prefs.getString(KEY_LANGUAGE, "en");
        if (currentLang.equals("zh")) {
            rbChinese.setChecked(true);
        } else if (currentLang.equals("ms")) {
            rbMalay.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }

        btnSave.setOnClickListener(v -> {
            int selectedId = rgLanguages.getCheckedRadioButtonId();
            String langCode = "en";
            if (selectedId == R.id.rbChinese) {
                langCode = "zh";
            } else if (selectedId == R.id.rbMalay) {
                langCode = "ms";
            }
            
            saveLanguage(langCode);
        });
    }

    private void saveLanguage(String lang) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LANGUAGE, lang);
        editor.apply();

        Toast.makeText(this, "Language updated", Toast.LENGTH_SHORT).show();

        // Set the result to notify the previous activity that the language has changed
        setResult(RESULT_OK);
        finish();
    }
}
