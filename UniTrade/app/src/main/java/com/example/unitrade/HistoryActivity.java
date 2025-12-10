package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private RecyclerView rvHistory;
    private LinearLayout emptyState;
    private MaterialButton btnEdit;

    private HistoryAdapter adapter;

    private TabLayout tabHistory;

    private List<Product> purchasedList = new ArrayList<>();
    private List<Product> soldList = new ArrayList<>();

    private boolean isEditMode = false;
    private boolean isPurchasedTab = true;  // default tab = Purchased

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // ----- APP BAR -----
        MaterialToolbar toolbar = findViewById(R.id.appBarHistory);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        tintToolbarOverflow(toolbar);

        // ----- VIEWS -----
        rvHistory = findViewById(R.id.rvHistory);
        emptyState = findViewById(R.id.emptyStateView);
        btnEdit = findViewById(R.id.btnEditHistory);
        tabHistory = findViewById(R.id.tabHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // Load sample data
        loadSampleData();

        // Setup tabs
        setupTabs();

        // Setup Edit Button
        setupEditButton();

        // Default screen = Purchased tab
        showPurchased();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the lists to reflect currency changes
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // -------------------------------------------------------------
    // LOAD SAMPLE DATA
    // -------------------------------------------------------------
    private void loadSampleData() {
        List<Product> all = SampleData.generateSampleProducts(this);

        purchasedList.clear();
        soldList.clear();

        for (Product p : all) {
            if (p.getStatus().equalsIgnoreCase("Sold")) {
                soldList.add(p);
            } else {
                purchasedList.add(p);
            }
        }
    }

    // -------------------------------------------------------------
    // SETUP TABS
    // -------------------------------------------------------------
    private void setupTabs() {
        // Add tabs
        tabHistory.addTab(tabHistory.newTab().setText("Purchased"));
        tabHistory.addTab(tabHistory.newTab().setText("Sold"));

        tabHistory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isPurchasedTab = true;
                    showPurchased();
                } else {
                    isPurchasedTab = false;
                    showSold();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // -------------------------------------------------------------
    // DISPLAY PURCHASED
    // -------------------------------------------------------------
    private void showPurchased() {
        updateAdapter(purchasedList, true);
    }

    // -------------------------------------------------------------
    // DISPLAY SOLD
    // -------------------------------------------------------------
    private void showSold() {
        updateAdapter(soldList, false);
    }

    // -------------------------------------------------------------
    // UPDATE ADAPTER BASED ON TAB
    // -------------------------------------------------------------
    private void updateAdapter(List<Product> list, boolean isPurchasedMode) {

        TextView txtEmptyTitle = emptyState.findViewById(R.id.txtEmptyTitle);
        TextView txtEmptySubtitle = emptyState.findViewById(R.id.txtEmptySubtitle);
        MaterialButton btnShopNow = emptyState.findViewById(R.id.btnShopNow);

        // --- UPDATE EMPTY VIEW TEXT & BUTTON ---
        if (isPurchasedMode) {
            // PURCHASED TAB
            txtEmptyTitle.setText("Nothing purchased yet");
            txtEmptySubtitle.setText("Start browsing items to buy.");
            btnShopNow.setText("Shop Now");

            btnShopNow.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("goToHome", true);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });

        } else {
            // SOLD TAB
            txtEmptyTitle.setText("No sold items yet");
            txtEmptySubtitle.setText("List an item to start selling.");
            btnShopNow.setText("List an Item");

            btnShopNow.setOnClickListener(v -> {
                Intent i = new Intent(HistoryActivity.this, MainActivity.class);
                i.putExtra("openSellFragment", true);
                i.putExtra("fromExternal", true);
                startActivityForResult(i, 201);
            });

        }

        // --- SHOW/HIDE EMPTY STATE ---
        if (list.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        adapter = new HistoryAdapter(this, list, isPurchasedMode);
        rvHistory.setAdapter(adapter);

        adapter.setOnHistoryActionListener(new HistoryAdapter.OnHistoryActionListener() {
            @Override
            public void onDelete(Product p) {
                ConfirmDialog.show(
                        HistoryActivity.this,
                        "Remove entry?",
                        "Do you want to delete this entry?",
                        "Delete",
                        () -> {
                            list.remove(p);
                            updateAdapter(list, isPurchasedMode);
                        }
                );
            }

            @Override
            public void onEdit(Product p) {
                openSellFragmentForEdit(p);
            }
        });

        adapter.setEditMode(isEditMode);
    }


    // -------------------------------------------------------------
    // EDIT MODE BUTTON
    // -------------------------------------------------------------
    private void setupEditButton() {
        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            btnEdit.setText(isEditMode ? "Done" : "Edit History");

            if (adapter != null) {
                adapter.setEditMode(isEditMode);
            }
        });
    }

    private void openSellFragmentForEdit(Product product) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("editMode", true);
        intent.putExtra("product_to_edit", product); // Parcelable
        intent.putExtra("fromExternal", true);
        // ⚠️ Important: tell MainActivity to open SellFragment immediately
        intent.putExtra("openSellFragment", true);


        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        // EDIT ITEM result
        if (requestCode == 200) {
            Product updated = data.getParcelableExtra("updated_product");
            if (updated != null) replaceProductInList(updated);
        }

        // LIST NEW ITEM result (you may handle this later if needed)
        if (requestCode == 201) {
            // OPTIONAL: refresh your lists after posting new item
            loadSampleData();
            if (isPurchasedTab) {
                showPurchased();
            } else {
                showSold();
            }
        }
    }

    private void replaceProductInList(Product updatedProduct) {
        for (int i = 0; i < soldList.size(); i++) {
            if (soldList.get(i).getId().equals(updatedProduct.getId())) {
                soldList.set(i, updatedProduct);
                adapter.notifyItemChanged(i);
                return;
            }
        }
    }
}
