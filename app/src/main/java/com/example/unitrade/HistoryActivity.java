package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    // ---------------- UI ----------------
    private RecyclerView rvHistory;
    private LinearLayout emptyState;
    private android.widget.ProgressBar loadingSpinner;
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
    private boolean isLoading = false;

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
        loadingSpinner = findViewById(R.id.loadingSpinner);
        btnEdit = findViewById(R.id.btnEditHistory);
        tabHistory = findViewById(R.id.tabHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        setupRecycler();
        setupTabs();
        setupEditButton();

        setupEditButton();

        reloadData();
    }

    // ============================================================
    @Override
    protected void onResume() {
        super.onResume();
        reloadData();

        if (isPurchasedTab) {
            // showPurchased(); // handled by reloadData callback
        } else {
            // showSold();
        }
    }

    // ============================================================
    // DATA RELOAD (NO SAMPLE DATA)
    // ============================================================
    private void reloadData() {
        // Use temp lists so we don't clear UI immediately
        List<Product> tempPurchased = new ArrayList<>();
        List<Product> tempSold = new ArrayList<>();

        String currentUserId = UserSession.get().getId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading state
        isLoading = true;
        loadingSpinner.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        // Purchased
        db.collection("products")
                .whereEqualTo("buyerId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Product p = doc.toObject(Product.class);
                            if (p != null && p.getTransactionDate() > 0 && p.getPrice() > 0) {
                                tempPurchased.add(p);
                            }
                        }
                    }

                    // Sold
                    db.collection("products")
                            .whereEqualTo("sellerId", currentUserId)
                            .get()
                            .addOnCompleteListener(sellTask -> {
                                if (sellTask.isSuccessful()) {
                                    for (DocumentSnapshot doc : sellTask.getResult()) {
                                        Product p = doc.toObject(Product.class);
                                        if (p != null &&
                                                (p.getStatus().equalsIgnoreCase("Sold") ||
                                                        p.getStatus().equalsIgnoreCase("Donated"))
                                                &&
                                                p.getTransactionDate() > 0) {
                                            tempSold.add(p);
                                        }
                                    }
                                }

                                // Update Member Lists
                                purchasedList.clear();
                                purchasedList.addAll(tempPurchased);
                                soldList.clear();
                                soldList.addAll(tempSold);

                                // Sort
                                Collections.sort(purchasedList,
                                        (p1, p2) -> Long.compare(p2.getTransactionDate(), p1.getTransactionDate()));
                                Collections.sort(soldList,
                                        (p1, p2) -> Long.compare(p2.getTransactionDate(), p1.getTransactionDate()));

                                isLoading = false;
                                loadingSpinner.setVisibility(View.GONE);

                                if (isPurchasedTab)
                                    showPurchased();
                                else
                                    showSold();
                            });
                });
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

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
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

        if (list.isEmpty() && !isLoading) {

            rvHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            if (isPurchasedMode) {
                // 🛒 Purchased tab
                txtEmptyTitle.setText("Nothing purchased yet");
                txtEmptySubtitle.setText("Start browsing items to buy.");
                btnAction.setText("Shop Now");

                btnAction.setOnClickListener(v -> {
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra("goToHome", true);
                    startActivity(i);
                    finish();
                });

            } else {
                // 💰 Sold tab
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

        } else {
            rvHistory.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

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
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("products")
                            .document(product.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Refresh lists
                                reloadData();
                                showSold();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete listing", Toast.LENGTH_SHORT).show();
                            });
                });
    }

}
