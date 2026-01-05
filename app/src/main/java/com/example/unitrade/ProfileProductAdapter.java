package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class ProfileProductAdapter
        extends RecyclerView.Adapter<ProfileProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;

    // ----------------------------------------
    // CALLBACK INTERFACE
    // ----------------------------------------
    public interface OnProductActionListener {
        void onEdit(Product p);
        void onDelete(Product p);
    }

    private OnProductActionListener actionListener;

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.actionListener = listener;
    }

    // ----------------------------------------
    // MANAGE MODE
    // ----------------------------------------
    private boolean manageMode = false;

    public void setManageMode(boolean mode) {
        manageMode = mode;
        notifyDataSetChanged();
    }

    // ----------------------------------------
    // CONSTRUCTOR
    // ----------------------------------------
    public ProfileProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = products;
    }

    // ----------------------------------------
    // VIEW HOLDER
    // ----------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productStatus;
        LinearLayout manageButtons;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage  = itemView.findViewById(R.id.productImage);
            productName   = itemView.findViewById(R.id.productName);
            productPrice  = itemView.findViewById(R.id.productPrice);
            productStatus = itemView.findViewById(R.id.productStatus);
            manageButtons = itemView.findViewById(R.id.manageButtons);
            btnEdit       = itemView.findViewById(R.id.btnEdit);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }
    }

    // ----------------------------------------
    // CREATE VIEW
    // ----------------------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_profile_product, parent, false);

        return new ViewHolder(view);
    }

    // ----------------------------------------
    // BIND DATA
    // ----------------------------------------
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Product p = productList.get(position);

        holder.productName.setText(p.getName());
        holder.productPrice.setText(
                AppSettings.formatPrice(context, p.getPrice())
        );

        holder.productStatus.setText(p.getStatus());
        applyStatusBackground(holder.productStatus, p.getStatus());

        // 🔥 VERY IMPORTANT — CLEAR IMAGE FIRST
        Glide.with(context).clear(holder.productImage);
        holder.productImage.setImageDrawable(null);

        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .signature(new ObjectKey(p.getImageVersion())) // 🔥
                    .placeholder(R.drawable.bg_rounded_border)
                    .error(R.drawable.bg_rounded_border)
                    .into(holder.productImage);
        }

        // ----------------------------------------
        // CLICK → PRODUCT DETAIL
        // ----------------------------------------
        holder.itemView.setOnClickListener(v -> {
            if (!manageMode) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", p.getId()); // ✅ FIXED
                context.startActivity(intent);
            }
        });

        // ----------------------------------------
        // MANAGE MODE
        // ----------------------------------------
        holder.manageButtons.setVisibility(
                manageMode ? View.VISIBLE : View.GONE
        );

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(p);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ----------------------------------------
    // STATUS BADGE
    // ----------------------------------------
    private void applyStatusBackground(TextView badge, String status) {

        if (status == null) status = "";

        switch (status.toLowerCase()) {
            case "available":
                badge.setBackgroundResource(R.drawable.bg_status_available);
                break;
            case "sold":
            case "donated":
                badge.setBackgroundResource(R.drawable.bg_status_donated);
                break;
            default:
                badge.setBackgroundResource(R.drawable.bg_status_badge);
        }
    }
}
