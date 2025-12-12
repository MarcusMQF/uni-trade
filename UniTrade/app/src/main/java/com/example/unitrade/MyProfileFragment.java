package com.example.unitrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyProfileFragment extends Fragment {

    private static final String USER_PREFS = "user_prefs";
    private static final String USER_KEY = "user_profile";

    // ───────────────────────────────
    // USER DATA
    // ───────────────────────────────
    private User viewedUser;

    // ───────────────────────────────
    // UI COMPONENTS
    // ───────────────────────────────
    private ImageView imgProfile;
    private ImageButton btnEditProfile;

    private TextView txtUsername;
    private TextView txtUserDescription;

    private TextView txtFullName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtAddress;

    private TextView txtLastSeen;

    // ⭐ NEW: Rating UI
    private ImageView star1, star2, star3, star4, star5;
    private TextView txtRating;

    private Handler lastSeenHandler = new Handler(Looper.getMainLooper());
    private Runnable lastSeenRunnable;

    // ───────────────────────────────
    // ACTIVITY RESULT LAUNCHER
    // ───────────────────────────────
    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::handleEditProfileResult);

    // ───────────────────────────────
    // FRAGMENT LIFECYCLE
    // ───────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewedUser = loadUserFromPrefs();
        if (viewedUser == null) {
            viewedUser = SampleData.getUserById(requireContext(), "u1");
        }

        // Load the last seen time from shared preferences
        loadLastSeenTimestamp();

        bindViews(view);
        showUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLastSeenUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLastSeenUpdates();
    }

    // ───────────────────────────────
    // BIND UI VIEWS
    // ───────────────────────────────
    private void bindViews(View view) {

        imgProfile = view.findViewById(R.id.imgProfile);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        txtUsername = view.findViewById(R.id.txtUsername);
        txtLastSeen = view.findViewById(R.id.txtLastSeen);
        txtUserDescription = view.findViewById(R.id.txtUserDescription);

        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtAddress = view.findViewById(R.id.txtAddress);

        // ⭐ BIND STARS + RATING
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);
        txtRating = view.findViewById(R.id.txtRating);

        LinearLayout reviewButton = view.findViewById(R.id.reviewButton);
        LinearLayout cartButton = view.findViewById(R.id.cartButton);
        LinearLayout historyButton = view.findViewById(R.id.historyButton);

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("user_to_edit", viewedUser);
                editProfileLauncher.launch(intent);
            });
        }

        reviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RatingReviewsActivity.class);
            intent.putExtra("user_to_view", viewedUser);
            intent.putExtra("hide_fab", true);
            startActivity(intent);
        });

        cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShoppingCartActivity.class);
            intent.putParcelableArrayListExtra("cart", new ArrayList<>(CartManager.cartList));
            startActivity(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
        });
    }

    // ───────────────────────────────
    // DISPLAY USER DATA ON SCREEN
    // ───────────────────────────────
    private void showUserData() {

        if (viewedUser == null) return;

        txtUsername.setText(viewedUser.getUsername());
        txtLastSeen.setText("Last seen: " + viewedUser.getLastSeenString());
        txtUserDescription.setText(viewedUser.getBio());

        txtFullName.setText(viewedUser.getFullName());
        txtEmail.setText(viewedUser.getEmail());
        txtPhone.setText(viewedUser.getPhoneNumber());

        // Use getDefaultAddress() method from User class
        txtAddress.setText(viewedUser.getDefaultAddress());

        loadRatingStars(viewedUser.getOverallRating());

        // Load profile image
        if (imgProfile != null && getActivity() != null && !getActivity().isFinishing()) {
            Glide.with(this)
                    .load(viewedUser.getProfileImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.profile_pic_1)
                    .into(imgProfile);
        }
    }

    // ───────────────────────────────
    // ⭐ UPDATE STAR RATING DISPLAY
    // ───────────────────────────────
    private void loadRatingStars(double rating) {

        txtRating.setText(String.format("%.1f", rating));

        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {

            double starPosition = i + 1;        // 1 → star1, 2 → star2, ...

            if (rating >= starPosition) {
                // FULL STAR
                stars[i].setImageResource(R.drawable.ic_star_filled);

            } else if (rating >= starPosition - 0.5) {
                // HALF STAR (example: 3.4 fills 3 stars + half star)
                stars[i].setImageResource(R.drawable.ic_star_half);

            } else {
                // EMPTY STAR
                stars[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
    }

    private void loadLastSeenTimestamp() {
        if (getContext() == null || viewedUser == null) {
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String jsonLoginHistory = sharedPreferences.getString("login_history", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LoginHistoryItem>>() {}.getType();
        List<LoginHistoryItem> loginHistory = gson.fromJson(jsonLoginHistory, type);

        long lastSeenTimestamp;

        if (loginHistory != null && !loginHistory.isEmpty()) {
            if (loginHistory.size() > 1) {
                // The previous session is the second to last in the list
                lastSeenTimestamp = loginHistory.get(loginHistory.size() - 2).getTimestamp();
            } else {
                // If this is the first session, "last seen" is the start of this session
                lastSeenTimestamp = loginHistory.get(0).getTimestamp();
            }
        } else {
            // Fallback if there's no history, although login should create it.
            // In this case, it will just be "Just now".
            lastSeenTimestamp = System.currentTimeMillis();
        }
        
        viewedUser.setLastSeen(lastSeenTimestamp);
    }


    // ───────────────────────────────
    // HANDLE RESULT FROM EDIT PROFILE
    // ───────────────────────────────
    private void handleEditProfileResult(ActivityResult result) {

        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

            User updatedUser = result.getData().getParcelableExtra("updated_user");

            if (updatedUser != null) {
                viewedUser = updatedUser;
                saveUserToPrefs(updatedUser);
                showUserData();

                Toast.makeText(getContext(),
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserToPrefs(User user) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(USER_KEY, json);
        editor.apply();
    }

    private User loadUserFromPrefs() {
        if (getContext() == null) return null;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(USER_KEY, null);
        return gson.fromJson(json, User.class);
    }

    private void startLastSeenUpdates() {
        lastSeenRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewedUser != null) {
                    txtLastSeen.setText("Last seen: " + viewedUser.getLastSeenString());
                }
                lastSeenHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        lastSeenHandler.post(lastSeenRunnable);
    }

    private void stopLastSeenUpdates() {
        lastSeenHandler.removeCallbacks(lastSeenRunnable);
    }
}
