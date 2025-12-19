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

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        btnCart = view.findViewById(R.id.btnCart);
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        topView = requireActivity().findViewById(R.id.appBarMain);

        mover.enable(btnCart, rootView, topView, bottomNav);

        btnCart.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ShoppingCartActivity.class))
        );

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

        categoryAdapter =
                new CategoryAdapter(categoryList,
                        category -> filterProductsByCategory(category.getName()));

        rvCategory.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false)
        );
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
                }
        );

        // -------- SEARCH --------
        EditText edtSearch = view.findViewById(R.id.edtSearch);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            // Inside your edtSearch.setOnEditorActionListener
            if (isEnter) {
                String query = edtSearch.getText().toString().trim();

                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                Log.d("SearchDebug", "User pressed Enter. Query: " + query);

                try {
                    // Use the action ID you defined in nav_graph.xml
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(
                            R.id.action_homeFragment_to_searchFragment,
                            bundle);
                    Log.d("SearchDebug", "Navigation successful");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SearchDebug", "Navigation failed: " + e.getMessage());
                }

                return true; // handled Enter
            }

            return false;
        });
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
        List<String> topCategories =
                RecommendationManager.getTopCategories(2);

        productList.clear();

        if (topCategories.isEmpty()) {
            productList.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (topCategories.contains(p.getCategory())) {
                    productList.add(p);
                }
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecommendations();
    }
}
