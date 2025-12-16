package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtSignUp;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Keep your original UI initialization
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        // Keep your original click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is already logged in
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
            goToMainActivity(currentUser.getEmail());
        }
    }

    private void attemptLogin() {
        // Get input values
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable login button to prevent multiple clicks
        btnLogin.setEnabled(false);

        // Firebase Authentication - Sign In
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            btnLogin.setEnabled(true);

            if (task.isSuccessful()) {
                // Login success
                Log.d(TAG, "signInWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    Toast.makeText(LoginActivity.this,
                            "Login successful!",
                            Toast.LENGTH_SHORT).show();

                    // Save login time (keep your original method)
                    saveLoginTime();

                    // Go to MainActivity
                    goToMainActivity(user.getEmail());
                }

            } else {
                // Login failed
                Log.w(TAG, "signInWithEmail:failure", task.getException());

                String errorMessage = "Login failed. Please check your credentials.";
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage();
                }

                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMainActivity(String email) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("email", email);
        startActivity(i);
        finish();
    }

    // Keep your original saveLoginTime method exactly as it was
    private void saveLoginTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String jsonLoginHistory = sharedPreferences.getString("login_history", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LoginHistoryItem>>() {}.getType();
        List<LoginHistoryItem> loginHistory = gson.fromJson(jsonLoginHistory, type);

        if (loginHistory == null) {
            loginHistory = new ArrayList<>();
        }

        loginHistory.add(new LoginHistoryItem(System.currentTimeMillis()));

        String updatedJsonLoginHistory = gson.toJson(loginHistory);
        sharedPreferences.edit().putString("login_history", updatedJsonLoginHistory).apply();
    }
}
