package com.example.unitrade;

import static org.apache.http.client.utils.DateUtils.parseDate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitrade.backend.FetchProductId;
import com.example.unitrade.backend.Filter;
import com.example.unitrade.backend.RecommendationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import com.example.unitrade.backend.Sorting;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategory, rvRecommended;
    private CategoryAdapter categoryAdapter;
    private ItemAdapter itemAdapter;
    private List<Product> productList;
    private List<Product> allProducts;

    private FloatingActionButton btnCart;
    private BottomNavigationView bottomNav;
    private View topView;
    private View rootView;
    private MovableFabHelper mover = new MovableFabHelper();

    private String filterCondition = null; // "Used" or "Brand New"
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterDate = null; // e.g., "2026-01-08"


    // Search & Filter Views
    private EditText edtSearch, minPrice, maxPrice, dateDay, dateMonth, dateYear;

    private ImageButton btnFilter;
    private View filterPanel;
    private View filterButtonRow;
    private Button btnLatest, btnOldest, btnNearest, btnPrice, btnCampusOn, btnCampusOff, btnUsed, btnUnused,btnReset,
            btnConfirmPrice, btnConfirmDate;

    private TextView txtCategory, txtRecommended;

    // Filter State
    private String selectedPriceMode = null;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        btnCart = view.findViewById(R.id.btnCart);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        topView = requireActivity().findViewById(R.id.appBarMain);

        mover.enable(btnCart, rootView, topView, bottomNav);

        btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), ShoppingCartActivity.class)));

        rvCategory = view.findViewById(R.id.rvCategory);
        rvRecommended = view.findViewById(R.id.rvRecommended);

        // -------- CATEGORY LIST --------
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category("All", R.drawable.ic_all_category));
        categoryList.add(new Category("Textbooks", R.drawable.sample_textbooks));
        categoryList.add(new Category("Electronics", R.drawable.sample_electronics));
        categoryList.add(new Category("Fashion", R.drawable.sample_fashion));
        categoryList.add(new Category("Room Essentials", R.drawable.sample_room_essentials));
        categoryList.add(new Category("Sports", R.drawable.sample_sports));
        categoryList.add(new Category("Stationery", R.drawable.sample_stationery));
        categoryList.add(new Category("Hobbies", R.drawable.sample_hobbies));
        categoryList.add(new Category("Food", R.drawable.sample_food));
        categoryList.add(new Category("Personal Care", R.drawable.sample_personal_care));
        categoryList.add(new Category("Others", R.drawable.ic_others));

        categoryAdapter = new CategoryAdapter(categoryList,
                category -> filterProductsByCategory(category.getName()));

        rvCategory.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false));
        rvCategory.setAdapter(categoryAdapter);

        // -------- PRODUCT LISTS --------
        allProducts = new ArrayList<>();
        productList = new ArrayList<>();

        itemAdapter = new ItemAdapter(productList, product -> {
            RecommendationManager.recordClick(product.getCategory());

            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        rvRecommended.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRecommended.setAdapter(itemAdapter);

        // -------- FIRESTORE LOAD --------
        FetchProductId.searchProductsByKeyword(
                "",
                new FetchProductId.OnResultListener() {
                    @Override
                    public void onSuccess(List<Product> products) {
                        allProducts.clear();
                        allProducts.addAll(products);
                        updateRecommendations();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("HomeFragment", "Failed to load products", e);
                    }
                });

        // Initialize Search & Filter Views
        edtSearch = view.findViewById(R.id.edtSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnCampusOff = view.findViewById(R.id.btnCampusOff);
        btnCampusOn = view.findViewById(R.id.btnCampusOn);
        btnUnused = view.findViewById(R.id.btnUnused);
        btnUsed = view.findViewById(R.id.btnUsed);
        dateDay = view.findViewById(R.id.dateDay);
        dateMonth = view.findViewById(R.id.dateMonth);
        dateYear = view.findViewById(R.id.dateYear);
        minPrice = view.findViewById(R.id.minPrice);
        maxPrice = view.findViewById(R.id.maxPrice);
        filterPanel = view.findViewById(R.id.filterPanel);
        btnLatest = view.findViewById(R.id.btnLatest);
        btnOldest = view.findViewById(R.id.btnOldest);
        btnNearest = view.findViewById(R.id.btnNearest);
        btnPrice = view.findViewById(R.id.btnPrice);
        txtCategory = view.findViewById(R.id.txtCategory);
        txtRecommended = view.findViewById(R.id.txtRecommended);

        // CONFIRM BUTTON
        btnConfirmDate = view.findViewById(R.id.btnConfirmDate);
        btnConfirmPrice = view.findViewById(R.id.btnConfirmPrice);
        btnReset = view.findViewById(R.id.btnReset);

        // -------- SEARCH LOGIC --------
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Toggle Filter Panel
        btnFilter.setOnClickListener(v -> {
            boolean isVisible = filterPanel.getVisibility() == View.VISIBLE;
            filterPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        // -------- SORTING BUTTONS --------
        // -------- LOCATION BUTTONS --------
        btnNearest.setOnClickListener(v -> {
            if (!checkLocationPermission()) return;

            filterByNearest();
        });


        // -------- PRICE BUTTONS --------
        btnPrice.setOnClickListener(v -> showPriceDropdown());

        btnConfirmPrice.setOnClickListener(v -> {
            String min = minPrice.getText().toString().trim();
            String max = maxPrice.getText().toString().trim();

            if (!min.isEmpty() && !max.isEmpty()) {
                filterMinPrice = Double.parseDouble(min);
                filterMaxPrice = Double.parseDouble(max);
                applyFilters();
                minPrice.getText().clear();
                maxPrice.getText().clear();
            } else {
                Toast.makeText(getContext(), "Enter both min and max", Toast.LENGTH_SHORT).show();
            }
        });

        // -------- DATE BUTTONS --------
        btnLatest.setOnClickListener(v -> {
            Sorting.sortByLatest(productList);
            itemAdapter.notifyDataSetChanged();
            resetButtonStyle(btnOldest);
            applySelectedStyle(btnLatest);
            selectedPriceMode = null;
            btnPrice.setText("Price ▼");
        });

        btnOldest.setOnClickListener(v -> {
            Sorting.sortByOldest(productList);
            itemAdapter.notifyDataSetChanged();
            resetButtonStyle(btnLatest);
            applySelectedStyle(btnOldest);
        });

        btnConfirmDate.setOnClickListener(v -> {
            String dayStr = dateDay.getText().toString().trim();
            String monthStr = dateMonth.getText().toString().trim();
            String yearStr = dateYear.getText().toString().trim();

            if (!dayStr.isEmpty() && !monthStr.isEmpty() && !yearStr.isEmpty()) {
                Filter filter = new Filter();
                filter.dateAfter(dayStr, monthStr, yearStr, products -> {

                    productList.clear();
                    productList.addAll(applySearchIfAny(products));

                    itemAdapter.notifyDataSetChanged();
                    resetButtonStyle(btnUnused);
                    applySelectedStyle(btnUnused);
                    dateDay.getText().clear();
                    dateMonth.getText().clear();
                    dateYear.getText().clear();
                });
            } else {
                Toast.makeText(getContext(), "Please fill all date fields", Toast.LENGTH_SHORT).show();
            }
        });


        // -------- CONDITION BUTTONS --------
        btnUsed.setOnClickListener(v -> {
            filterCondition = "Used";

            // Fetch from Firestore
            Filter filter = new Filter();
            filter.chooseCondition(filterCondition, products -> {
                // apply local filters like search, price, date on top of Firestore result
                List<Product> filtered = new ArrayList<>(products);

                // Optional: apply search
                filtered = filterBySearch(filtered);
                // Optional: apply price filter
                filtered = filterByPrice(filtered);
                // Optional: apply date filter
                filtered = filterByDate(filtered);

                productList.clear();
                productList.addAll(filtered);
                itemAdapter.notifyDataSetChanged();
            });

            resetButtonStyle(btnUnused);
            applySelectedStyle(btnUsed);
        });

        btnUnused.setOnClickListener(v -> {
            filterCondition = "Unused";

            Filter filter = new Filter();
            filter.chooseCondition(filterCondition, products -> {
                List<Product> filtered = new ArrayList<>(products);
                filtered = filterBySearch(filtered);
                filtered = filterByPrice(filtered);
                filtered = filterByDate(filtered);

                productList.clear();
                productList.addAll(filtered);
                itemAdapter.notifyDataSetChanged();
            });

            resetButtonStyle(btnUsed);
            applySelectedStyle(btnUnused);
        });



        btnReset.setOnClickListener(v -> {
            // Clear all filter states
            filterCondition = null;
            filterMinPrice = null;
            filterMaxPrice = null;
            filterDate = null;

            // Clear search box
            edtSearch.getText().clear();

            // Reset any UI styles on buttons
            resetButtonStyle(btnUsed);
            resetButtonStyle(btnUnused);
            resetButtonStyle(btnPrice);
            resetButtonStyle(btnLatest);
            resetButtonStyle(btnOldest);
            resetButtonStyle(btnNearest);

            // Reset text on price button
            btnPrice.setText("Price ▼");

            // Reapply filters (which are now all cleared)
            applyFilters();
        });

    }


    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Home Mode
            rvCategory.setVisibility(View.VISIBLE);
            txtCategory.setVisibility(View.VISIBLE);
            txtRecommended.setText("Recommended For You");
            updateRecommendations();
        } else {
            // Search Mode
            rvCategory.setVisibility(View.GONE);
            txtCategory.setVisibility(View.GONE);
            txtRecommended.setText("Search Results");

            productList.clear();
            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                    productList.add(p);
                }
            }
            itemAdapter.notifyDataSetChanged();
        }
    }

    private void showPriceDropdown() {
        PopupMenu menu = new PopupMenu(getContext(), btnPrice);
        menu.getMenu().add("Lowest");
        menu.getMenu().add("Highest");
        menu.setOnMenuItemClickListener(item -> {
            selectedPriceMode = item.getTitle().toString();
            btnPrice.setText(selectedPriceMode);

            boolean ascending = selectedPriceMode.equalsIgnoreCase("Lowest");
            Sorting.sortByPrice(productList, ascending);
            itemAdapter.notifyDataSetChanged();

            resetButtonStyle(btnPrice);
            applySelectedStyle(btnPrice);
            return true;
        });
        menu.show();
    }

    private void resetButtonStyle(Button b) {
        b.setBackgroundResource(R.drawable.bg_filter_chip);
        b.setTextColor(Color.BLACK);
    }

    private void applySelectedStyle(Button b) {
        b.setBackgroundResource(R.drawable.bg_filter_chip_selected);
        b.setTextColor(Color.WHITE);
    }

    private double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // -------- FILTER BY CATEGORY --------
    private void filterProductsByCategory(String categoryName) {
        productList.clear();

        if (categoryName.equals("All")) {
            productList.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.getCategory().equalsIgnoreCase(categoryName)) {
                    productList.add(p);
                }
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

    // -------- RECOMMENDATION --------
    private void updateRecommendations() {
        // Include all products
        productList.clear();
        productList.addAll(allProducts);

        // Sort products by category click count descending
        RecommendationManager.sortByRecommendation(productList);

        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecommendations();
    }

    private List<Product> applySearchIfAny(List<Product> source) {
        String query = edtSearch.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            return source;
        }

        List<Product> result = new ArrayList<>();
        for (Product p : source) {
            if (p.getName().toLowerCase().contains(query)) {
                result.add(p);
            }
        }
        return result;
    }

    private void applyFilters() {
        List<Product> filtered = new ArrayList<>(allProducts);

        // Condition
        if (filterCondition != null) {
            filtered.removeIf(p -> !p.getCondition().equalsIgnoreCase(filterCondition));
        }

        // Price
        if (filterMinPrice != null && filterMaxPrice != null) {
            filtered.removeIf(p -> p.getPrice() < filterMinPrice || p.getPrice() > filterMaxPrice);
        }

        // Date
        if (filterDate != null) {
            Date filterDateObj = parseDate(filterDate);
            filtered.removeIf(p -> p.getListingDate().before(filterDateObj));
        }

        // Search query
        String query = edtSearch.getText().toString().trim().toLowerCase();
        if (!query.isEmpty()) {
            filtered.removeIf(p -> !p.getName().toLowerCase().contains(query));
        }

        // Update RecyclerView
        productList.clear();
        productList.addAll(filtered);
        itemAdapter.notifyDataSetChanged();
    }

    // Helper for search filter
    private List<Product> filterBySearch(List<Product> products) {
        String query = edtSearch.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) return products;

        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(query)) {
                result.add(p);
            }
        }
        return result;
    }

    // Helper for price filter
    private List<Product> filterByPrice(List<Product> products) {
        if (filterMinPrice == null || filterMaxPrice == null) return products;

        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getPrice() >= filterMinPrice && p.getPrice() <= filterMaxPrice) {
                result.add(p);
            }
        }
        return result;
    }

    // Helper for date filter
    private List<Product> filterByDate(List<Product> products) {
        if (filterDate == null) return products;

        Date filterDateObj = parseDate(filterDate);
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (!p.getListingDate().before(filterDateObj)) {
                result.add(p);
            }
        }
        return result;
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // User denied before but didn't select "Don't ask again"
                Toast.makeText(getContext(),
                        "Location permission is needed to find nearby items.",
                        Toast.LENGTH_SHORT).show();
            }

            // Request permission
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true; // Permission already granted
    }

    private void filterByNearest() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Permission is granted, just display all products
        productList.clear();
        productList.addAll(allProducts);
        itemAdapter.notifyDataSetChanged();

        // Update button style
        resetButtonStyle(btnNearest);
        applySelectedStyle(btnNearest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry Nearest filter
                filterByNearest();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
