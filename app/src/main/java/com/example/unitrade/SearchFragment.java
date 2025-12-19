package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitrade.backend.FetchProductId;
import com.example.unitrade.backend.Sorting;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText edtSearch;
    private ImageView btnFilter;
    private RecyclerView rvProducts;
    private List<Product> filteredProducts = new ArrayList<>();
    private ItemAdapter adapter = new ItemAdapter(filteredProducts, product -> {
    });
    private ScrollView filterPanel;

    private Button btnLatest, btnNearest, btnPrice;
    private Button currentSelected = null;
    private String selectedPriceMode = null;

    private TextView tvNoProduct; // For "No Product Match" message

    public SearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Views
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        rvProducts = view.findViewById(R.id.rvSearchProducts);
        filterPanel = view.findViewById(R.id.filterPanel);

        btnLatest = view.findViewById(R.id.btnLatest);
        btnNearest = view.findViewById(R.id.btnNearest);
        btnPrice = view.findViewById(R.id.btnPrice);

        // RecyclerView setup
        filteredProducts = new ArrayList<>();
        adapter = new ItemAdapter(filteredProducts, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);

        // Filter panel toggle
        btnFilter.setOnClickListener(v -> {
            filterPanel.setVisibility(filterPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        // Price dropdown
        btnPrice.setOnClickListener(v -> {
            if (selectedPriceMode != null) {
                selectedPriceMode = null;
                btnPrice.setText("price ▼");
                resetButtonStyle(btnPrice);
                return;
            }
            showPriceDropdown();
        });

        // Latest button sorting
        btnLatest.setOnClickListener(v -> {
            Sorting.sortByLatest(filteredProducts);
            adapter.notifyDataSetChanged();
            resetButtonStyle(btnLatest);
        });

        // Search when typing or pressing enter
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchProducts(edtSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String query = "";
        if (getArguments() != null) {
            query = getArguments().getString("query", "");
        }

        if (!query.isEmpty()) {
            searchProducts(query);
        }
    }


    // ----- Helper methods -----

    private void showPriceDropdown() {
        PopupMenu menu = new PopupMenu(getContext(), btnPrice);
        menu.getMenu().add("Lowest");
        menu.getMenu().add("Highest");

        menu.setOnMenuItemClickListener(item -> {
            selectedPriceMode = item.getTitle().toString();
            btnPrice.setText(selectedPriceMode);

            // Reset other buttons
            resetButtonStyle(btnLatest);
            resetButtonStyle(btnNearest);
            currentSelected = null;
            applySelectedStyle(btnPrice);

            // Sorting
            boolean ascending = selectedPriceMode.equalsIgnoreCase("Lowest");
            Sorting.sortByPrice(filteredProducts, ascending);
            adapter.notifyDataSetChanged();
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


    // ----- Firestore Search -----
    protected void searchProducts(String query) {
        if (!isAdded()) return; // Fragment not attached, skip

        // Make sure filteredProducts and adapter are initialized
        if (filteredProducts == null) filteredProducts = new ArrayList<>();
        if (adapter == null) {
            adapter = new ItemAdapter(filteredProducts, product -> {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
            rvProducts.setAdapter(adapter);
        }

        if (query.isEmpty()) {
            filteredProducts.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        FetchProductId.searchProductsByKeyword(query, new FetchProductId.OnResultListener() {
            @Override
            public void onSuccess(List<Product> products) {
                if (!isAdded()) return; // Check again in async callback

                filteredProducts.clear();
                if (products != null) {
                    filteredProducts.addAll(products);
                }

                adapter.notifyDataSetChanged();

            }
            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;

                e.printStackTrace();
                filteredProducts.clear();
                adapter.notifyDataSetChanged();

            }
        });
    }
}