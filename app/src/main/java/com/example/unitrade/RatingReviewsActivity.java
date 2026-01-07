package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingReviewsActivity extends AppCompatActivity {

    private TabLayout tabReviews;
    private ViewPager2 viewPagerReviews;

    private TextView txtOverall, txtUserRating, txtSellerRating, txtSellerRatingCount, txtUserRatingCount, txtOverallRatingCount;


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
        txtUserRatingCount = findViewById(R.id.txtUserRatingCount);
        txtSellerRatingCount = findViewById(R.id.txtSellerRatingCount);
        txtOverallRatingCount = findViewById(R.id.txtOverallCount);


        tabReviews = findViewById(R.id.tabReviews);
        viewPagerReviews = findViewById(R.id.viewPagerReviews);

        ImageView imgUserProfile = findViewById(R.id.imgUserProfile);
        TextView txtUsername = findViewById(R.id.txtUsername);

        TextView txtUserDescription = findViewById(R.id.txtUserDescription);
        FloatingActionButton btnWriteReview = findViewById(R.id.btnWriteReview);

        // ---------------- Load User ----------------
        String userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUser(userId, imgUserProfile, txtUsername, txtUserDescription, btnWriteReview);

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
                            db.collection("reviews")
                                    .add(newReview)
                                    .addOnSuccessListener(docRef -> {
                                        newReview.setId(docRef.getId()); // optional
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save review", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    }
                });
    }

    protected void onResume() {
        super.onResume();

        if (user != null) {
            loadReviewsFromDatabase();
        }
    }

    private void loadUser(String userId, ImageView imgProfile, TextView txtUsername,
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
                    txtUsername.setText(user.getFullName());

                    txtBio.setText(user.getBio());
                    txtOverallRatingCount.setText(user.getOverallRatingCount() + " ratings");
                    txtUserRatingCount.setText(user.getUserRatingCount() + " ratings");
                    txtSellerRatingCount.setText(user.getSellerRatingCount() + " ratings");

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
        pagerAdapter = new ReviewsPagerAdapter(
                this,
                user,
                allReviews,
                review -> showReportDialog(review)
        );
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

    private void showReportDialog(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Report review")
                .setMessage("Why are you reporting this review?")
                .setItems(
                        new String[]{"Spam", "Abusive content", "Fake review", "Other"},
                        (dialog, which) -> saveReport(review, which)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onReportClick(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Report Review")
                .setMessage("Are you sure you want to report this review?")
                .setPositiveButton("Report", (dialog, which) -> {

                    FirebaseFirestore.getInstance()
                            .collection("reported_reviews")
                            .add(new Report(
                                    review.getId(),
                                    UserSession.get().getId(),
                                    review.getTargetUserId(),
                                    "Inappropriate content",
                                    System.currentTimeMillis()
                            ))
                            .addOnSuccessListener(r ->
                                    Toast.makeText(this, "Review reported", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to report review", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void testReportDatabase() {
        FirebaseFirestore.getInstance()
                .collection("review_reports") // IMPORTANT: match collection name
                .limit(5)
                .get()
                .addOnSuccessListener(query -> {
                    Log.d("REPORT_TEST", "Reports found: " + query.size());

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Log.d("REPORT_TEST", doc.getId() + " -> " + doc.getData());
                    }

                    Toast.makeText(
                            this,
                            "Report test OK, found " + query.size(),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("REPORT_TEST", "FAILED", e);
                    Toast.makeText(
                            this,
                            "Report test FAILED: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void saveReport(Review review, int reasonIndex) {
        String[] reasons = {"Spam", "Abusive", "Fake", "Other"};

        Map<String, Object> report = new HashMap<>();
        report.put("reviewId", review.getId());
        report.put("reportedUserId", user.getId());
        report.put("reporterId", UserSession.get().getId());
        report.put("reason", reasons[reasonIndex]);
        report.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("review_reports")
                .add(report)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
                    testReportDatabase();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
                );

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

        int overallCount = userCount + sellerCount;

        double userAvg = userCount == 0 ? 0 : userSum / userCount;
        double sellerAvg = sellerCount == 0 ? 0 : sellerSum / sellerCount;

        user.setUserRating(userAvg);
        user.setSellerRating(sellerAvg);
        user.setUserRatingCount(userCount);
        user.setSellerRatingCount(sellerCount);



        // Persist in Firestore
        db.collection("users").document(user.getId())
                .update(
                        "userRating", userAvg,
                        "sellerRating", sellerAvg,
                        "userRatingCount", userCount,
                        "sellerRatingCount", sellerCount,
                        "overallRatingCount", overallCount
                );

        // Update UI
        txtUserRating.setText(String.format("%.1f", userAvg));
        txtSellerRating.setText(String.format("%.1f", sellerAvg));
        txtOverall.setText(String.format("%.1f", user.getOverallRating()));
        txtUserRatingCount.setText(userCount + " ratings");
        txtSellerRatingCount.setText(sellerCount + " ratings");
        txtOverallRatingCount.setText(overallCount + " ratings");
    }
}
