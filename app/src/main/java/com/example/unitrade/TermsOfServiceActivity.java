package com.example.unitrade;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

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
        tvLastUpdated.setText("Last updated: 13 May 2024");
    }
}
