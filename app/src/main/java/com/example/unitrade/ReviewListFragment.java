package com.example.unitrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReviewListFragment extends Fragment {
    private ReviewsPagerAdapter.OnReportClickListener reportListener;



    private RecyclerView rvFilteredReviews;
    private ReviewAdapter adapter;

    private User user;
    private List<Review> allReviews;
    private String filterType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
            @Nullable Bundle savedInstanceState) {

        rvFilteredReviews = view.findViewById(R.id.rvFilteredReviews);
        rvFilteredReviews.setLayoutManager(new LinearLayoutManager(requireContext()));

        user = getArguments().getParcelable("user");
        allReviews = getArguments().getParcelableArrayList("reviews");
        filterType = getArguments().getString("filter");

        List<Review> filtered = new ArrayList<>();

        for (Review r : allReviews) {
            String type = r.getType();
            if (type == null)
                type = "unknown"; // prevention

            switch (filterType) {
                case "user":
                    if (type.equals("user"))
                        filtered.add(r);
                    break;
                case "seller":
                    if (type.equals("seller"))
                        filtered.add(r);
                    break;
                default:
                    filtered.add(r);
            }
        }

        adapter = new ReviewAdapter(
                requireContext(),
                filtered,
                review -> {
                    // Pass event to parent activity
                    if (getActivity() instanceof RatingReviewsActivity) {
                        ((RatingReviewsActivity) getActivity()).onReportClick(review);
                    }
                }
        );

        rvFilteredReviews.setAdapter(adapter);
    }

    public void setReportListener(ReviewsPagerAdapter.OnReportClickListener listener) {
        this.reportListener = listener;
    }
}
