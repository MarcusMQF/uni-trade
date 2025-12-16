package com.example.unitrade;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class ProductDetailActivity extends BaseActivity {

    private ViewPager2 viewPagerImages;
    private TabLayout tabDots;
    private ImageSliderAdapter imageSliderAdapter;

    private TextView txtItemTitle, txtPrice, txtLocation, txtConditionUsedDays, txtConditionTag;
    private TextView txtSellerName, txtRating, txtDescription;
    private ImageView imgSeller;

    private FloatingActionButton btnCart;
    private MaterialButton btnAddToCart, btnChatSeller, btnBuyNow, btnVisitProfile;
    private MaterialButton btnManageListing;

    private LinearLayout layoutBuyerButtons;
    private View bottomBar;

    private Product product;
    private User seller;

    private String currentUserId ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        setupToolbar();
        bindViews();

        currentUserId = UserSession.get().getId();


        String productId = getIntent().getStringExtra("product_id");
        product = SampleData.getProductById(this, productId);

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        seller = SampleData.getUserById(this, product.getSellerId());

        // -----------------------------------------------------
        // Initial UI
        // -----------------------------------------------------
        showProductInfo();
        setupSellerInfo();
        setupImageSlider();
        applyStatusUI();
        setupActions();

        if (seller != null && seller.getId().equals(currentUserId)) {
            showSellerBottomBar();
        } else {
            showBuyerBottomBar();
        }
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.appBarProductDetail);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Product Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void bindViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabDots = findViewById(R.id.tabDots);

        txtItemTitle = findViewById(R.id.txtItemTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtLocation = findViewById(R.id.txtLocation);
        txtConditionUsedDays = findViewById(R.id.txtConditionUsedDays);
        txtConditionTag = findViewById(R.id.txtConditionTag);
        txtSellerName = findViewById(R.id.txtSellerName);
        txtRating = findViewById(R.id.txtRating);
        txtDescription = findViewById(R.id.txtDescription);
        imgSeller = findViewById(R.id.imgSeller);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnChatSeller = findViewById(R.id.btnChatSeller);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnVisitProfile = findViewById(R.id.btnVisitProfile);
        btnManageListing = findViewById(R.id.btnManageListing);

        bottomBar = findViewById(R.id.bottomBar);
        layoutBuyerButtons = findViewById(R.id.layoutBuyerButtons);

        btnCart = findViewById(R.id.btnCart);

        //movable FAB
        View rootView = getWindow().getDecorView();
        View topView = findViewById(R.id.appBarProductDetail);


        MovableFabHelper mover = new MovableFabHelper();
        mover.enable(btnCart, rootView, topView, bottomBar);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ShoppingCartActivity.class);
            startActivity(intent);
        });
    }


    private void showProductInfo() {
        txtItemTitle.setText(product.getName());
        txtPrice.setText(AppSettings.formatPrice(this, product.getPrice()));
        txtLocation.setText(product.getLocation());
        txtDescription.setText(product.getDescription());

        txtConditionUsedDays.setText(formatUsage(product.getUsedDaysTotal()));
        txtConditionTag.setText(product.getCondition());

        applyConditionStyle(txtConditionTag, product.getCondition());
        applyUsageColor(txtConditionUsedDays, product.getUsedDaysTotal());
    }


    private void setupSellerInfo() {
        if (seller == null) {
            txtSellerName.setText("Unknown");
            return;
        }

        txtSellerName.setText(seller.getUsername());
        txtRating.setText(String.format("%.1f rating", seller.getOverallRating()));

        Glide.with(this)
                .load(seller.getProfileImageUrl())
                .signature(new ObjectKey(seller.getProfileImageVersion()))
                .circleCrop()
                .into(imgSeller);

        btnVisitProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, UserProfileActivity.class);
            i.putExtra("user_id", seller.getId());
            startActivity(i);
        });
    }


    private void setupImageSlider() {
        imageSliderAdapter = new ImageSliderAdapter(
                this,
                product.getImageUrls(),
                product.getImageVersion()
        );
        viewPagerImages.setAdapter(imageSliderAdapter);

        new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {})
                .attach();
    }


    private void showBuyerBottomBar() {
        bottomBar.setVisibility(View.VISIBLE);
        layoutBuyerButtons.setVisibility(View.VISIBLE);
        btnManageListing.setVisibility(View.GONE);
    }

    private void showSellerBottomBar() {
        bottomBar.setVisibility(View.VISIBLE);
        layoutBuyerButtons.setVisibility(View.GONE);
        btnManageListing.setVisibility(View.VISIBLE);

        btnManageListing.setOnClickListener(v -> showEditDialog());
    }


    private void showEditDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Manage Listing")
                .setItems(new String[]{"Edit Listing", "Cancel"}, (d, which) -> {
                    if (which == 0) openEditScreen();
                })
                .show();
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("editMode", true);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("openSellFragment", true);
        intent.putExtra("origin", "product_detail");
        startActivity(intent);
    }


    private void applyStatusUI() {
        String status = product.getStatus() == null ? "Available" : product.getStatus();

        switch (status.toLowerCase()) {
            case "available":
                btnBuyNow.setText("Buy Now");
                btnBuyNow.setEnabled(true);
                btnAddToCart.setVisibility(View.VISIBLE);
                break;

            case "sold":
            case "donated":
                btnBuyNow.setText(status);
                btnBuyNow.setEnabled(false);
                btnAddToCart.setVisibility(View.GONE);
                break;
        }
    }


    private void setupActions() {

        btnAddToCart.setOnClickListener(v -> {
            CartManager.addItem(this, product.getId());
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });

        btnBuyNow.setOnClickListener(v -> {
            if (!"available".equalsIgnoreCase(product.getStatus())) {
                Toast.makeText(this, "Item unavailable", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, CheckoutActivity.class);
            i.putExtra("product_id", product.getId());
            startActivity(i);
        });

        btnChatSeller.setOnClickListener(v -> {
            if (seller == null) return;

            Chat chat = new Chat(
                    seller.getId(),                 // userId
                    "Start conversation",           // last message
                    System.currentTimeMillis(),     // timestamp (long)
                    false                            // bookmarked
            );


            Intent i = new Intent(this, ConversationActivity.class);
            i.putExtra("chat", chat);
            startActivity(i);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Always reload product
        product = SampleData.getProductById(this, product.getId());
        if (product == null) return;



        // Refresh images
        imageSliderAdapter = new ImageSliderAdapter(
                this,
                product.getImageUrls(),
                product.getImageVersion()
        );
        viewPagerImages.setAdapter(imageSliderAdapter);
    }


    private String formatUsage(int totalDays) {
        if (totalDays <= 0) return "Unused";

        int years = totalDays / 365;
        int months = (totalDays % 365) / 30;
        int days = totalDays % 30;

        if (years > 0) return "Used (" + years + " years)";
        if (months > 0) return "Used (" + months + " months)";
        return "Used (" + days + " days)";
    }

    private void applyConditionStyle(TextView tag, String cond) {
        int bg, text;

        switch (cond) {
            case "Good":
                bg = Color.parseColor("#C8E6C9");
                text = Color.parseColor("#1B5E20");
                break;
            case "Fair":
                bg = Color.parseColor("#FFE0B2");
                text = Color.parseColor("#E65100");
                break;
            case "Like New":
                bg = Color.parseColor("#B2EBF2");
                text = Color.parseColor("#006064");
                break;
            case "Brand New":
                bg = Color.parseColor("#D1C4E9");
                text = Color.parseColor("#4A148C");
                break;
            default:
                bg = Color.parseColor("#E0E0E0");
                text = Color.parseColor("#424242");
        }

        tag.setBackgroundTintList(ColorStateList.valueOf(bg));
        tag.setTextColor(text);
    }

    private void applyUsageColor(TextView txt, int totalDays) {
        if (totalDays <= 0) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E7FF")));
            txt.setTextColor(Color.parseColor("#3949AB"));
            return;
        }

        if (totalDays <= 30) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C8E6C9")));
            txt.setTextColor(Color.parseColor("#1B5E20"));
            return;
        }

        if (totalDays <= 180) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
            txt.setTextColor(Color.parseColor("#F9A825"));
            return;
        }

        if (totalDays <= 365) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE0B2")));
            txt.setTextColor(Color.parseColor("#EF6C00"));
            return;
        }

        txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
        txt.setTextColor(Color.parseColor("#C62828"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
