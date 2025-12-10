package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText edtName, edtSiswamail, edtPassword, edtConfirmPassword;
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


        String prefillEmail = getIntent().getStringExtra("email_prefill");
        if (prefillEmail != null) {
            edtSiswamail.setText(prefillEmail);
        }

        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email = edtSiswamail.getText().toString().trim();

        // TODO: Firebase create user
        Intent i = new Intent(this, EmailVerificationActivity.class);
        i.putExtra("email", email);
        startActivity(i);
    }
}
