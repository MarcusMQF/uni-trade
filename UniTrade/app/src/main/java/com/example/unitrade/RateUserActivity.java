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
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class RateUserActivity extends BaseActivity {

    private ImageView star1, star2, star3, star4, star5;
    private MaterialButton btnBuyer, btnSeller, btnSubmit;
    private EditText edtReview;

    private int rating = 0;      // 1–5 stars
    private String ratingRole = ""; // "buyer" or "seller"

    private User targetUser;     // The user being rated

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user);


        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.appBarRateUser);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tintToolbarOverflow(toolbar);

        // Retrieve user to rate
        targetUser = getIntent().getParcelableExtra("user_to_view");
        if (targetUser == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupStarListeners();
        setupRoleButtons();
        setupSubmitAction();
    }

    private void bindViews() {
        // Stars
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        // Role buttons
        btnBuyer = findViewById(R.id.btnBuyer);
        btnSeller = findViewById(R.id.btnSeller);

        // Review input
        edtReview = findViewById(R.id.edtReview);

        // Submit
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    // -------------------------------------------------------------------
    // ⭐ STAR RATING HANDLING
    // -------------------------------------------------------------------
    private void setupStarListeners() {

        View.OnClickListener listener = v -> {

            int id = v.getId();

            if (id == R.id.star1) rating = 1;
            else if (id == R.id.star2) rating = 2;
            else if (id == R.id.star3) rating = 3;
            else if (id == R.id.star4) rating = 4;
            else if (id == R.id.star5) rating = 5;

            updateStarUI();
        };

        star1.setOnClickListener(listener);
        star2.setOnClickListener(listener);
        star3.setOnClickListener(listener);
        star4.setOnClickListener(listener);
        star5.setOnClickListener(listener);
    }

    private void updateStarUI() {
        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < 5; i++) {
            if (i < rating)
                stars[i].setImageResource(R.drawable.ic_star_filled);
            else
                stars[i].setImageResource(R.drawable.ic_star_outline);
        }
    }

    // -------------------------------------------------------------------
    // 👤 ROLE SELECTION
    // -------------------------------------------------------------------
    private void setupRoleButtons() {

        btnBuyer.setOnClickListener(v -> {
            ratingRole = "buyer";

            btnBuyer.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#00A06A"))); // green border
            btnSeller.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // light gray
        });

        btnSeller.setOnClickListener(v -> {
            ratingRole = "seller";

            btnSeller.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#00A06A"))); // green
            btnBuyer.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // gray
        });
    }

    // -------------------------------------------------------------------
    // ✔ SUBMIT ACTION
    // -------------------------------------------------------------------
    private void setupSubmitAction() {
        btnSubmit.setOnClickListener(v -> {

            // Validate
            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ratingRole.isEmpty()) {
                Toast.makeText(this, "Please choose a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String reviewText = edtReview.getText() != null ? edtReview.getText().toString() : "";
            //Dumpy loggedin User
            User loggedinUser = SampleData.generateSampleUsers(this).get(0);
            // Create review object
            Review review = new Review(
                    "rev_" + System.currentTimeMillis(),
                    loggedinUser, // reviewer
                    reviewText,
                    rating,
                    "Today",
                    ratingRole.equals("buyer") ? "user" : "seller"
            );

            // Recalculate and update the user's rating
            List<Review> allReviews = SampleData.generateReviewsForUser(this, targetUser);
            allReviews.add(review);

            double totalRating = 0;
            for (Review r : allReviews) {
                totalRating += r.getRating();
            }
            double newOverallRating = totalRating / allReviews.size();
            targetUser.setOverallRating(newOverallRating);

            // Send back to RatingReviewsActivity
            Intent data = new Intent();
            data.putExtra("new_review", review);
            data.putExtra("updated_user", targetUser);
            setResult(RESULT_OK, data);

            Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();

            finish();
        });
    }
}
