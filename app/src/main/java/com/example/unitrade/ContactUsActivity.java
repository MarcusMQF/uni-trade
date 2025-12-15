package com.example.unitrade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ContactUsActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        Toolbar toolbar = findViewById(R.id.appBarContactUs);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contact Us");
        }

        tintToolbarOverflow(toolbar);

        // Setup contact methods
        setupContactMethods();
    }

    private void setupContactMethods() {
        // Email
        View emailLayout = findViewById(R.id.layoutEmail);
        if (emailLayout != null) {
            emailLayout.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:support@unitrade.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
                startActivity(Intent.createChooser(intent, "Send email"));
            });
        }

        // Phone
        View phoneLayout = findViewById(R.id.layoutPhone);
        if (phoneLayout != null) {
            phoneLayout.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+1234567890"));
                startActivity(intent);
            });
        }

        // WhatsApp
        View whatsappLayout = findViewById(R.id.layoutWhatsApp);
        if (whatsappLayout != null) {
            whatsappLayout.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://wa.me/1234567890"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
