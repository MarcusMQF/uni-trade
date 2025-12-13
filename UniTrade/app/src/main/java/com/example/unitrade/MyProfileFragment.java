package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;
import java.util.List;

public final class MyProfileFragment extends Fragment {

    // -------------------------------------------------
    // USER + DATA
    // -------------------------------------------------
    private User viewedUser;
    private final List<Product> userProducts = new ArrayList<>();

    // -------------------------------------------------
    // UI
    // -------------------------------------------------
    private ImageView imgProfile;
    private TextView txtUserName, txtFullName, txtEmail, txtPhone, txtRating;

    private TextView tabActive, tabCompleted;
    private RecyclerView recyclerMyProducts;
    private ProfileProductAdapter productAdapter;

    private LinearLayout emptyState;
    private Button btnSellItem;
    private Button btnManageListings;

    private ImageButton btnEditProfile;

    private ImageView imgReviews, imgShoppingCart, imgHistory;


    // -------------------------------------------------
    // STATE
    // -------------------------------------------------
    private boolean manageMode = false;
    private boolean isActiveTab = true;


    private TextView txtAddress;
    // =================================================
    // LIFECYCLE
    // =================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View v,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(v, savedInstanceState);

        // -------------------------------------------------
        // LOAD CURRENT USER
        // -------------------------------------------------
        viewedUser = UserSession.get();
        if (viewedUser == null) {
            viewedUser = SampleData.getUserById(requireContext(), "u1");
        }

