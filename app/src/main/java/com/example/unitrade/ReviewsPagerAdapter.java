package com.example.unitrade;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

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

    private OnReportClickListener reportListener;

    public ReviewsPagerAdapter(
            @NonNull FragmentActivity activity,
            User user,
            List<Review> allReviews,
            OnReportClickListener listener
    ) {
        super(activity);
        this.user = user;
        this.allReviews = allReviews;
        this.reportListener = listener;
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
        fragment.setReportListener(reportListener);
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


    @Override
    public long getItemId(int position) {
        // Unique ID based on position + current review count
        return position + (allReviews == null ? 0 : allReviews.size() * 10L);
    }

    @Override
    public boolean containsItem(long itemId) {
        // Always return false so ViewPager2 recreates fragments
        return false;
    }

    public interface OnReportClickListener {
        void onReportClick(Review review);
    }
}
