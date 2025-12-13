package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    // ---------------- UI ----------------
    private EditText edtSearch;
    private RecyclerView rvProducts;

    private FloatingActionButton btnCart;
    private BottomNavigationView bottomNav;
    private View topView;
    private View rootView;

    private final MovableFabHelper mover = new MovableFabHelper();

    // ---------------- Data ----------------
    private ItemAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();

    private String currentUserId = "";

    public SearchFragment() {}

    // ==========================================================
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        rootView = view;

        // ---------------- Current user ----------------
        if (UserSession.get() != null) {
            currentUserId = UserSession.get().getId();
        }

        // ---------------- Views ----------------
        edtSearch = view.findViewById(R.id.edtSearch);
        rvProducts = view.findViewById(R.id.rvSearchProducts);
        btnCart = view.findViewById(R.id.btnCart);

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        topView = view.findViewById(R.id.searchBarContainer);

        // ---------------- Movable FAB ----------------
        mover.enable(btnCart, rootView, topView, bottomNav);

        btnCart.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ShoppingCartActivity.class))
        );

        // ---------------- Restore search query ----------------
        Bundle args = getArguments();
        if (args != null) {
            edtSearch.setText(args.getString("query", ""));
        }

        return view;
    }

    // ==========================================================
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        setupRecycler();
        loadProducts();
        setupSearchLogic();

        // Initial filter if query exists
        String initialQuery = edtSearch.getText().toString().trim();
        if (!initialQuery.isEmpty()) {
            filterProducts(initialQuery);
        }
    }

    // ==========================================================
    // SETUP RECYCLER
    // ==========================================================
    private void setupRecycler() {

        adapter = new ItemAdapter(filteredProducts, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);
    }

    // ==========================================================
    // LOAD AVAILABLE PRODUCTS
    // ==========================================================
    private void loadProducts() {

        allProducts = SampleData.getAvailableItems(requireContext(), currentUserId);

        filteredProducts.clear();
        filteredProducts.addAll(allProducts);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // ==========================================================
    // SEARCH LOGIC
    // ==========================================================
    private void setupSearchLogic() {

        // Search on keyboard action
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearch =
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

            if (isSearch) {
                filterProducts(edtSearch.getText().toString());
                return true;
            }
            return false;
        });

        // Live search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterProducts(s.toString());
            }
        });
    }

    // ==========================================================
    // FILTER PRODUCTS
    // ==========================================================
    private void filterProducts(String query) {

        filteredProducts.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lower = query.toLowerCase().trim();

            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(lower)
                        || p.getDescription().toLowerCase().contains(lower)) {
                    filteredProducts.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ==========================================================
    // REFRESH ON RETURN (STATUS MAY CHANGE)
    // ==========================================================
    @Override
    public void onResume() {
        super.onResume();

        loadProducts();

        btnCart.post(() ->
                mover.enable(btnCart, rootView, topView, bottomNav)
        );
    }
}
