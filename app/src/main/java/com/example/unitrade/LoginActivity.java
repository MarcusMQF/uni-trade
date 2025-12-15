package com.example.unitrade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        btnLogin.setOnClickListener(v -> attemptLogin());
        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        // TODO: Firebase login
        saveLoginTime();
        String email = edtEmail.getText().toString().trim();

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("email", email);
        startActivity(i);
    }

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
