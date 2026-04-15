package com.example.unitrade;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);


        Toolbar toolbar = findViewById(R.id.appBarAboutUs);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) { //check action bar exist 
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back/up arrow
            getSupportActionBar().setTitle("About Us");//set the title shown on the toolbar
        }


        tintToolbarOverflow(toolbar); //call helper method to tint the overflow menu icon to match the theme
        // Set up the about us content
        setupAboutUsContent();
    }

    private void setupAboutUsContent() {
        TextView contentView = findViewById(R.id.aboutUsContent);
        if (contentView != null) {
            String aboutUs = "Welcome to UniTrade\n\n" +
                "UniTrade is a platform designed for students to buy and sell items within their university community.\n\n" +
                "Our Mission\n" +
                "To create a safe, convenient, and sustainable marketplace for university students.\n\n" +
                "Our Vision\n" +
                "To be the go-to platform for student commerce, fostering a community of trust and sustainability.\n\n" +
                "Version: 1.0.0\n" +
                "© 2023 UniTrade. All rights reserved.";
            
            contentView.setText(aboutUs);
        }
    }
}
