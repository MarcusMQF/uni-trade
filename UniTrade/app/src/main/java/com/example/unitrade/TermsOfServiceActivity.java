package com.example.unitrade;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TermsOfServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);

        MaterialToolbar toolbar = findViewById(R.id.appBarTermsOfService);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Terms of Service");
        }

        tintToolbarOverflow(toolbar);

        // --- Set Last Updated Date ---
        TextView tvLastUpdated = findViewById(R.id.tvLastUpdated);
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvLastUpdated.setText("Last updated: " + currentDate);
    }
}
