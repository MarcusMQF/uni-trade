package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM   = 1;

    private Context context;
    private List<Object> flatList = new ArrayList<>();
    private boolean isEditMode = false;

    // sellerId → item list
    private Map<String, List<Product>> groupedMap = new LinkedHashMap<>();


    public ShoppingCartAdapter(Context ctx, List<Product> cartList) {
        this.context = ctx;
        rebuild(cartList);
    }


    // --------------------------------------
    // REBUILD GROUPED + FLAT STRUCTURE
    // --------------------------------------
    private void rebuild(List<Product> cartList) {
        flatList.clear();
        groupedMap.clear();

        for (Product p : cartList) {
            String sellerId = p.getSellerId();

            groupedMap.putIfAbsent(sellerId, new ArrayList<>());
            groupedMap.get(sellerId).add(p);
        }

        for (String sellerId : groupedMap.keySet()) {
            flatList.add("HEADER_" + sellerId);
            flatList.addAll(groupedMap.get(sellerId));
        }

        notifyDataSetChanged();
    }


    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return (flatList.get(position) instanceof String)
                ? VIEW_TYPE_HEADER
                : VIEW_TYPE_ITEM;
    }


    @Override
    public int getItemCount() {
        return flatList.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.header_cart_seller, parent, false);
            return new HeaderHolder(view);

        } else {
            View view = inflater.inflate(R.layout.item_shopping_cart, parent, false);
            return new ItemHolder(view);
        }
    }


    // --------------------------------------
    // SINGLE CORRECT onBindViewHolder
    // --------------------------------------
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            String headerKey = (String) flatList.get(position);
            bindHeader((HeaderHolder) holder, headerKey);

        } else {
            Product p = (Product) flatList.get(position);
            bindItem((ItemHolder) holder, p);
        }
    }


    // --------------------------------------
    // HEADER BINDING
    // --------------------------------------
    private void bindHeader(HeaderHolder h, String headerKey) {

        String sellerId = headerKey.replace("HEADER_", "");
        List<Product> sellerItems = groupedMap.get(sellerId);

        User seller = SampleData.getUserById(context, sellerId);

        if (seller != null) {

            h.txtSellerName.setText(seller.getUsername());

            Glide.with(context)
                    .load(seller.getProfileImageUrl())
                    .circleCrop()
                    .into(h.imgSeller);
        }

        h.btnCheckout.setOnClickListener(v -> {

            Intent intent = new Intent(context, CheckoutActivity.class);
            intent.putParcelableArrayListExtra(
                    "checkoutItems",
                    new ArrayList<>(sellerItems)
            );

            context.startActivity(intent);
        });
    }


    // --------------------------------------
    // ITEM BINDING
    // --------------------------------------
    private void bindItem(ItemHolder h, Product p) {

        Glide.with(context)
                .load(p.getImageUrls().get(0))
                .into(h.imgItem);

        h.txtItemName.setText(p.getName());
        h.txtPrice.setText(AppSettings.formatPrice(context, p.getPrice()));
        h.txtStatus.setText(p.getStatus());

        // Edit mode delete button
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {

            ConfirmDialog.show(
                    context,
                    "Are You Sure?",
                    "Do you really want to delete this item?",
                    "Delete",
                    () -> {

                        // Remove from cart storage
                        CartManager.removeItem(context, p);

                        // Remove from group
                        String sellerId = p.getSellerId();
                        groupedMap.get(sellerId).remove(p);

                        if (groupedMap.get(sellerId).isEmpty()) {
                            groupedMap.remove(sellerId);
                        }

                        rebuild(CartManager.cartList);

                        if (cartListener != null) {
                            cartListener.onCartUpdated();
                        }
                    }
            );
        });

        // Item click → open product details
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", p);
            context.startActivity(intent);
        });
    }


    // --------------------------------------
    // LISTENER
    // --------------------------------------
    public interface OnCartChangedListener {
        void onCartUpdated();
    }

    private OnCartChangedListener cartListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartListener = listener;
    }


    // --------------------------------------
    // VIEW HOLDERS
    // --------------------------------------
    static class HeaderHolder extends RecyclerView.ViewHolder {
        ImageView imgSeller;
        TextView txtSellerName;
        Button btnCheckout;

        public HeaderHolder(@NonNull View itemView) {
            super(itemView);
            imgSeller = itemView.findViewById(R.id.imgSeller);
            txtSellerName = itemView.findViewById(R.id.txtSellerName);
            btnCheckout = itemView.findViewById(R.id.btnCheckout);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtItemName, txtPrice, txtStatus;
        Button btnDelete;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnDelete = itemView.findViewById(R.id.btnConfirm);
        }
    }
}
