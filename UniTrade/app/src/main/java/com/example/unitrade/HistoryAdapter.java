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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_PURCHASED = 0;
    private static final int VIEW_SOLD = 1;

    private final Context context;
    private final List<Product> list;
    private boolean isEditMode = false;

    public interface OnHistoryActionListener {
        void onDelete(Product p);
        void onEdit(Product p);
    }

    private OnHistoryActionListener listener;

    public void setOnHistoryActionListener(OnHistoryActionListener l) {
        listener = l;
    }

    public HistoryAdapter(Context ctx, List<Product> list, boolean isPurchasedMode) {
        this.context = ctx;
        this.list = list;
    }

    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Product p = list.get(position);
        if ("Sold".equalsIgnoreCase(p.getStatus())) {
            return VIEW_SOLD;
        } else {
            return VIEW_PURCHASED;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_PURCHASED) {
            View view = inflater.inflate(R.layout.item_purchased_row, parent, false);
            return new PurchasedHolder(view);

        } else {
            View view = inflater.inflate(R.layout.item_sold_row, parent, false);
            return new SoldHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        Product p = list.get(position);

        // Bind UI
        if (holder instanceof PurchasedHolder) {
            bindPurchased((PurchasedHolder) holder, p);
        } else {
            bindSold((SoldHolder) holder, p);
        }

        // Handle click only when NOT in edit mode
        if (!isEditMode) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", p.getId());
                context.startActivity(intent);
            });
        } else {
            // In edit mode → disable normal click
            holder.itemView.setOnClickListener(null);
        }
    }

    // ============================================================
    //                      PURCHASED BINDING
    // ============================================================

    private void bindPurchased(PurchasedHolder h, Product p) {

        // Load item image
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .signature(new com.bumptech.glide.signature.ObjectKey(
                            p.getId() + "_" + p.getImageUrls().hashCode()
                    ))
                    .placeholder(R.drawable.bg_rounded_border)
                    .error(R.drawable.bg_rounded_border)
                    .into(h.imgItem);
        }

        // FIX: Retrieve seller by sellerId
        User seller = SampleData.getUserById(context, p.getSellerId());

        if (seller != null) {
            Glide.with(context)
                    .load(seller.getProfileImageUrl())
                    .signature(new com.bumptech.glide.signature.ObjectKey(
                            seller.getId() + "_" + seller.getProfileImageUrl().hashCode()
                    ))
                    .circleCrop()
                    .into(h.imgSeller);

        } else {
            h.txtSeller.setText("Unknown");
        }

        h.txtName.setText(p.getName());
        h.txtPrice.setText(String.format("-RM%.2f", p.getPrice()));
        h.txtPrice.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));

        long transactionDate = p.getTransactionDate();
        if (transactionDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            h.txtDate.setText(sdf.format(new Date(transactionDate)));
        } else {
            h.txtDate.setText(""); // Set empty if date is invalid
        }

        // Delete button (only purchased tab)
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    static class PurchasedHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, imgSeller;
        Button btnDelete;
        TextView txtName, txtPrice, txtSeller, txtDate;

        public PurchasedHolder(@NonNull View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            imgSeller = v.findViewById(R.id.imgSeller);
            txtName = v.findViewById(R.id.txtName);
            txtSeller = v.findViewById(R.id.txtSeller);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtDate = v.findViewById(R.id.txtDate);
            btnDelete = v.findViewById(R.id.btnConfirm);
        }
    }

    // ============================================================
    //                      SOLD BINDING
    // ============================================================

    private void bindSold(SoldHolder h, Product p) {

        // Item image
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .signature(new ObjectKey(p.getImageVersion())) // 🔥
                    .into(h.imgItem);

        }

        h.txtName.setText(p.getName());
        h.txtPrice.setText(String.format("+RM%.2f", p.getPrice()));
        h.txtPrice.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));

        long transactionDate = p.getTransactionDate();
        if (transactionDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            h.txtDate.setText(sdf.format(new Date(transactionDate)));
        } else {
            h.txtDate.setText(""); // Set empty if date is invalid
        }

        // Status badge
        if (p.getStatus() != null) {
            h.txtStatus.setText(p.getStatus());
            switch (p.getStatus()) {
                case "Available":
                    h.txtStatus.setBackgroundResource(R.drawable.bg_status_available);
                    break;
                case "Sold":
                    h.txtStatus.setBackgroundResource(R.drawable.bg_status_sold);
                    break;
                case "Donated":
                    h.txtStatus.setBackgroundResource(R.drawable.bg_status_donated);
                    break;
                default:
                    h.txtStatus.setBackgroundResource(R.drawable.bg_status_available);
                    break;
            }
        } else {
            h.txtStatus.setText("");
            h.txtStatus.setBackgroundResource(R.drawable.bg_status_available);
        }


        // Edit + Delete buttons
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        h.btnEdit.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });
    }

    static class SoldHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;

        Button btnDelete, btnEdit;
        TextView txtName, txtPrice, txtStatus, txtDate;

        public SoldHolder(@NonNull View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtDate = v.findViewById(R.id.txtDate);
            btnDelete = v.findViewById(R.id.btnConfirm);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }

    public void updateList(List<Product> newList) {
        this.list.clear();
        this.list.addAll(newList);
        notifyDataSetChanged();
    }

}
