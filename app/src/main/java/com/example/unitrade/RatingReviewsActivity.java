package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RatingReviewsActivity extends AppCompatActivity {

    private TabLayout tabReviews;
    private ViewPager2 viewPagerReviews;

    private TextView txtOverall;
    private TextView txtUserRating;
    private TextView txtSellerRating;

    private User user;
    private final List<Review> allReviews = new ArrayList<>();
    private ReviewsPagerAdapter pagerAdapter;

    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> reviewLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_reviews);

        db = FirebaseFirestore.getInstance();

        // ---------------- Toolbar ----------------
        MaterialToolbar toolbar = findViewById(R.id.appBarRatingReviews);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ---------------- UI Elements ----------------
        txtOverall = findViewById(R.id.txtOverallRating);
        txtUserRating = findViewById(R.id.txtUserRating);
        txtSellerRating = findViewById(R.id.txtSellerRating);

        tabReviews = findViewById(R.id.tabReviews);
        viewPagerReviews = findViewById(R.id.viewPagerReviews);

        ImageView imgUserProfile = findViewById(R.id.imgUserProfile);
        TextView txtUsername = findViewById(R.id.txtUsername);
        TextView txtLastSeen = findViewById(R.id.txtLastSeen);
        TextView txtUserDescription = findViewById(R.id.txtUserDescription);
        FloatingActionButton btnWriteReview = findViewById(R.id.btnWriteReview);

        // ---------------- Load User ----------------
        String userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUser(userId, imgUserProfile, txtUsername, txtLastSeen, txtUserDescription, btnWriteReview);

        // ---------------- Handle New Review ----------------
        reviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Review newReview = result.getData().getParcelableExtra("new_review");
                        if (newReview != null) {
                            allReviews.add(0, newReview);
                            pagerAdapter.notifyDataSetChanged();
                            refreshRatingsAndUser();

                            // Save to Firestore
                            db.collection("reviews").document(newReview.getId())
                                    .set(newReview)
                                    .addOnFailureListener(e -> Toast
                                            .makeText(this, "Failed to save review", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void loadUser(String userId, ImageView imgProfile, TextView txtUsername, TextView txtLastSeen,
            TextView txtBio, FloatingActionButton btnWriteReview) {

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    try {
                        user = doc.toObject(User.class);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        finish();
                        return;
                    }

                    if (user == null) {
                        Toast.makeText(this, "User object is null", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Force set ID if missing
                    user.setId(doc.getId());

                    // Set header UI
                    txtUsername.setText(user.getUsername());
                    txtLastSeen.setText(user.getLastSeenString());
                    txtBio.setText(user.getBio());

                    Glide.with(this)
                            .load(user.getProfileImageUrl())
                            .signature(new ObjectKey(user.getProfileImageVersion()))
                            .circleCrop()
                            .into(imgProfile);

                    // Load reviews
                    loadReviewsFromDatabase();

                    // FAB action
                    boolean hideFab = getIntent().getBooleanExtra("hide_fab", false);
                    if (hideFab)
                        btnWriteReview.setVisibility(View.GONE);
                    else
                        btnWriteReview.setOnClickListener(v -> {
                            Intent intent = new Intent(this, RateUserActivity.class);
                            intent.putExtra("user_id", user.getId());
                            reviewLauncher.launch(intent);
                        });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadReviewsFromDatabase() {
        db.collection("reviews")
                .whereEqualTo("targetUserId", user.getId())
                .get()
                .addOnSuccessListener(query -> {
                    allReviews.clear();
                    for (var doc : query.getDocuments()) {
                        try {
                            Review r = doc.toObject(Review.class);
                            if (r != null) {
                                allReviews.add(r);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Skip malformed review
                        }
                    }
                    setupViewPager();
                    refreshRatingsAndUser();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupViewPager() {
        pagerAdapter = new ReviewsPagerAdapter(this, user, allReviews);
        viewPagerReviews.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabReviews, viewPagerReviews, (tab, pos) -> {
            if (pos == 0)
                tab.setText("All");
            else if (pos == 1)
                tab.setText("User");
            else
                tab.setText("Seller");
        }).attach();
    }

    private void refreshRatingsAndUser() {
        double userSum = 0, sellerSum = 0;
        int userCount = 0, sellerCount = 0;

        for (Review r : allReviews) {
            String type = r.getType();
            if ("user".equals(type)) {
                userSum += r.getRating();
                userCount++;
            }
            if ("seller".equals(type)) {
                sellerSum += r.getRating();
                sellerCount++;
            }
        }

        double userAvg = userCount == 0 ? 0 : userSum / userCount;
        double sellerAvg = sellerCount == 0 ? 0 : sellerSum / sellerCount;

        user.setUserRating(userAvg);
        user.setSellerRating(sellerAvg);

        // Persist in Firestore
        db.collection("users").document(user.getId())
                .update("userRating", userAvg, "sellerRating", sellerAvg)
                .addOnFailureListener(e -> {
                });

        // Update UI
        txtUserRating.setText(String.format("%.1f", userAvg));
        txtSellerRating.setText(String.format("%.1f", sellerAvg));
        txtOverall.setText(String.format("%.1f", user.getOverallRating()));
    }
}
