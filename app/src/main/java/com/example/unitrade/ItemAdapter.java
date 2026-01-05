package com.example.unitrade;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.unitrade.backend.RecommendationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private Map<String, User> sellerCache = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);
        Context context = holder.itemView.getContext();

        // Clear previous content
        holder.txtUsername.setText("");
        holder.imgSeller.setImageDrawable(null);

        holder.txtProductName.setText(product.getName());
        holder.txtProductPrice.setText(AppSettings.formatPrice(context, product.getPrice()));
        holder.txtLocation.setText(product.getLocation());

        // Load product image
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .into(holder.imgProduct);
        }

        // ✅ Seller logic
        String sellerId = product.getSellerId();
        holder.txtUsername.setTag(sellerId); // tag to ensure correct async update

        if (sellerCache.containsKey(sellerId)) {
            User seller = sellerCache.get(sellerId);
            holder.txtUsername.setText(seller.getFullName());
            Glide.with(context)
                    .load(seller.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.imgSeller);
        } else {
            db.collection("users").document(sellerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User seller = doc.toObject(User.class);
                            if (seller != null) {
                                seller.setId(doc.getId());
                                sellerCache.put(sellerId, seller);

                                // Only update if this holder is still showing the same seller
                                if (sellerId.equals(holder.txtUsername.getTag())) {
                                    holder.txtUsername.setText(seller.getFullName());
                                    Glide.with(context)
                                            .load(seller.getProfileImageUrl())
                                            .circleCrop()
                                            .into(holder.imgSeller);
                                }
                            }
                        } else {
                            Log.w("ItemAdapter", "Seller document not found for ID: " + sellerId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ItemAdapter", "Failed to load seller", e));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
            RecommendationManager.recordClick(product.getCategory());
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