package com.example.unitrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Product> list;
    private OnProductClickListener listener;

    // ----- CLICK LISTENER INTERFACE -----
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    // ----- CONSTRUCTOR -----
    public ItemAdapter(List<Product> list, OnProductClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommended, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);
        Context context = holder.itemView.getContext();

        // Product image
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .signature(new ObjectKey(product.getImageVersion())) // 🔥
                    .into(holder.imgProduct);
        }

        holder.txtProductName.setText(product.getName());
        holder.txtProductPrice.setText(AppSettings.formatPrice(context, product.getPrice()));
        holder.txtLocation.setText(product.getLocation());

        // ----- FIXED: Always get seller by ID -----
        User seller = SampleData.getUserById(
                context,
                product.getSellerId()        // <<< Using sellerId now
        );

        if (seller != null) {
            holder.txtUsername.setText(seller.getUsername());
            Glide.with(context)
                    .load(seller.getProfileImageUrl())
                    .signature(new ObjectKey(seller.getProfileImageVersion()))
                    .into(holder.imgSeller);
        }

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, imgSeller;
        TextView txtUsername, txtProductPrice, txtProductName, txtLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgSeller = itemView.findViewById(R.id.imgSeller);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtUsername = itemView.findViewById(R.id.txtUsername);
        }
    }
}
