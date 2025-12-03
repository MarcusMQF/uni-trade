package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingCartActivity extends BaseActivity {

    RecyclerView rvCart;
    Button btnEdit;
    ShoppingCartAdapter adapter;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // -------------------------------
        // Toolbar Setup
        // -------------------------------
        Toolbar toolbar = findViewById(R.id.appBarShoppingCart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tintToolbarOverflow(toolbar);

        // -------------------------------
        // Load cart data
        // -------------------------------
        CartManager.loadCart(this);

        // -------------------------------
        // Views
        // -------------------------------
        rvCart = findViewById(R.id.rvCart);
        btnEdit = findViewById(R.id.btnEditCart);
        LinearLayout layoutCartHeader = findViewById(R.id.layoutCartHeader);
        View emptyStateView = findViewById(R.id.emptyStateView);

        // -------------------------------
        // Adapter (MUST create before using)
        // -------------------------------
        adapter = new ShoppingCartAdapter(this, CartManager.cartList);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        // -------------------------------
        // Listener (SAFE now, adapter is not null)
        // -------------------------------
        adapter.setOnCartChangedListener(() -> updateEmptyState());

        // -------------------------------
        // Edit Mode
        // -------------------------------
        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            btnEdit.setText(isEditMode ? "Done" : "Edit Shopping Cart");
        });

        // -------------------------------
        // FIRST TIME UI UPDATE
        // -------------------------------
        updateEmptyState();
    }

    private void updateEmptyState() {
        LinearLayout header = findViewById(R.id.layoutCartHeader);
        View emptyState = findViewById(R.id.emptyStateView);
        Button btnShopNow = emptyState.findViewById(R.id.btnShopNow);

        if (CartManager.cartList.isEmpty()) {
            rvCart.setVisibility(View.GONE);
            header.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            TextView title = emptyState.findViewById(R.id.txtEmptyTitle);
            TextView subtitle = emptyState.findViewById(R.id.txtEmptySubtitle);

            title.setText("Your cart is empty");
            subtitle.setText("Add some items to get started.");

            btnShopNow.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("goToHome", true); // Pass flag to MainActivity
                startActivity(intent);
                finish();
            });
        } else {
            rvCart.setVisibility(View.VISIBLE);
            header.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();     // ⬅️ closes the activity
        return true;
    }

}
