package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserProfileActivity extends BaseActivity {

    private User viewedUser;

    private ImageView imgProfile;
    private TextView txtUsername, txtLastSeen, txtUserDescription, txtShowMore, txtViewMoreListings;
    private RecyclerView rvListings;
    private UserProductsAdapter productAdapter;
    private Button btnViewReviews;

    private static final int MAX_PREVIEW_ITEMS = 4;

    private static final int MAX_DESCRIPTION_LINES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);



        // Toolbar
        Toolbar toolbar = findViewById(R.id.appBarUserProfile);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        tintToolbarOverflow(toolbar);

        // Receive user
        viewedUser = getIntent().getParcelableExtra("user_to_view");
        if (viewedUser == null) {
            finish();
            return;
        }

        bindViews();
        showUserData();
        loadUserListings();
        setupViewReviewsButton();

        setupDescriptionExpand();

    }

    private void setupDescriptionExpand() {

        TextView txtUserDescription = findViewById(R.id.txtUserDescription);
        TextView txtShowFullDescription = findViewById(R.id.txtShowFullDescription);


        txtUserDescription.setMaxLines(Integer.MAX_VALUE);
        txtUserDescription.setEllipsize(null);

        txtUserDescription.post(() -> {

            int lineCount = txtUserDescription.getLineCount();


            if (lineCount > 3) {
                txtUserDescription.setMaxLines(3);
                txtUserDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                txtShowFullDescription.setVisibility(View.VISIBLE);
                txtShowFullDescription.setText("show more ▼");
            } else {
                txtShowFullDescription.setVisibility(View.GONE);
            }
        });


        txtShowFullDescription.setOnClickListener(new View.OnClickListener() {

            boolean expanded = false;

            @Override
            public void onClick(View v) {

                if (expanded) {
                    // collapse
                    txtUserDescription.setMaxLines(3);
                    txtUserDescription.setEllipsize(TextUtils.TruncateAt.END);
                    txtShowFullDescription.setText("show more ▼");
                } else {
                    // expand
                    txtUserDescription.setMaxLines(Integer.MAX_VALUE);
                    txtUserDescription.setEllipsize(null);
                    txtShowFullDescription.setText("show less ▲");
                }

                expanded = !expanded;
            }
        });
    }


    private void expandDescription() {
        txtUserDescription.setMaxLines(Integer.MAX_VALUE);
        txtShowMore.setText("show less ▲");

        txtShowMore.setOnClickListener(v -> collapseDescription());
    }

    private void collapseDescription() {
        txtUserDescription.setMaxLines(MAX_DESCRIPTION_LINES);
        txtShowMore.setText("show more ▼");

        txtShowMore.setOnClickListener(v -> expandDescription());
    }


    private void bindViews() {
        imgProfile = findViewById(R.id.imgProfile);
        txtUsername = findViewById(R.id.txtUsername);
        txtLastSeen = findViewById(R.id.txtLastSeen);
        txtUserDescription = findViewById(R.id.txtUserDescription);
        txtViewMoreListings = findViewById(R.id.txtViewMoreListings);
        txtShowMore = findViewById(R.id.txtShowFullDescription);
        rvListings = findViewById(R.id.rvProfileListings);
        btnViewReviews = findViewById(R.id.btnViewReviews);
    }

    private void showUserData() {
        txtUsername.setText(viewedUser.getUsername());
        txtLastSeen.setText("Last seen: " + viewedUser.getLastSeenString());
        txtUserDescription.setText(viewedUser.getBio());

        Glide.with(this)
                .load(viewedUser.getProfileImageUrl())
                .circleCrop()
                .into(imgProfile);
    }

    private void loadUserListings() {

        List<Product> all = SampleData.generateSampleProducts(this);
        List<Product> userProducts = Product.filterBySeller(all, viewedUser.getId());

        rvListings.setLayoutManager(new GridLayoutManager(this, 2));

        if (userProducts.size() > MAX_PREVIEW_ITEMS) {

            // Reset text when collapsed
            txtViewMoreListings.setText("show more ▼");

            // Show preview only
            List<Product> previewList = userProducts.subList(0, MAX_PREVIEW_ITEMS);

            productAdapter = new UserProductsAdapter(this, previewList);
            rvListings.setAdapter(productAdapter);

            txtViewMoreListings.setVisibility(View.VISIBLE);

            txtViewMoreListings.setOnClickListener(v -> {
                // Expand to full
                productAdapter = new UserProductsAdapter(UserProfileActivity.this, userProducts);
                rvListings.setAdapter(productAdapter);

                txtViewMoreListings.setText("show less ▲");
                txtViewMoreListings.setOnClickListener(x -> loadUserListings());
            });

        } else {

            // Show all listings
            productAdapter = new UserProductsAdapter(this, userProducts);
            rvListings.setAdapter(productAdapter);

            txtViewMoreListings.setVisibility(View.GONE);
        }
    }



    private void setupViewReviewsButton() {
        btnViewReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, RatingReviewsActivity.class);
            intent.putExtra("user_to_view", viewedUser);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}