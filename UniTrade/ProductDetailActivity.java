package com.example.unitrade;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class ProductDetailActivity extends BaseActivity {

    private static final String TAG = "ProductDetailActivity";
    private ViewPager2 viewPagerImages;
    private TabLayout tabDots;

    private TextView txtItemTitle, txtPrice, txtConditionUsedDays, txtConditionTag,
            txtSellerName, txtRating, txtDescription;

    private ImageView imgSeller;

    private FloatingActionButton btnCart;
    private View topView;
    private View bottomView;
    private MovableFabHelper mover = new MovableFabHelper();
    private View rootView;


    private MaterialButton btnAddToCart, btnChatSeller, btnBuyNow, btnVisitProfile;

    private Product product;   // passed from adapter via Intent

    // Variables for movable FAB
    private float dX, dY;
    private long startClickTime;
    private static final int CLICK_DURATION_THRESHOLD = 200; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.appBarProductDetail);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Product Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }

        tintToolbarOverflow(toolbar);

        rootView = getWindow().getDecorView();

        btnCart = findViewById(R.id.btnCart);
        topView = findViewById(R.id.appBarProductDetail);
        bottomView = findViewById(R.id.bottomBar);

        mover.enable(btnCart, rootView, topView, bottomView);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ShoppingCartActivity.class);
            intent.putParcelableArrayListExtra("cart", new ArrayList<>(CartManager.cartList));
            startActivity(intent);
        });

        // ---------------------------------
        // 1. BIND ALL VIEWS FROM XML
        // ---------------------------------
        viewPagerImages = findViewById(R.id.viewPagerImages);
        Log.d(TAG, "onCreate: viewPagerImages found");
        tabDots = findViewById(R.id.tabDots);
        Log.d(TAG, "onCreate: tabDots found");

        txtItemTitle = findViewById(R.id.txtItemTitle);
        Log.d(TAG, "onCreate: txtItemTitle found");
        txtPrice = findViewById(R.id.txtPrice);
        Log.d(TAG, "onCreate: txtPrice found");
        txtConditionUsedDays = findViewById(R.id.txtConditionUsedDays);
        Log.d(TAG, "onCreate: txtConditionUsedDays found");
        txtConditionTag = findViewById(R.id.txtConditionTag);
        Log.d(TAG, "onCreate: txtConditionTag found");

        imgSeller = findViewById(R.id.imgSeller);
        Log.d(TAG, "onCreate: imgSeller found");
        txtSellerName = findViewById(R.id.txtSellerName);
        Log.d(TAG, "onCreate: txtSellerName found");
        txtRating = findViewById(R.id.txtRating);
        Log.d(TAG, "onCreate: txtRating found");

        txtDescription = findViewById(R.id.txtDescription);
        Log.d(TAG, "onCreate: txtDescription found");

        btnAddToCart = findViewById(R.id.btnAddToCart);
        Log.d(TAG, "onCreate: btnAddToCart found");
        btnChatSeller = findViewById(R.id.btnChatSeller);
        Log.d(TAG, "onCreate: btnChatSeller found");
        btnBuyNow = findViewById(R.id.btnBuyNow);
        Log.d(TAG, "onCreate: btnBuyNow found");
        btnVisitProfile = findViewById(R.id.btnVisitProfile);
        Log.d(TAG, "onCreate: btnVisitProfile found");


        // ---------------------------------
        // 2. RECEIVE PRODUCT FROM INTENT
        // ---------------------------------
        product = getIntent().getParcelableExtra("product");
        Log.d(TAG, "onCreate: product received");

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        String usedLabel = formatUsage(
                product.getUsedDaysTotal()
        );

        // ---------------------------------
        // 3. SET PRODUCT INFO
        // ---------------------------------
        txtItemTitle.setText(product.getName());
        txtPrice.setText(AppSettings.formatPrice(this, product.getPrice()));
        txtConditionUsedDays.setText(usedLabel);
        txtConditionTag.setText(product.getCondition());
        txtDescription.setText(product.getDescription());
        Log.d(TAG, "onCreate: product info set");

        applyConditionStyle(txtConditionTag, product.getCondition());
        applyUsageColor(txtConditionUsedDays, product.getUsedDaysTotal());


        // ---------------------------------
        // 4. SELLER INFO (FIXED)
        // ---------------------------------
        User seller = SampleData.getUserById(this, product.getSellerId());   // ✔ FIX
        Log.d(TAG, "onCreate: seller found");

        if (seller != null) {
            txtSellerName.setText(seller.getUsername());
            txtRating.setText(String.format("%.1f rating", seller.getOverallRating()));

            Glide.with(this)
                    .load(seller.getProfileImageUrl())
                    .circleCrop()
                    .into(imgSeller);
            Log.d(TAG, "onCreate: seller info set");
        }

        // ---------------------------------
        // Visit Seller Profile
        // ---------------------------------
        btnVisitProfile.setOnClickListener(v -> {
            if (seller != null) {
                Intent intent = new Intent(ProductDetailActivity.this, UserProfileActivity.class);
                intent.putExtra("user_to_view", seller);
                startActivity(intent);
            }
        });

        // ---------------------------------
        // Chat Seller
        // ---------------------------------
        btnChatSeller.setOnClickListener(v -> {
            if (seller != null) {
                Chat chat = new Chat(
                        seller.getUsername(),
                        "Start conversation",
                        "Now",
                        seller.getProfileImageUrl(),
                        false,
                        seller.getId()
                );
                Intent intent = new Intent(ProductDetailActivity.this, ConversationActivity.class);
                intent.putExtra("chat", chat);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Seller info not available", Toast.LENGTH_SHORT).show();
            }
        });


        // ---------------------------------
        // 5. IMAGE SLIDER
        // ---------------------------------
        ImageSliderAdapter adapter =
                new ImageSliderAdapter(ProductDetailActivity.this, product.getImageUrls());

        viewPagerImages.setAdapter(adapter);
        Log.d(TAG, "onCreate: image slider adapter set");

        // Connect dots indicator
        new TabLayoutMediator(tabDots, viewPagerImages, (tab, position) -> {}).attach();


        // ---------------------------------
        // 6. BUTTON ACTIONS
        // ---------------------------------
        btnAddToCart.setOnClickListener(v -> {
            CartManager.addItem(this, product);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });

        btnBuyNow.setOnClickListener(v -> {

            ArrayList<Product> tempList = new ArrayList<>();
            tempList.add(product);   // THIS PRODUCT ONLY

            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            intent.putParcelableArrayListExtra("checkoutItems", tempList);
            startActivity(intent);
        });


        btnVisitProfile.setOnClickListener(v -> {

            if (seller != null) {
                Intent intent = new Intent(ProductDetailActivity.this, UserProfileActivity.class);
                intent.putExtra("user_to_view", seller); // pass Parcelable user
                startActivity(intent);
            }
        });
        Log.d(TAG, "onCreate: finished");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (product != null) {
            txtPrice.setText(AppSettings.formatPrice(this, product.getPrice()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void applyConditionStyle(TextView tag, String condition) {

        int bgColor;
        int textColor = Color.parseColor("#004D40"); // default dark green text

        switch (condition) {

            case "Good":
                bgColor = Color.parseColor("#C8E6C9");  // pastel green
                textColor = Color.parseColor("#1B5E20"); // dark green
                break;

            case "Fair":
                bgColor = Color.parseColor("#FFE0B2");  // pastel orange
                textColor = Color.parseColor("#E65100"); // deep orange
                break;

            case "Like New":
                bgColor = Color.parseColor("#B2EBF2");  // pastel cyan
                textColor = Color.parseColor("#006064"); // teal blue
                break;

            case "Brand New":
                bgColor = Color.parseColor("#D1C4E9");  // pastel purple
                textColor = Color.parseColor("#4A148C"); // deep purple
                break;

            case "Excellent":
                bgColor = Color.parseColor("#BBDEFB");  // pastel baby blue
                textColor = Color.parseColor("#0D47A1"); // strong blue
                break;

            case "Old But Working":
                bgColor = Color.parseColor("#FFECB3");  // cream yellow
                textColor = Color.parseColor("#FF6F00"); // amber
                break;

            case "For Parts/ Not Working":
                bgColor = Color.parseColor("#FFCDD2");  // pastel red
                textColor = Color.parseColor("#B71C1C"); // deep red
                break;

            case "Refurbished":
                bgColor = Color.parseColor("#DCEDC8");  // pastel light green
                textColor = Color.parseColor("#33691E"); // olive green
                break;

            default:
                bgColor = Color.parseColor("#E0E0E0");  // grey
                textColor = Color.parseColor("#424242"); // dark grey
                break;
        }

        tag.setBackgroundResource(R.drawable.bg_condition_tag);
        tag.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        tag.setTextColor(textColor);
    }

    private String formatUsage(int totalDays) {

        if (totalDays <= 0)
            return "Unused";

        int years = totalDays / 365;
        int months = (totalDays % 365) / 30;
        int days = (totalDays % 365) % 30;


        if (years > 0) return "Used (" + years + " years)";
        if (months > 0) return "Used (" + months + " months)";
        return "Used (" + days + " days)";

    }

    private void applyUsageColor(TextView txt, int totalDays) {

        txt.setBackgroundResource(R.drawable.bg_condition_tag);

        // --- Unused ---
        if (totalDays <= 0) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E7FF"))); // lavender
            txt.setTextColor(Color.parseColor("#3949AB"));  // deep blue
            return;
        }

        // --- 0–1 month ---
        if (totalDays <= 30) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C8E6C9"))); // light green
            txt.setTextColor(Color.parseColor("#1B5E20")); // dark green
            return;
        }

        // --- 1–6 months ---
        if (totalDays <= 180) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF9C4"))); // light yellow
            txt.setTextColor(Color.parseColor("#F9A825")); // amber
            return;
        }

        // --- 6–12 months ---
        if (totalDays <= 365) {
            txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE0B2"))); // light orange
            txt.setTextColor(Color.parseColor("#EF6C00")); // dark orange
            return;
        }

        // --- More than 1 year ---
        txt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); // light red
        txt.setTextColor(Color.parseColor("#C62828")); // dark red
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


}
