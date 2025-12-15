package com.example.unitrade;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LoginHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LoginHistoryAdapter adapter;
    private List<LoginHistoryItem> loginHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);

        Toolbar toolbar = findViewById(R.id.appBarLoginHistory);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Login History");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewLoginHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadLoginHistory();

        adapter = new LoginHistoryAdapter(loginHistory);
        recyclerView.setAdapter(adapter);
    }

    private void loadLoginHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String jsonLoginHistory = sharedPreferences.getString("login_history", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LoginHistoryItem>>() {}.getType();
        loginHistory = gson.fromJson(jsonLoginHistory, type);

        if (loginHistory == null) {
            loginHistory = new ArrayList<>();
        }
    }
}
