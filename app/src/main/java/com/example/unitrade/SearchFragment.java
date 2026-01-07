package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitrade.backend.FetchProductId;
import com.example.unitrade.backend.Filter;
import com.example.unitrade.backend.RecommendationManager;
import com.example.unitrade.backend.Sorting;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private EditText edtSearch, minPrice, maxPrice, dateDay, dateMonth, dateYear;
    private ImageView btnFilter;
    private RecyclerView rvProducts;
    private List<Product> filteredProducts = new ArrayList<>();
    private ItemAdapter adapter = new ItemAdapter(filteredProducts, product -> {
    });
    private ScrollView filterPanel;

    private Button btnLatest, btnOldest, btnNearest, btnPrice, btnCampusOn, btnCampusOff, btnUsed, btnUnused, btnConfirmPrice, btnConfirmDate;
    private Button currentSelected = null;
    private String selectedPriceMode = null;

    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0;
    private double userLng = 0;

    private List<Product> allProducts = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView tvNoProduct; // For "No Product Match" message

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
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

        // filter
        btnLatest = view.findViewById(R.id.btnLatest);
        btnOldest = view.findViewById(R.id.btnOldest);
        btnNearest = view.findViewById(R.id.btnNearest);
        btnUsed = view.findViewById(R.id.btnUsed);
        btnUnused = view.findViewById(R.id.btnUnused);
        btnConfirmPrice = view.findViewById(R.id.btnConfirmPrice);
        btnConfirmDate = view.findViewById(R.id.btnConfirmDate);
        // offcampus
        // oncampus
        btnPrice = view.findViewById(R.id.btnPrice);
        btnCampusOff = view.findViewById(R.id.btnCampusOff);
        btnCampusOn = view.findViewById(R.id.btnCampusOn);
        btnUsed = view.findViewById(R.id.btnUsed);
        minPrice = view.findViewById(R.id.minPrice);
        maxPrice = view.findViewById(R.id.maxPrice);
        dateDay = view.findViewById(R.id.dateDay);
        dateMonth = view.findViewById(R.id.dateMonth);
        dateYear = view.findViewById(R.id.dateYear);

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

        // filter price
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

        minPrice.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchProducts(minPrice.getText().toString().trim());
                return true;
            }
            return false;
        });

        minPrice.addTextChangedListener(new TextWatcher() {
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

        maxPrice.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchProducts(maxPrice.getText().toString().trim());
                return true;
            }
            return false;
        });

        maxPrice.addTextChangedListener(new TextWatcher() {
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

        btnConfirmPrice.setOnClickListener(v -> {
            String searchText = edtSearch.getText().toString().trim().toLowerCase();

            String minText = minPrice.getText().toString().trim();
            String maxText = maxPrice.getText().toString().trim();

            double min = minText.isEmpty() ? 0 : Double.parseDouble(minText);
            double max = maxText.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxText);

            // Filter products by search text AND price
            List<Product> filtered = new ArrayList<>();
            for (Product p : allProducts) {
                boolean matchesSearch = p.getName().toLowerCase().contains(searchText);
                boolean matchesPrice = p.getPrice() >= min && p.getPrice() <= max;

                if (matchesSearch && matchesPrice) {
                    filtered.add(p);
                }
            }

            filteredProducts.clear();
            filteredProducts.addAll(filtered);
            adapter.notifyDataSetChanged();
        });


        // Latest button sorting
        btnLatest.setOnClickListener(v -> {
            Sorting.sortByLatest(filteredProducts);
            adapter.notifyDataSetChanged();
            resetButtonStyle(btnOldest);
            applySelectedStyle(btnLatest);
        });

        // Oldest button sorting
        btnOldest.setOnClickListener(v -> {
            Sorting.sortByOldest(filteredProducts);
            adapter.notifyDataSetChanged();
            resetButtonStyle(btnLatest);
            applySelectedStyle(btnOldest);
        });

        // used button logic
        // Used
        btnUsed.setOnClickListener(v -> {
            filteredProducts.clear();
            for (Product p : allProducts) {
                if ("Used".equalsIgnoreCase(p.getProductUsed())) {
                    filteredProducts.add(p);
                }
            }
            adapter.notifyDataSetChanged();
            resetButtonStyle(btnUnused);
            applySelectedStyle(btnUsed);
        });

// Unused
        btnUnused.setOnClickListener(v -> {
            filteredProducts.clear();
            for (Product p : allProducts) {
                if ("Unused".equalsIgnoreCase(p.getProductUsed())) {
                    filteredProducts.add(p);
                }
            }
            adapter.notifyDataSetChanged();
            resetButtonStyle(btnUsed);
            applySelectedStyle(btnUnused);
        });


        // Nearest button logic
        btnNearest.setOnClickListener(v -> {

            // 1. Check permission
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            // 2. Get last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            Toast.makeText(getContext(), "Unable to get your location", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double userLat = location.getLatitude();
                        double userLng = location.getLongitude();

                        filteredProducts.clear();

                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                        for (Product p : allProducts) {
                            try {
                                List<Address> addresses = geocoder.getFromLocationName(p.getLocation(), 1);
                                if (!addresses.isEmpty()) {
                                    double sellerLat = addresses.get(0).getLatitude();
                                    double sellerLng = addresses.get(0).getLongitude();

                                    double dist = distanceInKm(userLat, userLng, sellerLat, sellerLng);
                                    if (dist <= 5) {
                                        filteredProducts.add(p);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // Optional: sort by recommendation clicks within 5 km
                        filteredProducts.sort((p1, p2) -> Integer.compare(
                                RecommendationManager.getClicks(p2.getCategory()),
                                RecommendationManager.getClicks(p1.getCategory())));

                        adapter.notifyDataSetChanged();

                        resetButtonStyle(btnNearest);
                        applySelectedStyle(btnNearest);
                    });
        });

        String dayStr = dateDay.getText().toString().trim();
        String monthStr = dateDay.getText().toString().trim();
        String yearStr = dateYear.getText().toString().trim();

        btnConfirmDate.setOnClickListener(v -> {
            if (!dayStr.isEmpty() && !monthStr.isEmpty() && !yearStr.isEmpty()) {
                Filter filter = new Filter();
                filter.dateAfter(dayStr, monthStr, yearStr, products -> {

                    filteredProducts.clear();
                    filteredProducts.addAll(products);

                    adapter.notifyDataSetChanged();
                    resetButtonStyle(btnUnused);
                    applySelectedStyle(btnUnused);
                });
            }
            ;
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
            edtSearch.setText(query);
            edtSearch.setSelection(query.length());
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
        if (!isAdded())
            return; // Fragment not attached, skip

        // Make sure filteredProducts and adapter are initialized
        if (filteredProducts == null)
            filteredProducts = new ArrayList<>();
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
                if (!isAdded()) return;

                // Save the full search results for filtering
                allProducts.clear();
                if (products != null) {
                    allProducts.addAll(products);
                }

                // Display search results in RecyclerView
                filteredProducts.clear();
                filteredProducts.addAll(allProducts);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;

                e.printStackTrace();
                allProducts.clear();
                filteredProducts.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1️⃣ Initialize RecyclerView, Adapter, Buttons, etc ---
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ItemAdapter(filteredProducts, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvProducts.setAdapter(adapter);

        // --- 2️⃣ Setup all buttons, filters, etc ---
        btnFilter.setOnClickListener(v -> {
            filterPanel.setVisibility(filterPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
        // ... setup other buttons like btnPrice, btnUsed, btnUnused, btnNearest, btnConfirmPrice ...

        // --- 3️⃣ Setup search listeners ---
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (isEnter) {
                searchProducts(edtSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        // --- 4️⃣ Handle query from HomeFragment ---
        String query = "";
        if (getArguments() != null) {
            query = getArguments().getString("query", "");
        }
        if (!query.isEmpty()) {
            edtSearch.setText(query);   // show in EditText
            searchProducts(query);      // immediately fetch products
        }
    }


    private double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}