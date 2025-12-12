package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.List;



public class RatingReviewsActivity extends BaseActivity{

    private TabLayout tabReviews;
    private ViewPager2 viewPagerReviews;

    private User user;
    private List<Review> allReviews;
    private ActivityResultLauncher<Intent> reviewLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        reviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        Review newReview = result.getData().getParcelableExtra("new_review");

                        if (newReview != null) {
                            allReviews.add(0, newReview);   // add to top of list
                            refreshReviewUI();
                        }
                    }
                }
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_reviews);

        FloatingActionButton btnWriteReview = findViewById(R.id.btnWriteReview);
        boolean hideFab = getIntent().getBooleanExtra("hide_fab", false);

        if (hideFab) {
            btnWriteReview.setVisibility(View.GONE);   // 👈 hide the floating button
        } else {
            btnWriteReview.show();   // visible normally when viewing others
        }


        MaterialToolbar toolbar = findViewById(R.id.appBarRatingReviews);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tintToolbarOverflow(toolbar);


        user = getIntent().getParcelableExtra("user_to_view");


        if (user == null) {
            finish(); // prevent crash
            return;
        }

        // -------------------------------------------------------
        // LOAD USER PROFILE SECTION (name, image, last seen, desc)
        // -------------------------------------------------------

        ImageView imgUserProfile = findViewById(R.id.imgUserProfile);
        TextView txtUsername = findViewById(R.id.txtUsername);
        TextView txtLastSeen = findViewById(R.id.txtLastSeen);
        TextView txtUserDescription = findViewById(R.id.txtUserDescription);


        // User name
        txtUsername.setText(user.getUsername());

        // Profile image
        Glide.with(this)
                .load(user.getProfileImageUrl())  // works with android.resource:// URIs
                .circleCrop()
                .into(imgUserProfile);

        // Last seen text
        txtLastSeen.setText(user.getLastSeenString());  // you already have lastSeenMillis in User model

        // Description (bio)
        txtUserDescription.setText(user.getBio());


        // Load reviews
        allReviews = SampleData.generateReviewsForUser(this, user);

        tabReviews = findViewById(R.id.tabReviews);
        viewPagerReviews = findViewById(R.id.viewPagerReviews);

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



        btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(this, RateUserActivity.class);
            intent.putExtra("user_to_view", user);
            reviewLauncher.launch(intent);
        });

        // Update summary text
        TextView txtOverall = findViewById(R.id.txtOverallRating);
        TextView txtUserRating = findViewById(R.id.txtUserRating);
        TextView txtSellerRating = findViewById(R.id.txtSellerRating);

        txtOverall.setText(String.format("%.1f", user.getOverallRating()));
        txtUserRating.setText(String.format("%.1f", user.getUserRating()));
        txtSellerRating.setText(String.format("%.1f", user.getSellerRating()));
    }

    private void refreshReviewUI() {

        // Recalculate rating averages
        double totalRating = 0;
        int userCount = 0;
        int sellerCount = 0;

        for (Review r : allReviews) {
            totalRating += r.getRating();

            if (r.getType().equals("user")) userCount++;
            if (r.getType().equals("seller")) sellerCount++;
        }

        double overall = totalRating / allReviews.size();
        double userAvg = userCount == 0 ? 0 : userCount / (double) userCount;
        double sellerAvg = sellerCount == 0 ? 0 : sellerCount / (double) sellerCount;

        ((TextView) findViewById(R.id.txtOverallRating))
                .setText(String.format("%.1f", overall));

        ((TextView) findViewById(R.id.txtUserRating))
                .setText(String.format("%.1f", userAvg));

        ((TextView) findViewById(R.id.txtSellerRating))
                .setText(String.format("%.1f", sellerAvg));

        // Refresh ViewPager contents
        viewPagerReviews.setAdapter(new ReviewsPagerAdapter(this, user, allReviews));
    }

}
