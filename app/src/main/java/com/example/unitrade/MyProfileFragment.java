package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public final class MyProfileFragment extends Fragment {

    private FirebaseFirestore db;

    private User viewedUser;
    private final List<Product> userProducts = new ArrayList<>();

    private ImageView imgProfile;
    private TextView txtUserName, txtFullName, txtEmail, txtPhone, txtRating, txtAddress, txtUserDescription;
    private TextView tabActive, tabCompleted;
    private RecyclerView recyclerMyProducts;
    private ProfileProductAdapter productAdapter;
    private LinearLayout emptyState;
    private Button btnSellItem, btnManageListings;
    private ImageButton btnEditProfile;
    private ImageView imgReviews, imgShoppingCart, imgHistory;
    private android.widget.ProgressBar progressBar;
    private View profileContent;

    private boolean manageMode = false;
    private boolean isActiveTab = true;

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

        db = FirebaseFirestore.getInstance();
        bindViews(v);
        setupRecycler();

        // Get current Firebase user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();
        loadUser(userId);
    }

    private void loadUser(String userId) {
        if (progressBar != null && profileContent != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileContent.setVisibility(View.GONE);
        }

        UserRepository.getUserByUid(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                viewedUser = user;
                if (isAdded()) {
                    showUserData();
                    showActiveItems(); // default tab
                    if (progressBar != null && profileContent != null) {
                        progressBar.setVisibility(View.GONE);
                        profileContent.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load user", Toast.LENGTH_SHORT).show();
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                }
                Log.e("MyProfile", "User fetch error", e);
            }
        });
    }

    private void bindViews(@NonNull View v) {
        progressBar = v.findViewById(R.id.progressBar);
        profileContent = v.findViewById(R.id.profileContent);

        imgProfile = v.findViewById(R.id.imgProfile);
        txtUserName = v.findViewById(R.id.txtUsername);
        txtFullName = v.findViewById(R.id.txtFullName);
        txtEmail = v.findViewById(R.id.txtEmail);
        txtPhone = v.findViewById(R.id.txtPhone);
        txtRating = v.findViewById(R.id.txtRating);
        txtAddress = v.findViewById(R.id.txtAddress);
        txtUserDescription = v.findViewById(R.id.txtUserDescription);

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

        imgReviews.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), RatingReviewsActivity.class);
            intent.putExtra("user_id", viewedUser.getId());
            startActivity(intent);
        });

        imgShoppingCart
                .setOnClickListener(view -> startActivity(new Intent(requireContext(), ShoppingCartActivity.class)));
        imgHistory.setOnClickListener(view -> startActivity(new Intent(requireContext(), HistoryActivity.class)));

        tabActive.setOnClickListener(v1 -> showActiveItems());
        tabCompleted.setOnClickListener(v1 -> showCompletedItems());
    }

    private void showUserData() {
        txtUserName.setText(viewedUser.getUsername());
        txtFullName.setText(viewedUser.getFullName());
        txtEmail.setText(viewedUser.getEmail());
        txtPhone.setText(viewedUser.getPhoneNumber());
        txtRating.setText(String.format("%.1f", viewedUser.getOverallRating()));

        String bio = viewedUser.getBio();
        if (bio != null && !bio.isEmpty()) {
            txtUserDescription.setText(bio);
        } else {
            txtUserDescription.setText("No bio available.");
        }

        List<Address> addresses = viewedUser.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            txtAddress.setText("No address set");
        } else {
            Address defaultAddress = null;
            for (Address a : addresses)
                if (a.isDefault()) {
                    defaultAddress = a;
                    break;
                }
            txtAddress.setText(defaultAddress != null ? defaultAddress.getAddress() : addresses.get(0).getAddress());
        }

        Glide.with(requireContext())
                .load(viewedUser.getProfileImageUrl())
                .signature(new ObjectKey(viewedUser.getProfileImageVersion()))
                .circleCrop()
                .into(imgProfile);
    }

    private void setupRecycler() {
        recyclerMyProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProfileProductAdapter(requireContext(), userProducts);
        recyclerMyProducts.setAdapter(productAdapter);

        productAdapter.setOnProductActionListener(new ProfileProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product p) {
                openSellFragmentForEdit(p);
            }

            @Override
            public void onDelete(Product p) {
                requestDelete(p);
            }
        });
    }

    private void showActiveItems() {
        isActiveTab = true;
        styleTabs(true);
        loadProductsRealtime("Available");
    }

    private void showCompletedItems() {
        isActiveTab = false;
        styleTabs(false);
        loadProductsRealtime("Sold");
    }

    private void styleTabs(boolean active) {
        tabActive.setBackgroundResource(active ? R.drawable.bg_tab_selected_pill : 0);
        tabCompleted.setBackgroundResource(active ? 0 : R.drawable.bg_tab_selected_pill);

        tabActive.setTextColor(active ? Color.parseColor("#009688") : Color.parseColor("#95A5A6"));
        tabCompleted.setTextColor(active ? Color.parseColor("#95A5A6") : Color.parseColor("#009688"));
    }

    private void loadProductsRealtime(String status) {
        if (viewedUser == null) {
            Log.e("MyProfile", "viewedUser is null! Cannot fetch products.");
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Log.e("MyProfile", "No logged-in user. Cannot fetch products.");
            return;
        }

        String uid = firebaseUser.getUid();

        String userId = viewedUser.getId();
        Log.d("MyProfile", "Fetching products for sellerId=" + userId + ", status=" + status);
        db.collection("products")
                .whereEqualTo("sellerId", uid)
                .whereEqualTo("status", status) // comment out for testing
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("MyProfile", "Error fetching products", error);
                        return;
                    }
                    userProducts.clear();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Product p = doc.toObject(Product.class);
                            userProducts.add(p);
                            Log.d("MyProfile", "Loaded product: " + p.getName() + ", status=" + p.getStatus());
                        }
                    }
                    productAdapter.notifyDataSetChanged();
                    updateUI();
                });
    }

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

    private void requestDelete(Product p) {
        ConfirmDialog.show(
                requireContext(),
                "Delete Listing?",
                "Are you sure you want to delete this listing? This action cannot be undone.",
                "Delete",
                () -> db.collection("products")
                        .document(p.getId())
                        .delete()
                        .addOnSuccessListener(v -> {
                            Toast.makeText(getContext(), "Listing deleted", Toast.LENGTH_SHORT).show();
                            userProducts.remove(p);
                            productAdapter.notifyDataSetChanged();
                            updateUI();
                        })
                        .addOnFailureListener(e -> Toast
                                .makeText(getContext(), "Failed to delete listing", Toast.LENGTH_SHORT).show()));
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    User updatedUser = result.getData().getParcelableExtra("updated_user");
                    if (updatedUser != null) {
                        viewedUser = updatedUser;
                        UserSession.set(updatedUser); // Ensure session is updated too
                        showUserData();
                    }
                }
            });

    private void openEditProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null)
            return;

        Intent i = new Intent(requireActivity(), EditProfileActivity.class);
        i.putExtra("userId", firebaseUser.getUid());
        editProfileLauncher.launch(i);
    }
}
