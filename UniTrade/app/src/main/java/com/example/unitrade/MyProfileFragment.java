package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public final class MyProfileFragment extends Fragment {

    // ---------------------------------
    // USER + PRODUCT LIST
    // ---------------------------------
    private User viewedUser;

    private RecyclerView recyclerMyProducts;
    private ProfileProductAdapter productAdapter;
    private final List<Product> userProducts = new ArrayList<>();

    private LinearLayout emptyState;   // <-- your include view
    private TextView txtEmptyTitle, txtEmptySubtitle;
    private Button btnSellItem;


    // ---------------------------------
    // LIFECYCLE
    // ---------------------------------
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        viewedUser = SampleData.getUserById(requireContext(), "u1");

        bindViews(v);
        setupRecycler();
        loadUserProducts();
        updateUI();
        showUserData();
    }


    // ---------------------------------
    // BIND VIEWS
    // ---------------------------------
    private void bindViews(View v) {

        recyclerMyProducts = v.findViewById(R.id.recyclerMyProducts);

        // EMPTY STATE VIEWS
        emptyState = v.findViewById(R.id.emptyListingState);
        txtEmptyTitle = emptyState.findViewById(R.id.txtEmptyTitle);
        txtEmptySubtitle = emptyState.findViewById(R.id.txtEmptySubtitle);
        btnSellItem = emptyState.findViewById(R.id.btnShopNow);

        btnSellItem.setText("List an Item");
        btnSellItem.setOnClickListener(view -> openSellFragmentForNew());
    }


    // ---------------------------------
    // SETUP RECYCLER
    // ---------------------------------
    private void setupRecycler() {

        recyclerMyProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productAdapter = new ProfileProductAdapter(
                requireContext(),
                userProducts
        );

        recyclerMyProducts.setAdapter(productAdapter);

        productAdapter.setOnProductActionListener(new ProfileProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product p) {
                requestEdit(p);   // 📌 open SellFragment for editing
            }

            @Override
            public void onDelete(Product p) {
                // Optional — if you want delete later
            }
        });
    }


    // ---------------------------------
    // LOAD USER PRODUCTS
    // ---------------------------------
    private void loadUserProducts() {
        userProducts.clear();

        for (Product p : SampleData.generateSampleProducts(requireContext())) {
            if (p.getSellerId().equals(viewedUser.getId())) {
                userProducts.add(p);
            }
        }
    }


    // ---------------------------------
    // UPDATE UI BASED ON ITEM COUNT
    // ---------------------------------
    private void updateUI() {
        if (userProducts.isEmpty()) {
            recyclerMyProducts.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

            txtEmptyTitle.setText("No listings yet");
            txtEmptySubtitle.setText("Start listing your items.");

        } else {
            recyclerMyProducts.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }


    // ---------------------------------
    // CLICK ACTION: EDIT LISTING
    // ---------------------------------
    private void openSellFragmentForEdit(Product product) {

        Intent intent = new Intent(getActivity(), MainActivity.class);

        intent.putExtra("editMode", true);
        intent.putExtra("product_to_edit", product);
        intent.putExtra("openSellFragment", true);
        intent.putExtra("fromExternal", true);

        startActivity(intent);
    }


    // ---------------------------------
    // CLICK ACTION: ADD NEW LISTING
    // ---------------------------------
    private void openSellFragmentForNew() {

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("openSellFragment", true);
        intent.putExtra("fromExternal", true);

        startActivity(intent);
    }


    // ---------------------------------
    // SHOW USER DATA (PROFILE HEADER)
    // ---------------------------------
    private void showUserData() {
        // If you want I can fill your full header code here.
    }


    // ---------------------------------
    // PUBLIC METHODS USED BY ADAPTER
    // ---------------------------------
    public void requestEdit(Product p) {
        openSellFragmentForEdit(p);
    }

    public void requestDelete(Product p) {

        ConfirmDialog.show(
                getContext(),
                "Delete Listing?",
                "Do you want to delete this item?",
                "Delete",
                () -> {
                    userProducts.remove(p);
                    productAdapter.notifyDataSetChanged();
                    updateUI();
                }
        );
    }
}
