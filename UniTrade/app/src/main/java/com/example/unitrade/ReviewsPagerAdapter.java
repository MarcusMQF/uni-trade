package com.example.unitrade;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReviewsPagerAdapter extends FragmentStateAdapter {

    private final User user;
    private final List<Review> allReviews;

    public ReviewsPagerAdapter(@NonNull FragmentActivity activity,
                               User user,
                               List<Review> allReviews) {
        super(activity);
        this.user = user;
        this.allReviews = allReviews;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String filter;

        switch (position) {
            case 1: filter = "user"; break;
            case 2: filter = "seller"; break;
            default: filter = "all"; break;
        }

        ReviewListFragment fragment = new ReviewListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        bundle.putParcelableArrayList("reviews", new ArrayList<>(allReviews));
        bundle.putString("filter", filter);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3; // All, User, Seller
    }
}
