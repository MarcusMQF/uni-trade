package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private EditText edtName, edtSiswamail, edtPassword, edtConfirmPassword;
    private Button btnCreateAccount;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Keep your original UI initialization
        edtName = findViewById(R.id.edtName);
        edtSiswamail = findViewById(R.id.edtSiswamail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnSignUp);

        // Keep your original prefill logic
        String prefillEmail = getIntent().getStringExtra("email_prefill");
        if (prefillEmail != null) {
            edtSiswamail.setText(prefillEmail);
        }

        // Keep your original click listener
        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        // Get input values
        String name = edtName.getText().toString().trim();
        String email = edtSiswamail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@siswa.um.edu.my")) {
            Toast.makeText(this, "Please use your Siswamail address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnCreateAccount.setEnabled(false);

        // Firebase - Create user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Save user data to Firestore
                            saveUserToFirestore(user.getUid(), name, email);
                        }

                    } else {
                        // Sign up failed
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        btnCreateAccount.setEnabled(true);

                        String errorMessage = "Sign up failed. Please try again.";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        Toast.makeText(SignupActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", name);
        userData.put("email", email);
        userData.put("university", "University of Malaya");
        userData.put("studentId", "");
        userData.put("phoneNumber", "");
        userData.put("address", "");
        userData.put("profileImageUrl", "");
        userData.put("rating", 0.0);
        userData.put("totalRatings", 0);
        userData.put("isVerified", false);
        userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        userData.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Save to Firestore
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created in Firestore");
                    btnCreateAccount.setEnabled(true);

                    Toast.makeText(SignupActivity.this,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Send email verification (optional but recommended)
                    sendEmailVerification(email);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user profile", e);
                    btnCreateAccount.setEnabled(true);
                    Toast.makeText(SignupActivity.this,
                            "Account created but profile save failed. Please try logging in.",
                            Toast.LENGTH_LONG).show();

                    // Still go to verification screen
                    goToEmailVerification(email);
                });
    }

    private void sendEmailVerification(String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email verification sent");
                        } else {
                            Log.w(TAG, "Failed to send verification email", task.getException());
                        }
                        // Go to email verification screen regardless
                        goToEmailVerification(email);
                    });
        } else {
            goToEmailVerification(email);
        }
    }

    private void goToEmailVerification(String email) {
        // Keep your original navigation
        Intent i = new Intent(this, EmailVerificationActivity.class);
        i.putExtra("email", email);
        startActivity(i);
        finish();
    }
}
