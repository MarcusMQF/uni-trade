package com.example.unitrade;

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
import com.example.unitrade.backend.RecommendationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
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

    // Search & Filter Views
    private EditText edtSearch;
    private ImageButton btnFilter;
    private View filterPanel;
    private View filterButtonRow;
    private Button btnLatest, btnNearest, btnPrice;
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
        filterPanel = view.findViewById(R.id.filterPanel);
        filterButtonRow = view.findViewById(R.id.filterButtonRow);
        btnLatest = view.findViewById(R.id.btnLatest);
        btnNearest = view.findViewById(R.id.btnNearest);
        btnPrice = view.findViewById(R.id.btnPrice);
        txtCategory = view.findViewById(R.id.txtCategory);
        txtRecommended = view.findViewById(R.id.txtRecommended);

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
        btnLatest.setOnClickListener(v -> {
            Sorting.sortByLatest(productList);
            itemAdapter.notifyDataSetChanged();
            resetButtonStyle(btnLatest);
            resetButtonStyle(btnNearest);
            resetButtonStyle(btnPrice);
            selectedPriceMode = null;
            btnPrice.setText("price ▼");
        });

        btnNearest.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location == null) {
                    Toast.makeText(getContext(), "Unable to get your location", Toast.LENGTH_SHORT).show();
                    return;
                }

                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Product> nearby = new ArrayList<>();

                for (Product p : productList) { // Filter current list
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(p.getLocation(), 1);
                        if (!addresses.isEmpty()) {
                            double sellerLat = addresses.get(0).getLatitude();
                            double sellerLng = addresses.get(0).getLongitude();
                            if (distanceInKm(userLat, userLng, sellerLat, sellerLng) <= 5) {
                                nearby.add(p);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                productList.clear();
                productList.addAll(nearby);
                itemAdapter.notifyDataSetChanged();

                resetButtonStyle(btnNearest);
                applySelectedStyle(btnNearest);
            });
        });

        btnPrice.setOnClickListener(v -> showPriceDropdown());
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
}
