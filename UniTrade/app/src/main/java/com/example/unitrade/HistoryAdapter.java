package com.example.unitrade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_PURCHASED = 0;
    private static final int VIEW_SOLD = 1;

    private Context context;
    private List<Product> list;
    private boolean isPurchasedTab;
    private boolean isEditMode = false;

    public interface OnHistoryActionListener {
        void onDelete(Product p);
        void onEdit(Product p);
    }

    private OnHistoryActionListener listener;

    public void setOnHistoryActionListener(OnHistoryActionListener l) {
        listener = l;
    }

    public HistoryAdapter(Context ctx, List<Product> list, boolean isPurchasedTab) {
        this.context = ctx;
        this.list = list;
        this.isPurchasedTab = isPurchasedTab;
    }

    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isPurchasedTab ? VIEW_PURCHASED : VIEW_SOLD;
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
        h.txtPrice.setText(AppSettings.formatPrice(context, p.getPrice()));

        // Delete button (only purchased tab)
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    static class PurchasedHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, imgSeller;
        Button btnDelete;
        TextView txtName, txtPrice, txtSeller;

        public PurchasedHolder(@NonNull View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            imgSeller = v.findViewById(R.id.imgSeller);
            txtName = v.findViewById(R.id.txtName);
            txtSeller = v.findViewById(R.id.txtSeller);
            txtPrice = v.findViewById(R.id.txtPrice);
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
        h.txtPrice.setText(AppSettings.formatPrice(context, p.getPrice()));


        // Status badge
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
        }

        h.txtStatus.setText(p.getStatus());

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
        TextView txtName, txtPrice, txtStatus;

        public SoldHolder(@NonNull View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            btnDelete = v.findViewById(R.id.btnConfirm);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }

    public void updateList(List<Product> newList) {
        this.list.clear();
        this.list.addAll(newList);
        notifyDataSetChanged();
    }

    public void setPurchasedTab(boolean isPurchasedTab) {
        this.isPurchasedTab = isPurchasedTab;
        notifyDataSetChanged();
    }
}
