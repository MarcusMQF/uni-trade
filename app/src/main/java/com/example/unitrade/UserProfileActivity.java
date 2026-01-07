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
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserProfileActivity extends BaseActivity {

    private User viewedUser;
    private Chip chipBlockUser;
    private boolean isBlocked = false;
    private String currentUserId;

    private ImageView imgProfile;
    private TextView txtUsername, txtUserDescription, txtShowMore, txtViewMoreListings, txtUserAddress;
    private TextView txtOverallRating, txtOverallCount, txtUserRating, txtUserRatingCount, txtSellerRating,
            txtSellerRatingCount;

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
                        Toast.LENGTH_SHORT).show();
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
            if (txtUserDescription == null)
                return;
            int lineCount = txtUserDescription.getLineCount();

            if (lineCount > 3) {
                txtUserDescription.setMaxLines(3);
                txtUserDescription.setEllipsize(TextUtils.TruncateAt.END);
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
                    txtUserDescription.setMaxLines(3);
                    txtUserDescription.setEllipsize(TextUtils.TruncateAt.END);
                    txtShowFullDescription.setText("show more ▼");
                } else {
                    txtUserDescription.setMaxLines(Integer.MAX_VALUE);
                    txtUserDescription.setEllipsize(null);
                    txtShowFullDescription.setText("show less ▲");
                }
                expanded = !expanded;
            }
        });
    }

    private void expandDescription() {
        TextView txtShowMore = findViewById(R.id.txtShowFullDescription);
        TextView txtUserDescription = findViewById(R.id.txtUserDescription);
        txtUserDescription.setMaxLines(Integer.MAX_VALUE);
        txtShowMore.setText("show less ▲");
        txtShowMore.setOnClickListener(v -> collapseDescription());
    }

    private void collapseDescription() {
        TextView txtShowMore = findViewById(R.id.txtShowFullDescription);
        TextView txtUserDescription = findViewById(R.id.txtUserDescription);
        txtUserDescription.setMaxLines(MAX_DESCRIPTION_LINES);
        txtShowMore.setText("show more ▼");
        txtShowMore.setOnClickListener(v -> expandDescription());
    }

    private void bindViews() {
        imgProfile = findViewById(R.id.imgProfile);
        txtUsername = findViewById(R.id.txtUsername);
        txtUserDescription = findViewById(R.id.txtUserDescription);
        txtUserAddress = findViewById(R.id.txtUserAddress);
        txtViewMoreListings = findViewById(R.id.txtViewMoreListings);
        txtShowMore = findViewById(R.id.txtShowFullDescription);
        rvListings = findViewById(R.id.rvProfileListings);
        btnViewReviews = findViewById(R.id.btnViewReviews);

        // Rating views
        txtOverallRating = findViewById(R.id.txtOverallRating);
        txtOverallCount = findViewById(R.id.txtOverallCount);
        txtUserRating = findViewById(R.id.txtUserRating);
        txtUserRatingCount = findViewById(R.id.txtUserRatingCount);
        txtSellerRating = findViewById(R.id.txtSellerRating);
        txtSellerRatingCount = findViewById(R.id.txtSellerRatingCount);

        //block button
        chipBlockUser = findViewById(R.id.chipBlockUser);
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    private void showUserData() {
        txtUsername.setText(viewedUser.getFullName());
        txtUserDescription.setText(viewedUser.getBio());

        // Fix Address Display
        String address = viewedUser.getAddress();
        if (address != null && !address.trim().isEmpty()) {
            txtUserAddress.setText("Address: " + address);
            txtUserAddress.setVisibility(View.VISIBLE);
        } else {
            txtUserAddress.setVisibility(View.GONE);
        }

        // Fix Ratings Display
        txtOverallRating.setText(String.format("%.1f", viewedUser.getOverallRating()));
        txtUserRating.setText(String.format("%.1f", viewedUser.getUserRating()));
        txtSellerRating.setText(String.format("%.1f", viewedUser.getSellerRating()));


        int userCount = viewedUser.getUserRatingCount();
        int sellerCount = viewedUser.getSellerRatingCount();
        int overallCount = viewedUser.getOverallRatingCount();
        txtUserRatingCount.setText(userCount + " rating" + (userCount == 1 ? "" : "s"));
        txtSellerRatingCount.setText(sellerCount + " rating" + (sellerCount == 1 ? "" : "s"));
        txtOverallCount.setText(overallCount + " rating" + (overallCount == 1 ? "" : "s"));

        //block button setup
        checkBlockStatus();
        setupBlockButton();

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
                        // Show all products directly
                        productAdapter = new UserProductsAdapter(UserProfileActivity.this, userProducts);
                        rvListings.setAdapter(productAdapter);
                        txtViewMoreListings.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(
                                UserProfileActivity.this,
                                "Failed to load listings",
                                Toast.LENGTH_SHORT).show();
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

        if (viewedUser == null)
            return;

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
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //block button
    private void checkBlockStatus() {
        if (currentUserId == null || viewedUser == null) return;

        String blockId = currentUserId + "_" + viewedUser.getId();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("blocks").document(blockId)
                .get().addOnSuccessListener(doc -> {
                    isBlocked = doc.exists();
                    updateBlockChipUI();
                });
    }

    private void updateBlockChipUI() {
        if (isBlocked) {
            chipBlockUser.setText("Blocked");
            // Using ColorStateList to ensure the color applies to the Material Chip correctly
            chipBlockUser.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_red_dark)));
        } else {
            chipBlockUser.setText("Block");
            chipBlockUser.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_green_dark)));
        }
    }

    private void setupBlockButton() {
        chipBlockUser.setOnClickListener(v -> {
            if (currentUserId == null || viewedUser == null) return;

            String blockId = currentUserId + "_" + viewedUser.getId();
            if (!isBlocked) {
                // Perform Block
                java.util.Map<String, Object> blockData = new java.util.HashMap<>();
                blockData.put("blockerId", currentUserId);
                blockData.put("blockedId", viewedUser.getId());
                blockData.put("timestamp", System.currentTimeMillis());

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("blocks").document(blockId)
                        .set(blockData).addOnSuccessListener(aVoid -> {
                            isBlocked = true;
                            updateBlockChipUI();
                            Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Unblock logic
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("blocks").document(blockId)
                        .delete().addOnSuccessListener(aVoid -> {
                            isBlocked = false;
                            updateBlockChipUI();
                            Toast.makeText(this, "User unblocked", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }


}
