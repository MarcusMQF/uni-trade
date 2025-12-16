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

    private boolean isPurchasedMode;   // 🔥 KEY FIX
    private boolean isEditMode = false;

    // ------------------------------------------------------------
    public interface OnHistoryActionListener {
        void onDelete(Product p);
        void onEdit(Product p);
    }

    private OnHistoryActionListener listener;

    public void setOnHistoryActionListener(OnHistoryActionListener l) {
        listener = l;
    }

    // ------------------------------------------------------------
    public HistoryAdapter(
            Context context,
            List<Product> list,
            boolean isPurchasedMode
    ) {
        this.context = context;
        this.list = list;
        this.isPurchasedMode = isPurchasedMode;
    }

    // ------------------------------------------------------------
    public void setPurchasedMode(boolean mode) {
        this.isPurchasedMode = mode;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }

    // ------------------------------------------------------------
    @Override
    public int getItemViewType(int position) {
        // 🔥 VIEW TYPE IS DECIDED BY TAB, NOT STATUS
        return isPurchasedMode ? VIEW_PURCHASED : VIEW_SOLD;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ------------------------------------------------------------
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_PURCHASED) {
            return new PurchasedHolder(
                    inflater.inflate(R.layout.item_purchased_row, parent, false)
            );
        } else {
            return new SoldHolder(
                    inflater.inflate(R.layout.item_sold_row, parent, false)
            );
        }
    }

    // ------------------------------------------------------------
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        Product p = list.get(position);

        if (holder instanceof PurchasedHolder) {
            bindPurchased((PurchasedHolder) holder, p);
        } else {
            bindSold((SoldHolder) holder, p);
        }

        // Disable click in edit mode
        if (!isEditMode) {
            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, ProductDetailActivity.class);
                i.putExtra("product_id", p.getId());
                context.startActivity(i);
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    // ============================================================
    // PURCHASED (EXPENSE)
    // ============================================================
    private void bindPurchased(PurchasedHolder h, Product p) {

        // Item image
        if (!p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .signature(new ObjectKey(p.getImageVersion()))
                    .into(h.imgItem);
        }

        // Seller
        User seller = SampleData.getUserById(context, p.getSellerId());
        if (seller != null) {
            Glide.with(context)
                    .load(seller.getProfileImageUrl())
                    .signature(new ObjectKey(seller.getProfileImageVersion()))
                    .circleCrop()
                    .into(h.imgSeller);

            h.txtSeller.setText(seller.getUsername());
        }

        h.txtName.setText(p.getName());
        h.txtPrice.setText("-" + AppSettings.formatPrice(context, p.getPrice()));
        h.txtPrice.setTextColor(
                ContextCompat.getColor(context, R.color.red)
        );

        setDate(h.txtDate, p.getTransactionDate());

        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    // ============================================================
    // SOLD (INCOME)
    // ============================================================
    private void bindSold(SoldHolder h, Product p) {

        if (!p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .signature(new ObjectKey(p.getImageVersion()))
                    .into(h.imgItem);
        }

        h.txtName.setText(p.getName());
        h.txtPrice.setText("+" + AppSettings.formatPrice(context, p.getPrice()));
        h.txtPrice.setTextColor(
                ContextCompat.getColor(context, R.color.green)
        );

        setDate(h.txtDate, p.getTransactionDate());

        h.txtStatus.setText(p.getStatus());

        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        h.btnEdit.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });
    }

    // ============================================================
    private void setDate(TextView tv, long time) {
        if (time <= 0) {
            tv.setText("");
            return;
        }
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        tv.setText(sdf.format(new Date(time)));
    }

    // ============================================================
    static class PurchasedHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, imgSeller;
        TextView txtName, txtSeller, txtPrice, txtDate;
        Button btnDelete;

        PurchasedHolder(View v) {
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

    static class SoldHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtName, txtPrice, txtStatus, txtDate;
        Button btnDelete, btnEdit;

        SoldHolder(View v) {
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

    // ------------------------------------------------------------
    public void updateList(List<Product> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}
