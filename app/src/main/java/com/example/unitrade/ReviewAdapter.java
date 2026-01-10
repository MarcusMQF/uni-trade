
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> list;
    private OnReportClickListener reportListener;


    public ReviewAdapter(Context context, List<Review> list, OnReportClickListener listener) {
        this.context = context;
        this.list = list;
        this.reportListener = listener;
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

        holder.txtName.setText(r.getReviewer().getFullName());
        holder.txtComment.setText(r.getComment());
        holder.ratingBar.setRating((float) r.getRating());

        Glide.with(context)
                .load(r.getReviewer().getProfileImageUrl())
                .signature(new ObjectKey(r.getReviewer().getProfileImageVersion()))
                .circleCrop()
                .placeholder(R.drawable.circle_profile)
                .into(holder.imgProfile);

        long ts = r.getTimestamp();

        if (ts > 0) {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault());
            holder.txtDate.setText(sdf.format(new Date(ts)));
        } else {
            holder.txtDate.setText("—"); // or "Unknown date"
        }

        holder.txtReport.setOnClickListener(v -> {
            if (reportListener != null) {
                reportListener.onReportClick(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= ViewHolder =================
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtDate, txtComment, txtReport;
        RatingBar ratingBar;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgReviewer);
            txtName = itemView.findViewById(R.id.txtReviewerName);
            txtDate = itemView.findViewById(R.id.txtReviewDate);
            txtComment = itemView.findViewById(R.id.txtReviewComment);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);

            txtReport = itemView.findViewById(R.id.btnReport);
        }
    }

    // ================= Interface =================
    public interface OnReportClickListener {
        void onReportClick(Review review);
    }
}
