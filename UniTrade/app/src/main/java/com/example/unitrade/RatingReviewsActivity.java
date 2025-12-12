

package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

    public class RatingReviewsActivity extends BaseActivity {

        private TabLayout tabReviews;
        private ViewPager2 viewPagerReviews;

        private TextView txtOverall;
        private TextView txtUserRating;
        private TextView txtSellerRating;

        private User user;
        private List<Review> allReviews = new ArrayList<>();

        private ActivityResultLauncher<Intent> reviewLauncher;

        // =====================================================
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            // -------------------------------------------------
            // HANDLE REVIEW RESULT
            // -------------------------------------------------
            reviewLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            Review newReview =
                                    result.getData().getParcelableExtra("new_review");

                            if (newReview != null) {
                                allReviews.add(0, newReview);
                                refreshRatingsAndUser();
                            }
                        }
                    }
            );

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rating_reviews);

            // -------------------------------------------------
            // TOOLBAR
            // -------------------------------------------------
            MaterialToolbar toolbar = findViewById(R.id.appBarRatingReviews);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
            tintToolbarOverflow(toolbar);

            // -------------------------------------------------
            // LOAD USER
            // -------------------------------------------------
            user = getIntent().getParcelableExtra("user_to_view");
            boolean hideFab = getIntent().getBooleanExtra("hide_fab", false);

            if (user == null) {
                finish();
                return;
            }

            // -------------------------------------------------
            // PROFILE HEADER
            // -------------------------------------------------
            ImageView imgUserProfile = findViewById(R.id.imgUserProfile);
            TextView txtUsername = findViewById(R.id.txtUsername);
            TextView txtLastSeen = findViewById(R.id.txtLastSeen);
            TextView txtUserDescription = findViewById(R.id.txtUserDescription);

            txtUsername.setText(user.getUsername());
            txtLastSeen.setText(user.getLastSeenString());
            txtUserDescription.setText(user.getBio());

            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(imgUserProfile);

            // -------------------------------------------------
            // RATING SUMMARY
            // -------------------------------------------------
            txtOverall = findViewById(R.id.txtOverallRating);
            txtUserRating = findViewById(R.id.txtUserRating);
            txtSellerRating = findViewById(R.id.txtSellerRating);

            // -------------------------------------------------
            // LOAD MOCK REVIEWS
            // -------------------------------------------------
            allReviews.clear();
            allReviews.addAll(
                    SampleData.generateMockReviewsForUser(this, user)
            );

            // -------------------------------------------------
            // VIEWPAGER
            // -------------------------------------------------
            tabReviews = findViewById(R.id.tabReviews);
            viewPagerReviews = findViewById(R.id.viewPagerReviews);

            setupViewPager();

            // -------------------------------------------------
            // WRITE REVIEW FAB
            // -------------------------------------------------
            FloatingActionButton btnWriteReview = findViewById(R.id.btnWriteReview);

            if (hideFab) {
                btnWriteReview.setVisibility(View.GONE);
            } else {
                btnWriteReview.setOnClickListener(v -> {
                    Intent intent = new Intent(this, RateUserActivity.class);
                    intent.putExtra("user_to_view", user);
                    reviewLauncher.launch(intent);
                });
            }

            // -------------------------------------------------
            // INITIAL CALCULATION
            // -------------------------------------------------
            refreshRatingsAndUser();
        }

        // =====================================================
        // VIEWPAGER SETUP
        // =====================================================
        private void setupViewPager() {

            ReviewsPagerAdapter adapter =
                    new ReviewsPagerAdapter(this, user, allReviews);

            viewPagerReviews.setAdapter(adapter);

            new TabLayoutMediator(tabReviews, viewPagerReviews,
                    (tab, pos) -> {
                        if (pos == 0) tab.setText("All");
                        else if (pos == 1) tab.setText("User");
                        else tab.setText("Seller");
                    }
            ).attach();
        }

        // =====================================================
        // RATING + USER UPDATE
        // =====================================================
        private void refreshRatingsAndUser() {

            int userCount = 0;
            int sellerCount = 0;
            double userSum = 0;
            double sellerSum = 0;

            for (Review r : allReviews) {

                if ("user".equals(r.getType())) {
                    userCount++;
                    userSum += r.getRating();
                }

                if ("seller".equals(r.getType())) {
                    sellerCount++;
                    sellerSum += r.getRating();
                }
            }

            double userAvg = userCount == 0 ? 0 : userSum / userCount;
            double sellerAvg = sellerCount == 0 ? 0 : sellerSum / sellerCount;

            // ✅ Update user (overall auto-updates)
            user.setUserRating(userAvg);
            user.setSellerRating(sellerAvg);

            // ✅ Persist
            SampleData.updateUser(this, user);

            // ✅ Update UI from USER model
            txtUserRating.setText(String.format("%.1f", user.getUserRating()));
            txtSellerRating.setText(String.format("%.1f", user.getSellerRating()));
            txtOverall.setText(String.format("%.1f", user.getOverallRating()));

            // Refresh tabs
            setupViewPager();
        }
    }


