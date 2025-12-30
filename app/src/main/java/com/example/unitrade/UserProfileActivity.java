package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class UserProfileActivity extends BaseActivity {

    private User viewedUser;

    private ImageView imgProfile;
    private TextView txtUsername, txtLastSeen, txtUserDescription, txtShowMore, txtViewMoreListings, txtUserAddress;
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        tintToolbarOverflow(toolbar);

        // Receive userId
        String userId = getIntent().getStringExtra("user_id");

        if (userId == null) {
            finish();
            return;
        }

        // 🔥 Firebase fetch
        UserRepository.getUserByUid(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                viewedUser = user;

                bindViews();
                showUserData();
                loadUserListings();
                setupViewReviewsButton();
                setupDescriptionExpand();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        UserProfileActivity.this,
                        "User not found",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        });
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
        txtUserAddress = findViewById(R.id.txtUserAddress);
        txtViewMoreListings = findViewById(R.id.txtViewMoreListings);
        txtShowMore = findViewById(R.id.txtShowFullDescription);
        rvListings = findViewById(R.id.rvProfileListings);
        btnViewReviews = findViewById(R.id.btnViewReviews);
    }

    private void showUserData() {
        txtUsername.setText(viewedUser.getUsername());
        txtLastSeen.setText("Last seen: " + viewedUser.getLastSeenString());
        txtUserDescription.setText(viewedUser.getBio());

        List<Address> address = viewedUser.getAddresses();
        if (address != null && !address.isEmpty() && !address.equals("No address set")) {
            txtUserAddress.setText("Address: " + address);
            txtUserAddress.setVisibility(View.VISIBLE);
        } else {
            txtUserAddress.setVisibility(View.GONE);
        }

        Glide.with(this)
                .load(viewedUser.getProfileImageUrl())
                .signature(new ObjectKey(viewedUser.getProfileImageVersion()))
                .circleCrop()
                .into(imgProfile);
    }

    private void loadUserListings() {

        rvListings.setLayoutManager(new GridLayoutManager(this, 2));

        ProductRepository.getActiveProductsByUser(
                viewedUser.getId(),
                new ProductRepository.ProductListCallback() {

                    @Override
                    public void onSuccess(List<Product> userProducts) {

                        if (userProducts.size() > MAX_PREVIEW_ITEMS) {

                            txtViewMoreListings.setText("show more ▼");

                            List<Product> previewList =
                                    userProducts.subList(0, MAX_PREVIEW_ITEMS);

                            productAdapter =
                                    new UserProductsAdapter(UserProfileActivity.this, previewList);
                            rvListings.setAdapter(productAdapter);

                            txtViewMoreListings.setVisibility(View.VISIBLE);

                            txtViewMoreListings.setOnClickListener(v -> {
                                productAdapter =
                                        new UserProductsAdapter(
                                                UserProfileActivity.this,
                                                userProducts
                                        );
                                rvListings.setAdapter(productAdapter);

                                txtViewMoreListings.setText("show less ▲");
                                txtViewMoreListings.setOnClickListener(x -> loadUserListings());
                            });

                        } else {
                            productAdapter =
                                    new UserProductsAdapter(UserProfileActivity.this, userProducts);
                            rvListings.setAdapter(productAdapter);
                            txtViewMoreListings.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(
                                UserProfileActivity.this,
                                "Failed to load listings",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    private void setupViewReviewsButton() {
        btnViewReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, RatingReviewsActivity.class);
            intent.putExtra("user_id", viewedUser.getId());
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (viewedUser == null) return;

        UserRepository.getUserByUid(
                viewedUser.getId(),
                new UserRepository.UserCallback() {

                    @Override
                    public void onSuccess(User user) {
                        viewedUser = user;
                        showUserData();
                        loadUserListings();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(
                                UserProfileActivity.this,
                                "Failed to refresh profile",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

}
