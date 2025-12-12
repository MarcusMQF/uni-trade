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

    private EditText edtSearch;

    private FloatingActionButton btnCart;
    private BottomNavigationView bottomNav;
    private View topView;
    private MovableFabHelper mover = new MovableFabHelper();
    private View rootView;

    private RecyclerView rvProducts;
    private ItemAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private String currentUserId = "";

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ----------------------------
        // ✔ Safe current user handling
        // ----------------------------
        if (UserSession.get() != null) {
            currentUserId = UserSession.get().getId();
        } else {
            currentUserId = "";   // fallback
        }

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rootView = view;
        btnCart = view.findViewById(R.id.btnCart);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        topView = view.findViewById(R.id.searchBarContainer);

        mover.enable(btnCart, rootView, topView, bottomNav);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShoppingCartActivity.class);
            intent.putParcelableArrayListExtra("cart", new ArrayList<>(CartManager.cartList));
            startActivity(intent);
        });

        // ----------------------------
        // Get passed search query
        // ----------------------------
        Bundle args = getArguments();
        String query = "";

        if (args != null) {
            query = args.getString("query", "");
        }

        edtSearch = view.findViewById(R.id.edtSearch);
        edtSearch.setText(query);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);
        rvProducts = view.findViewById(R.id.rvSearchProducts);

        loadProducts();

        // ------------------------------------------------
        // Initial filtering if there was a query passed in
        // ------------------------------------------------
        String initialQuery = edtSearch.getText().toString().trim();
        if (!initialQuery.isEmpty()) {
            filterProducts(initialQuery);
        }

        // ------------------------------
        // Search on enter / search button
        // ------------------------------
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

            if (isEnter) {
                filterProducts(edtSearch.getText().toString());
                return true;
            }
            return false;
        });

        // ----------
        // Live search
        // ----------
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ============================================
    // LOAD AVAILABLE PRODUCTS (NOT SOLD, NOT USER)
    // ============================================
    private void loadProducts() {

        allProducts = SampleData.getAvailableItems(requireContext(), currentUserId);
        filteredProducts = new ArrayList<>(allProducts);

        adapter = new ItemAdapter(filteredProducts, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);
    }

    // =======================
    // SEARCH FILTERING LOGIC
    // =======================
    private void filterProducts(String query) {

        filteredProducts.clear();

        if (query == null || query.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lower = query.toLowerCase().trim();

            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(lower) ||
                        p.getDescription().toLowerCase().contains(lower)) {
                    filteredProducts.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ==============================================================================
    // REFRESH WHEN COMING BACK FROM PRODUCT DETAIL OR CHECKOUT (status may change)
    // ==============================================================================
    @Override
    public void onResume() {
        super.onResume();

        // Reload list to reflect new Sold / Donated / Reserved status
        loadProducts();
        adapter.notifyDataSetChanged();

        btnCart.post(() -> mover.enable(btnCart, rootView, topView, bottomNav));
    }
}
