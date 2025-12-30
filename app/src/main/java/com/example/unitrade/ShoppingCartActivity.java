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
import java.util.ArrayList;

import java.util.List;

public class ShoppingCartActivity extends BaseActivity {

    private RecyclerView rvCart;
    private Button btnEdit;
    private ShoppingCartAdapter adapter;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.appBarShoppingCart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tintToolbarOverflow(toolbar);

        // Load cart IDs
        CartManager.loadCart(this);

        // Views
        rvCart = findViewById(R.id.rvCart);
        btnEdit = findViewById(R.id.btnEditCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        // Adapter (empty for now)
        adapter = new ShoppingCartAdapter(this, new ArrayList<>());
        rvCart.setAdapter(adapter);
        adapter.setOnCartChangedListener(this::updateEmptyState);

        // Load products async from Firestore
        CartManager.getCartProducts(this, products -> {
            adapter.rebuild(products);       // same as your old adapter setList
            updateEmptyState();              // show/hide empty state
        });

        // Edit mode
        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            btnEdit.setText(isEditMode ? "Done" : "Edit Shopping Cart");
        });

        // Initial empty state (based on cart IDs)
        updateEmptyState();
    }


    // =====================================================
    private void updateEmptyState() {

        LinearLayout header = findViewById(R.id.layoutCartHeader);
        View emptyState = findViewById(R.id.emptyStateView);
        Button btnShopNow = emptyState.findViewById(R.id.btnShopNow);

        boolean isEmpty = CartManager.cartProductIds.isEmpty();

        if (isEmpty) {
            rvCart.setVisibility(View.GONE);
            header.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            TextView title = emptyState.findViewById(R.id.txtEmptyTitle);
            TextView subtitle = emptyState.findViewById(R.id.txtEmptySubtitle);

            title.setText("Your cart is empty");
            subtitle.setText("Add some items to get started.");

            btnShopNow.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("goToHome", true);
                startActivity(i);
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
        finish();
        return true;
    }
}
