package com.example.unitrade;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;// Short popup messages.

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class ChangePasswordActivity extends BaseActivity {

    private LinearLayout layoutStep1, layoutStep2;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnNext, btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.appBarChangePassword);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Enables the back arrow in the toolbar.
            getSupportActionBar().setTitle("Change Password");//Sets the toolbar title to "Change Password".
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);//Changes the back arrow icon to a white arrow drawable.
        }
        tintToolbarOverflow(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Views
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnNext = findViewById(R.id.btnNext);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        // Step 1: Verify Current Password
        btnNext.setOnClickListener(v -> {
            String current = etCurrentPassword.getText().toString();
            //It gets the text entered by the user in an EditText (password field) and converts it into a String.
            if (current.isEmpty()) {
                etCurrentPassword.setError("Please enter your password");
                return;
            }
            
            // Simulate verification (In real app, verify with backend)
            if (current.equals("123456")) { // Mock password for demo
                showStep2();
            } else {
                // For this demo, we accept any non-empty password to proceed
                // or you could show error: "Incorrect password"
                // etCurrentPassword.setError("Incorrect password");
                showStep2(); // Proceeding for demo purposes
            }
        });

        // Step 2: Save New Password
        btnSavePassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (newPass.isEmpty()) {
                etNewPassword.setError("Enter new password");
                return;
            }
            if (confirmPass.isEmpty()) {
                etConfirmPassword.setError("Confirm new password");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            // Simulate saving
            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            finish();
            /*
            Shows a success message.
Calls finish() to close the activity and return to the previous screen.
             */
        });
    }

    private void showStep2() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        /*
        Hides step 1 layout.
Shows step 2 layout.
This swaps the visible part of the screen from verification to password entry.
         */
    }
}
