package com.example.unitrade;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class RateUserActivity extends BaseActivity {

    private ImageView star1, star2, star3, star4, star5;
    private MaterialButton btnBuyer, btnSeller, btnSubmit;
    private EditText edtReview;

    private int rating = 0;
    private String ratingRole = "";

    private User targetUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);

        db = FirebaseFirestore.getInstance();

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.appBarRateUser);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        tintToolbarOverflow(toolbar);

        // Retrieve target user ID
        String userId = getIntent().getStringExtra("user_id");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch target user from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(this::onUserFetched)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onUserFetched(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        targetUser = doc.toObject(User.class);

        bindViews();
        setupStarListeners();
        setupRoleButtons();
        setupSubmitAction();
    }

    private void bindViews() {
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        btnBuyer = findViewById(R.id.btnBuyer);
        btnSeller = findViewById(R.id.btnSeller);

        edtReview = findViewById(R.id.edtReview);

        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupStarListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.star1)
                rating = 1;
            else if (id == R.id.star2)
                rating = 2;
            else if (id == R.id.star3)
                rating = 3;
            else if (id == R.id.star4)
                rating = 4;
            else if (id == R.id.star5)
                rating = 5;
            updateStarUI();
        };
        star1.setOnClickListener(listener);
        star2.setOnClickListener(listener);
        star3.setOnClickListener(listener);
        star4.setOnClickListener(listener);
        star5.setOnClickListener(listener);
    }

    private void updateStarUI() {
        ImageView[] stars = { star1, star2, star3, star4, star5 };
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void setupRoleButtons() {
        btnBuyer.setOnClickListener(v -> {
            ratingRole = "buyer";
            btnBuyer.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#00A06A")));
            btnSeller.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        });

        btnSeller.setOnClickListener(v -> {
            ratingRole = "seller";
            btnSeller.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#00A06A")));
            btnBuyer.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
        });
    }

    private void setupSubmitAction() {
        btnSubmit.setOnClickListener(v -> {
            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ratingRole.isEmpty()) {
                Toast.makeText(this, "Please choose a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String reviewText = edtReview.getText() != null ? edtReview.getText().toString() : "";
            User loggedInUser = UserSession.get(); // current logged-in user

            Review review = new Review(
                    "rev_" + System.currentTimeMillis(),
                    loggedInUser,
                    reviewText,
                    rating,
                    "Today",
                    ratingRole.equals("buyer") ? "user" : "seller");

            // Send back result
            Intent data = new Intent();
            data.putExtra("new_review", review);
            data.putExtra("user_id", targetUser.getId());
            setResult(RESULT_OK, data);

            Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
