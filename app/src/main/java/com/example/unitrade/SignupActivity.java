package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    // EmailJS configuration
    private static final String EMAILJS_SERVICE_ID = "service_2sk1z1h";
    private static final String EMAILJS_TEMPLATE_ID = "template_t6djzkb";
    private static final String EMAILJS_PUBLIC_KEY = "xRkUe1XICuj_jAUyf";

    private static final long OTP_COOLDOWN_MS = 30_000; // 30 seconds

    private EditText edtName;
    private EditText edtSiswamail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edtName = findViewById(R.id.edtName);
        edtSiswamail = findViewById(R.id.edtSiswamail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnSignUp);

        btnCreateAccount.setOnClickListener(v -> startOtpFlow());
    }

    private void startOtpFlow() {
        String name = edtName.getText().toString().trim();
        String email = edtSiswamail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@siswa.um.edu.my")) {
            Toast.makeText(this, "Please use your Siswamail address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("OTP_PREF", MODE_PRIVATE);
        long lastOtpTime = prefs.getLong("otp_time", 0);

        if (System.currentTimeMillis() - lastOtpTime < OTP_COOLDOWN_MS) {
            Toast.makeText(
                    this,
                    "Please wait before requesting another code",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnCreateAccount.setEnabled(false);

        // Generate 6-digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // Save temporary signup data
        prefs.edit()
                .putString("otp", otp)
                .putLong("otp_time", System.currentTimeMillis())
                .putString("name", name)
                .putString("email", email)
                .putString("password", password)
                .apply();

        sendOtpEmail(email, otp);
    }

    private void sendOtpEmail(String email, String otp) {
        OkHttpClient client = new OkHttpClient();

        String json =
                "{"
                        + "\"service_id\":\"" + EMAILJS_SERVICE_ID + "\","
                        + "\"template_id\":\"" + EMAILJS_TEMPLATE_ID + "\","
                        + "\"user_id\":\"" + EMAILJS_PUBLIC_KEY + "\","
                        + "\"template_params\":{"
                        + "\"email\":\"" + email + "\","
                        + "\"passcode\":\"" + otp + "\","
                        + "\"time\":\"15 minutes\""
                        + "}"
                        + "}";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json
        );

        Request request = new Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "OTP email failed", e);

                runOnUiThread(() -> {
                    btnCreateAccount.setEnabled(true);
                    Toast.makeText(
                            SignupActivity.this,
                            "Failed to send verification code. Try again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseBody =
                            response.body() != null ? response.body().string() : "empty";

                    Log.d(TAG, "EmailJS response code: " + response.code());
                    Log.d(TAG, "EmailJS response body: " + responseBody);

                } catch (IOException e) {
                    Log.e(TAG, "Error reading response", e);
                }

                runOnUiThread(() -> {
                    btnCreateAccount.setEnabled(true);

                    if (!response.isSuccessful()) {
                        Toast.makeText(
                                SignupActivity.this,
                                "Failed to send verification code. Please retry.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    Toast.makeText(
                            SignupActivity.this,
                            "Verification code sent to email",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            SignupActivity.this,
                            EmailVerificationActivity.class
                    );
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}
