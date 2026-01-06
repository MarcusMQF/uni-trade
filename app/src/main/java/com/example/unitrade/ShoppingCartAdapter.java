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

    private final Context context;
    private final List<Product> flatList = new ArrayList<>();
    private final Map<String, List<Product>> groupedMap = new LinkedHashMap<>();
    private boolean isEditMode = false;

    public interface OnCartChangedListener {
        void onCartUpdated();
    }

    private OnCartChangedListener cartListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartListener = listener;
    }

    // Selection tracking
    private final java.util.Set<String> selectedIds = new java.util.HashSet<>();

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public double getSelectedTotal() {
        double total = 0;
        for (List<Product> list : groupedMap.values()) {
            for (Product p : list) {
                if (selectedIds.contains(p.getId())) {
                    total += p.getPrice();
                }
            }
        }
        return total;
    }

    public ArrayList<String> getSelectedIds() {
        return new ArrayList<>(selectedIds);
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
            flatList.addAll(groupedMap.get(sellerId));
        }

        notifyDataSetChanged();
    }

    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_shopping_cart, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItem((ItemHolder) holder, flatList.get(position));
    }

    private void bindItem(ItemHolder h, Product p) {
        Glide.with(context)
                .load(p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0))
                .signature(new ObjectKey(p.getImageVersion()))
                .into(h.imgItem);

        h.txtItemName.setText(p.getName());
        h.txtPrice.setText(AppSettings.formatPrice(context, p.getPrice()));
        h.txtStatus.setText(p.getStatus());

        // Checkbox logic
        h.cbSelect.setOnCheckedChangeListener(null);
        h.cbSelect.setChecked(selectedIds.contains(p.getId()));
        h.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(p.getId());
            } else {
                selectedIds.remove(p.getId());
            }
            if (selectionListener != null) {
                selectionListener.onSelectionChanged();
            }
        });

        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> ConfirmDialog.show(
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
                }));

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProductDetailActivity.class);
            i.putExtra("product_id", p.getId());
            context.startActivity(i);
        });
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtItemName, txtPrice, txtStatus;
        Button btnDelete;
        android.widget.CheckBox cbSelect;

        ItemHolder(View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            txtItemName = v.findViewById(R.id.txtItemName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            btnDelete = v.findViewById(R.id.btnConfirm);
            cbSelect = v.findViewById(R.id.cbSelect);
        }
    }
}
