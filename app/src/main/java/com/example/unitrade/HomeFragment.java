package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategory, rvRecommended;
    private CategoryAdapter categoryAdapter;
    private ItemAdapter itemAdapter;

    private final List<Product> productList = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();

    private FloatingActionButton btnCart;
    private BottomNavigationView bottomNav;
    private View topView, rootView;

    private final MovableFabHelper mover = new MovableFabHelper();

    public HomeFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
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

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ShoppingCartActivity.class);
            startActivity(intent);
        });

        rvCategory = view.findViewById(R.id.rvCategory);
        rvRecommended = view.findViewById(R.id.rvRecommended);

        setupCategories();
        loadAvailableProducts();
        setupItemAdapter();

        setupSearch(view);
    }

    // ======================================================
    // SEARCH
    // ======================================================
    private void setupSearch(View view) {
        EditText edtSearch = view.findViewById(R.id.edtSearch);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {

            boolean isEnter =
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                            (event != null
                                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (!isEnter) return false;

            String query = edtSearch.getText().toString().trim();

            Bundle b = new Bundle();
            b.putString("query", query);

            if (UserSession.get() != null) {
                b.putString("currentUserId", UserSession.get().getId());
            }

            NavController navController =
                    Navigation.findNavController(requireView());

            navController.navigate(R.id.nav_search, b);
            return true;
        });
    }

    // ======================================================
    // CATEGORY SETUP
    // ======================================================
    private void setupCategories() {

        List<Category> categories = new ArrayList<>();
        categories.add(new Category("All", R.drawable.ic_all_category));
        categories.add(new Category("Textbook", R.drawable.sample_textbooks));
        categories.add(new Category("Electronics", R.drawable.sample_electronics));
        categories.add(new Category("Fashion", R.drawable.sample_fashion));
        categories.add(new Category("Room Essentials", R.drawable.sample_room_essentials));
        categories.add(new Category("Sports", R.drawable.sample_sports));
        categories.add(new Category("Stationery", R.drawable.sample_stationery));
        categories.add(new Category("Hobbies", R.drawable.sample_hobbies));
        categories.add(new Category("Food", R.drawable.sample_food));
        categories.add(new Category("Personal Care", R.drawable.sample_personal_care));
        categories.add(new Category("Others", R.drawable.ic_others));

        categoryAdapter = new CategoryAdapter(
                categories,
                category -> filterProductsByCategory(category.getName())
        );

        rvCategory.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        rvCategory.setAdapter(categoryAdapter);
    }

    // ======================================================
    // LOAD PRODUCTS (EXCLUDING CURRENT USER)
    // ======================================================
    private void loadAvailableProducts() {

        String currentUserId =
                UserSession.get() != null
                        ? UserSession.get().getId()
                        : "";

        allProducts =
                SampleData.getAvailableItems(
                        requireContext(),
                        currentUserId
                );

        productList.clear();
        productList.addAll(allProducts);
    }

    // ======================================================
    // ITEM ADAPTER (FIXED: uses product_id)
    // ======================================================
    private void setupItemAdapter() {

        itemAdapter = new ItemAdapter(productList, product -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            ProductDetailActivity.class
                    );

            intent.putExtra("product_id", product.getId());

            startActivity(intent);
        });

        rvRecommended.setLayoutManager(
                new GridLayoutManager(getContext(), 2)
        );

        rvRecommended.setAdapter(itemAdapter);
    }

    // ======================================================
    // FILTER BY CATEGORY
    // ======================================================
    private void filterProductsByCategory(String categoryName) {

        productList.clear();

        if ("All".equalsIgnoreCase(categoryName)) {
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

    // ======================================================
    // REFRESH WHEN RETURNING
    // ======================================================
    @Override
    public void onResume() {
        super.onResume();

        loadAvailableProducts();

        productList.clear();
        productList.addAll(allProducts);

        if (itemAdapter != null) {
            itemAdapter.notifyDataSetChanged();
        }

        btnCart.post(() ->
                mover.enable(btnCart, rootView, topView, bottomNav)
        );
    }
}
