package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText edtSearch;

    private FloatingActionButton btnCart;
    private BottomNavigationView bottomNav;
    private View topView;
    private MovableFabHelper mover = new MovableFabHelper();
    private View rootView;
    private ImageView btnFilter;

    private Button btnLatest, btnNearest, btnPrice;
    private Button btnUsed, btnUnused;
    private Button btnOnCampus, btnOffCampus;
    private ScrollView filterPanel;

    private RecyclerView rvProducts;
    private ItemAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;

    private Button currentSelected = null;
    private String selectedPriceMode = null;

    // Variables for movable FAB
    private float dX, dY;
    private long startClickTime;
    private static final int CLICK_DURATION_THRESHOLD = 200; // milliseconds

    public SearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        btnCart = view.findViewById(R.id.btnCart);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        topView = view.findViewById(R.id.searchBarContainer);   // top limit

        mover.enable(btnCart, rootView, topView, bottomNav);


        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShoppingCartActivity.class);
            intent.putParcelableArrayListExtra("cart", new ArrayList<>(CartManager.cartList));
            startActivity(intent);
        });

        String query = getArguments() != null ? getArguments().getString("query", "") : "";
        edtSearch = view.findViewById(R.id.edtSearch);
        edtSearch.setText(query);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        btnLatest = view.findViewById(R.id.btnLatest);
        btnNearest = view.findViewById(R.id.btnNearest);
        btnPrice = view.findViewById(R.id.btnPrice);

        btnUsed = view.findViewById(R.id.btnUsed);
        btnUnused = view.findViewById(R.id.btnUnused);

        btnOnCampus = view.findViewById(R.id.btnCampusOn);
        btnOffCampus = view.findViewById(R.id.btnCampusOff);

        filterPanel = view.findViewById(R.id.filterPanel);
        rvProducts = view.findViewById(R.id.rvSearchProducts);

        btnFilter.setOnClickListener(v -> {
            filterPanel.setVisibility(filterPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        btnLatest.setOnClickListener(v -> highlightButton(btnLatest));
        btnNearest.setOnClickListener(v -> highlightButton(btnNearest));
        btnPrice.setOnClickListener(v -> {
            if (selectedPriceMode != null) {
                selectedPriceMode = null;
                btnPrice.setText("price ▼");
                resetButtonStyle(btnPrice);
                return;
            }
            showPriceDropdown();
        });

        setupTogglePair(btnUsed, btnUnused);
        setupTogglePair(btnOnCampus, btnOffCampus);

        loadProducts();
        
        // Initial search if query passed
        String initialQuery = edtSearch.getText().toString().trim();
        if (!initialQuery.isEmpty()) {
            filterProducts(initialQuery);
        }

        // Add text change listener for real-time search or enter key
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                filterProducts(edtSearch.getText().toString());
                return true;
            }
            return false;
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void setupTogglePair(Button b1, Button b2) {
        b1.setOnClickListener(v -> {
            applySelectedStyle(b1);
            resetButtonStyle(b2);
        });

        b2.setOnClickListener(v -> {
            applySelectedStyle(b2);
            resetButtonStyle(b1);
        });
    }


    private void highlightButton(Button selected) {
        if (selected == btnLatest || selected == btnNearest) {

            if (currentSelected == selected) {
                resetButtonStyle(selected);
                currentSelected = null;
                return;
            }

            resetButtonStyle(btnLatest);
            resetButtonStyle(btnNearest);

            selectedPriceMode = null;
            btnPrice.setText("price ▼");
            resetButtonStyle(btnPrice);

            applySelectedStyle(selected);
            currentSelected = selected;
            return;
        }

        if (selected == btnPrice) {
            if (selectedPriceMode != null) {
                selectedPriceMode = null;
                btnPrice.setText("price ▼");
                resetButtonStyle(btnPrice);
                return;
            }
            showPriceDropdown();
        }
    }

    private void showPriceDropdown() {
        PopupMenu menu = new PopupMenu(getContext(), btnPrice);
        menu.getMenu().add("Lowest");
        menu.getMenu().add("Highest");

        menu.setOnMenuItemClickListener(item -> {

            selectedPriceMode = item.getTitle().toString();
            btnPrice.setText(selectedPriceMode);

            resetButtonStyle(btnLatest);
            resetButtonStyle(btnNearest);
            currentSelected = null;

            applySelectedStyle(btnPrice);

            return true;
        });

        menu.show();
    }

    private void resetButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.bg_filter_chip);
        b.setBackgroundTintList(null);
        b.setTextColor(Color.BLACK);
    }

    private void applySelectedStyle(Button b) {
        b.setBackgroundResource(R.drawable.bg_filter_chip_selected);
        b.setBackgroundTintList(null);
        b.setTextColor(Color.WHITE);
    }

    private void loadProducts() {
        allProducts = SampleData.generateSampleProducts(requireContext());
        filteredProducts = new ArrayList<>(allProducts);

        adapter = new ItemAdapter(filteredProducts, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                    product.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        btnCart.post(() ->
                mover.enable(btnCart, rootView, topView, bottomNav)
        );
    }
}
