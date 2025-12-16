package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String TAG = "EmailVerification";
    private static final long OTP_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes

    private String currentEmail;
    private TextView txtEmail;
    private EditText edtOtp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        currentEmail = getIntent().getStringExtra("email");

        txtEmail = findViewById(R.id.txtEmail);
        edtOtp = findViewById(R.id.edtOtp);
        Button btnChangeEmail = findViewById(R.id.btnChangeEmail);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        txtEmail.setText(currentEmail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnChangeEmail.setOnClickListener(v -> {
            Intent i = new Intent(EmailVerificationActivity.this, SignupActivity.class);
            i.putExtra("email_prefill", currentEmail);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        btnConfirm.setOnClickListener(v -> verifyOtpAndCreateAccount());
    }

    private void verifyOtpAndCreateAccount() {
        String inputOtp = edtOtp.getText().toString().trim();

        if (TextUtils.isEmpty(inputOtp)) {
            Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("OTP_PREF", MODE_PRIVATE);
        String savedOtp = prefs.getString("otp", "");
        long otpTime = prefs.getLong("otp_time", 0);

        if (System.currentTimeMillis() - otpTime > OTP_EXPIRY_MS) {
            Toast.makeText(this, "Verification code expired", Toast.LENGTH_LONG).show();
            return;
        }

        if (!inputOtp.equals(savedOtp)) {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = prefs.getString("email", "");
        String password = prefs.getString("password", "");
        String name = prefs.getString("name", "");

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Signup data missing. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        createFirebaseAccount(email, password, name);
    }

    private void createFirebaseAccount(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserToFirestore(user.getUid(), name, email);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase account creation failed", e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", name);
        userData.put("email", email);
        userData.put("university", "University of Malaya");
        userData.put("isVerified", true);
        userData.put("createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    clearOtpData();
                    showEmailVerifiedPopup(); // SAME UI POPUP
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Account created but profile save failed",
                                Toast.LENGTH_LONG).show()
                );
    }

    private void clearOtpData() {
        getSharedPreferences("OTP_PREF", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private void showEmailVerifiedPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_email_verified, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Button btnContinue = view.findViewById(R.id.btnBackToLogin);
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}
