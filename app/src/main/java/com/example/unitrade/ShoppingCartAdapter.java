package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final Context context;
    private final List<Object> flatList = new ArrayList<>();
    private final Map<String, List<Product>> groupedMap = new LinkedHashMap<>();
    private boolean isEditMode = false;

    public interface OnCartChangedListener {
        void onCartUpdated();
    }

    private OnCartChangedListener cartListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartListener = listener;
    }

    public ShoppingCartAdapter(Context ctx, List<Product> cartProducts) {
        this.context = ctx;
        rebuild(cartProducts);
    }

    public void rebuild(List<Product> products) {
        flatList.clear();
        groupedMap.clear();

        for (Product p : products) {
            groupedMap
                    .computeIfAbsent(p.getSellerId(), k -> new ArrayList<>())
                    .add(p);
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
        return (flatList.get(position) instanceof String) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inflater.inflate(R.layout.header_cart_seller, parent, false);
            return new HeaderHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_shopping_cart, parent, false);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            bindHeader((HeaderHolder) holder, (String) flatList.get(position));
        } else {
            bindItem((ItemHolder) holder, (Product) flatList.get(position));
        }
    }

    private void bindHeader(HeaderHolder h, String headerKey) {
        String sellerId = headerKey.replace("HEADER_", "");
        List<Product> sellerItems = groupedMap.get(sellerId);

        // Placeholder while loading
        h.txtSellerName.setText("Loading...");
        h.imgSeller.setImageResource(R.drawable.ic_profile_small);

        // Fetch seller asynchronously
        UserRepository.getUserByUid(sellerId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                h.txtSellerName.setText(user.getUsername());
                Glide.with(context)
                        .load(user.getProfileImageUrl())
                        .signature(new ObjectKey(user.getProfileImageVersion()))
                        .circleCrop()
                        .into(h.imgSeller);
            }

            @Override
            public void onFailure(Exception e) {
                h.txtSellerName.setText("Unknown Seller");
            }
        });

        h.btnCheckout.setOnClickListener(v -> {
            ArrayList<String> ids = new ArrayList<>();
            for (Product p : sellerItems) ids.add(p.getId());

            Intent i = new Intent(context, CheckoutActivity.class);
            i.putStringArrayListExtra("checkout_ids", ids);
            context.startActivity(i);
        });
    }

    private void bindItem(ItemHolder h, Product p) {
        Glide.with(context)
                .load(p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0))
                .signature(new ObjectKey(p.getImageVersion()))
                .into(h.imgItem);

        h.txtItemName.setText(p.getName());
        h.txtPrice.setText(AppSettings.formatPrice(context, p.getPrice()));
        h.txtStatus.setText(p.getStatus());
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v ->
                ConfirmDialog.show(
                        context,
                        "Remove item?",
                        "Do you want to remove this item from cart?",
                        "Remove",
                        () -> {
                            // 1️⃣ Remove from cart storage
                            CartManager.removeItem(context, p.getId());

                            // 2️⃣ Reload products from Firestore
                            CartManager.getCartProducts(context, products -> {
                                // 3️⃣ Rebuild THIS adapter
                                rebuild(products);

                                // 4️⃣ Notify fragment/activity
                                if (cartListener != null) {
                                    cartListener.onCartUpdated();
                                }
                            });
                        }
                )
        );


        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProductDetailActivity.class);
            i.putExtra("product_id", p.getId());
            context.startActivity(i);
        });
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        ImageView imgSeller;
        TextView txtSellerName;
        Button btnCheckout;

        HeaderHolder(View v) {
            super(v);
            imgSeller = v.findViewById(R.id.imgSeller);
            txtSellerName = v.findViewById(R.id.txtSellerName);
            btnCheckout = v.findViewById(R.id.btnCheckout);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtItemName, txtPrice, txtStatus;
        Button btnDelete;

        ItemHolder(View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            txtItemName = v.findViewById(R.id.txtItemName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            btnDelete = v.findViewById(R.id.btnConfirm);
        }
    }
}
