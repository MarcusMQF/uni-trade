

package com.example.unitrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> list;

    public ReviewAdapter(Context context, List<Review> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review r = list.get(position);

        holder.txtName.setText(r.getReviewer().getUsername());
        holder.txtDate.setText(r.getDate());
        holder.txtComment.setText(r.getComment());
        holder.ratingBar.setRating((float) r.getRating());



        // profile image

        Glide.with(context)
                .load(r.getReviewer().getProfileImageUrl())
                .signature(new ObjectKey(r.getReviewer().getProfileImageVersion()))
                .circleCrop()
                .placeholder(R.drawable.circle_profile)
                .into(holder.imgProfile);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtDate, txtComment;
        RatingBar ratingBar;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgReviewer);
            txtName = itemView.findViewById(R.id.txtReviewerName);
            txtDate = itemView.findViewById(R.id.txtReviewDate);
            txtComment = itemView.findViewById(R.id.txtReviewComment);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
        }
    }
}
