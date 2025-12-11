package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProfileProductAdapter extends RecyclerView.Adapter<ProfileProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;

    // ------------------------------
    // CALLBACK (Edit/Delete actions)
    // ------------------------------
    public interface OnProductActionListener {
        void onEdit(Product p);
        void onDelete(Product p);   // optional, fragment may ignore
    }

    private OnProductActionListener actionListener;

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.actionListener = listener;
    }

    // ------------------------------
    // CONSTRUCTOR
    // ------------------------------
    public ProfileProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = products;
    }


    // ------------------------------
    // VIEW HOLDER
    // ------------------------------
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage  = itemView.findViewById(R.id.productImage);
            productName   = itemView.findViewById(R.id.productName);
            productPrice  = itemView.findViewById(R.id.productPrice);
            productStatus = itemView.findViewById(R.id.productStatus);
        }
    }


    // ------------------------------
    // INFLATE LAYOUT
    // ------------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_profile_product, parent, false);
        return new ViewHolder(view);
    }


    // ------------------------------
    // BIND VIEWS
    // ------------------------------
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product p = productList.get(position);

        // Name + Price
        holder.productName.setText(p.getName());
        holder.productPrice.setText("RM " + String.format("%.2f", p.getPrice()));

        // Status
        holder.productStatus.setText(p.getStatus());
        applyStatusBackground(holder.productStatus, p.getStatus());

        // Load product image
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .placeholder(R.drawable.bg_rounded_border)
                    .into(holder.productImage);
        }

        // -----------------------------------
        // CLICK → OPEN PRODUCT DETAILS
        // -----------------------------------
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", p);  // must implement Parcelable
            context.startActivity(intent);
        });

        // -----------------------------------
        // LONG PRESS → EDIT (Callback to Fragment)
        // -----------------------------------
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(p);
            }
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }


    // ------------------------------
    // STATUS BADGE UI
    // ------------------------------
    private void applyStatusBackground(TextView badge, String status) {

        if (status == null) status = "";

        switch (status.toLowerCase()) {

            case "available":
                badge.setBackgroundResource(R.drawable.bg_status_available);
                break;

            case "reserved":
            case "bad":
                badge.setBackgroundResource(R.drawable.bg_status_badge);
                break;

            case "sold":
            case "donated":
                badge.setBackgroundResource(R.drawable.bg_status_donated);
                break;

            default:
                badge.setBackgroundResource(R.drawable.bg_status_badge);
                break;
        }
    }
}
