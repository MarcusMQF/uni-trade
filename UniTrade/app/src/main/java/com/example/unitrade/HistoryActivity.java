package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    // ---------------- UI ----------------
    private RecyclerView rvHistory;
    private LinearLayout emptyState;
    private MaterialButton btnEdit;
    private TabLayout tabHistory;

    // ---------------- Adapter ----------------
    private HistoryAdapter adapter;

    // ---------------- Data ----------------
    private final List<Product> purchasedList = new ArrayList<>();
    private final List<Product> soldList = new ArrayList<>();

    // ---------------- State ----------------
    private boolean isEditMode = false;
    private boolean isPurchasedTab = true;

    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.appBarHistory);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        tintToolbarOverflow(toolbar);

        // Views
        rvHistory = findViewById(R.id.rvHistory);
        emptyState = findViewById(R.id.emptyStateView);
        btnEdit = findViewById(R.id.btnEditHistory);
        tabHistory = findViewById(R.id.tabHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        setupRecycler();
        setupTabs();
        setupEditButton();

        reloadData();
        showPurchased(); // default
    }

    // ============================================================
    @Override
    protected void onResume() {
        super.onResume();
        reloadData();

        if (isPurchasedTab) {
            showPurchased();
        } else {
            showSold();
        }
    }

    // ============================================================
    // DATA RELOAD (NO SAMPLE DATA)
    // ============================================================
    private void reloadData() {

        purchasedList.clear();
        soldList.clear();

        String currentUserId = UserSession.get().getId();

        // Purchased
        purchasedList.addAll(PurchaseHistoryManager.purchasedItems);

        // Sold / Donated
        for (Product p : SampleData.getAllProducts(this)) {
            if (p.getSellerId().equals(currentUserId)
                    && (p.getStatus().equalsIgnoreCase("Sold")
                    || p.getStatus().equalsIgnoreCase("Donated"))) {

                soldList.add(p);
            }
        }
    }

    // ============================================================
    // TABS
    // ============================================================
    private void setupTabs() {
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

    // ============================================================
    // DISPLAY
    // ============================================================
    private void showPurchased() {
        updateAdapter(purchasedList, true);
    }

    private void showSold() {
        updateAdapter(soldList, false);
    }

    private void updateAdapter(List<Product> list, boolean isPurchasedMode) {

        TextView txtEmptyTitle = emptyState.findViewById(R.id.txtEmptyTitle);
        TextView txtEmptySubtitle = emptyState.findViewById(R.id.txtEmptySubtitle);
        MaterialButton btnAction = emptyState.findViewById(R.id.btnShopNow);

        if (isPurchasedMode) {
            txtEmptyTitle.setText("Nothing purchased yet");
            txtEmptySubtitle.setText("Start browsing items to buy.");
            btnAction.setText("Shop Now");

            btnAction.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("goToHome", true);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            });

        } else {
            txtEmptyTitle.setText("No sold items yet");
            txtEmptySubtitle.setText("List an item to start selling.");
            btnAction.setText("List an Item");

            btnAction.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("openSellFragment", true);
                i.putExtra("origin", "from_history_new");
                startActivity(i);
            });
        }

        if (list.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        adapter.setPurchasedTab(isPurchasedMode);
        adapter.updateList(list);
        adapter.setEditMode(isEditMode);
    }

    // ============================================================
    // EDIT MODE
    // ============================================================
    private void setupEditButton() {
        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            btnEdit.setText(isEditMode ? "Done" : "Edit History");
            adapter.setEditMode(isEditMode);
        });
    }

    // ============================================================
    // RECYCLER + CALLBACKS
    // ============================================================
    private void setupRecycler() {
        adapter = new HistoryAdapter(this, new ArrayList<>(), true);

        adapter.setOnHistoryActionListener(new HistoryAdapter.OnHistoryActionListener() {
            @Override
            public void onEdit(Product product) {
                openSellFragmentForEdit(product);
            }

            @Override
            public void onDelete(Product product) {
                confirmDelete(product);
            }
        });

        rvHistory.setAdapter(adapter);
    }

    // ============================================================
    // ACTIONS
    // ============================================================
    private void openSellFragmentForEdit(Product product) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("openSellFragment", true);
        i.putExtra("editMode", true);
        i.putExtra("product_id", product.getId());
        i.putExtra("origin", "my_history");
        startActivity(i);
    }

    private void confirmDelete(Product product) {

        ConfirmDialog.show(
                this,
                "Delete Listing",
                "This action cannot be undone.",
                "Delete",
                () -> {
                    // Remove from data source
                    SampleData.getAllProducts(this).remove(product);

                    // Refresh lists + UI
                    reloadData();
                    showSold();
                }
        );
    }

}