        bindViews(v);
        setupRecycler();
        showUserData();
        showActiveItems();
    }

    // =================================================
    // VIEW BINDING
    // =================================================

    private void bindViews(@NonNull View v) {

        imgProfile = v.findViewById(R.id.imgProfile);
        txtUserName = v.findViewById(R.id.txtUsername);
        txtFullName = v.findViewById(R.id.txtFullName);
        txtEmail = v.findViewById(R.id.txtEmail);
        txtPhone = v.findViewById(R.id.txtPhone);
        txtRating = v.findViewById(R.id.txtRating);
        txtAddress = v.findViewById(R.id.txtAddress);

        recyclerMyProducts = v.findViewById(R.id.recyclerMyProducts);
        btnManageListings = v.findViewById(R.id.btnManageListings);

        emptyState = v.findViewById(R.id.emptyListingState);
        btnSellItem = emptyState.findViewById(R.id.btnShopNow);

        tabActive = v.findViewById(R.id.tabActive);
        tabCompleted = v.findViewById(R.id.tabCompleted);

        imgReviews = v.findViewById(R.id.imgReviews);
        imgShoppingCart = v.findViewById(R.id.imgShoppingCart);
        imgHistory = v.findViewById(R.id.imgHistory);


        btnSellItem.setText("List an Item");
        btnSellItem.setOnClickListener(v1 -> openSellFragmentForNew());

        btnManageListings.setOnClickListener(v1 -> toggleManageMode());
        btnEditProfile = v.findViewById(R.id.btnEditProfile);





        btnEditProfile.setOnClickListener(v1 -> openEditProfile());
        btnSellItem.setOnClickListener(v1 -> openSellFragmentForNew());
        btnManageListings.setOnClickListener(v1 -> toggleManageMode());

        imgReviews.setOnClickListener(view -> {
            String userId = viewedUser.getId();   // current logged-in user

            Intent intent = new Intent(requireContext(), RatingReviewsActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        imgShoppingCart.setOnClickListener(view -> {
            // Open Cart screen
            Intent intent = new Intent(requireContext(), ShoppingCartActivity.class);
            startActivity(intent);
        });

        imgHistory.setOnClickListener(view -> {
            // Open History screen
            Intent intent = new Intent(requireContext(), HistoryActivity.class);
            startActivity(intent);
        });

        tabActive.setOnClickListener(v1 -> showActiveItems());
        tabCompleted.setOnClickListener(v1 -> showCompletedItems());
    }

    // =================================================
    // USER HEADER
    // =================================================

    private void showUserData() {
        txtUserName.setText(viewedUser.getUsername());
        txtFullName.setText(viewedUser.getFullName());
        txtEmail.setText(viewedUser.getEmail());
        txtPhone.setText(viewedUser.getPhoneNumber());
        txtRating.setText(String.format("%.1f rating", viewedUser.getOverallRating()));

        List<Address> addresses = viewedUser.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            txtAddress.setText("No address set");
        } else {
            Address defaultAddress = null;

            for (Address a : addresses) {
                if (a.isDefault()) {
                    defaultAddress = a;
                    break;
                }
            }

            if (defaultAddress != null) {
                txtAddress.setText(defaultAddress.getAddress());
            } else {
                txtAddress.setText(addresses.get(0).getAddress());
            }
        }

        Glide.with(requireContext())
                .load(viewedUser.getProfileImageUrl())
                .signature(new ObjectKey(viewedUser.getProfileImageVersion()))
                .circleCrop()
                .into(imgProfile);
    }

    // =================================================
    // RECYCLER
    // =================================================

    private void setupRecycler() {

        recyclerMyProducts.setLayoutManager(
                new GridLayoutManager(requireContext(), 2));

        productAdapter = new ProfileProductAdapter(
                requireContext(), userProducts);

        recyclerMyProducts.setAdapter(productAdapter);

        productAdapter.setOnProductActionListener(
                new ProfileProductAdapter.OnProductActionListener() {

                    @Override
                    public void onEdit(Product p) {
                        openSellFragmentForEdit(p);
                    }

                    @Override
                    public void onDelete(Product p) {
                        requestDelete(p);
                    }
                }
        );
    }

    // =================================================
    // TAB LOGIC
    // =================================================

    private void showActiveItems() {
        isActiveTab = true;
        styleTabs(true);

        userProducts.clear();
        userProducts.addAll(
                SampleData.getActiveItems(
                        requireContext(),
                        viewedUser.getId()
                )
        );

        productAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void showCompletedItems() {
        isActiveTab = false;
        styleTabs(false);

        userProducts.clear();
        userProducts.addAll(
                SampleData.getCompletedItems(
                        requireContext(),
                        viewedUser.getId()
                )
        );

        productAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void styleTabs(boolean active) {
        tabActive.setBackgroundResource(
                active ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabCompleted.setBackgroundResource(
                active ? R.drawable.bg_tab_unselected : R.drawable.bg_tab_selected);

        tabActive.setTextColor(active ? Color.parseColor("#009688") : Color.parseColor("#666666"));
        tabCompleted.setTextColor(active ? Color.parseColor("#666666") : Color.parseColor("#009688"));
    }

    // =================================================
    // UI STATE
    // =================================================

    private void updateUI() {
        if (userProducts.isEmpty()) {
            recyclerMyProducts.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            btnManageListings.setVisibility(View.GONE);
        } else {
            recyclerMyProducts.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            btnManageListings.setVisibility(View.VISIBLE);
        }
    }

    private void toggleManageMode() {
        manageMode = !manageMode;
        productAdapter.setManageMode(manageMode);
        btnManageListings.setText(manageMode ? "Done" : "Manage Listings");
    }

    // =================================================
    // SELL FRAGMENT NAVIGATION
    // =================================================

    private void openSellFragmentForEdit(Product product) {
        Intent i = new Intent(requireActivity(), MainActivity.class);
        i.putExtra("editMode", true);
        i.putExtra("product_id", product.getId());
        i.putExtra("openSellFragment", true);
        i.putExtra("origin", "my_listings");
        i.putExtra("selectTab", "profile");
        startActivity(i);
    }

    private void openSellFragmentForNew() {
        Intent i = new Intent(requireActivity(), MainActivity.class);
        i.putExtra("editMode", false);
        i.putExtra("openSellFragment", true);
        i.putExtra("origin", "new_listing");
        i.putExtra("selectTab", "profile");
        startActivity(i);
    }

    // =================================================
    // DELETE
    // =================================================

    private void requestDelete(Product p) {
        ConfirmDialog.show(
                requireContext(),
                "Delete Listing?",
                "Do you want to delete this item?",
                "Delete",
                () -> {
                    SampleData.deleteProduct(requireContext(), p.getId());
                    userProducts.remove(p);
                    productAdapter.notifyDataSetChanged();
                    updateUI();
                }
        );
    }

    // =================================================
    // RESUME (SYNC DATA)
    // =================================================

    @Override
    public void onResume() {
        super.onResume();

        viewedUser = UserSession.get();

        if (viewedUser == null) {
            viewedUser = SampleData.getUserById(requireContext(), "u1");
        }

        showUserData();

        // --------------------
        // Existing product sync
        // --------------------
        userProducts.clear();

        if (isActiveTab) {
            userProducts.addAll(
                    SampleData.getActiveItems(requireContext(), viewedUser.getId())
            );
        } else {
            userProducts.addAll(
                    SampleData.getCompletedItems(requireContext(), viewedUser.getId())
            );
        }

        productAdapter.notifyDataSetChanged();

        if (manageMode) {
            manageMode = false;
            btnManageListings.setText("Manage Listings");
            productAdapter.setManageMode(false);
        }
    }

    private void openEditProfile() {
        Intent i = new Intent(requireActivity(), EditProfileActivity.class);
        i.putExtra("user_id", viewedUser.getId());   // <-- send only ID
        startActivity(i);
    }

}
