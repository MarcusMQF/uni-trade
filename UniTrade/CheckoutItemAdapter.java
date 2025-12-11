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

import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private List<Product> list;
    private Context ctx;

    public CheckoutItemAdapter(List<Product> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_checkout_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Product p = list.get(pos);

        Glide.with(ctx)
                .load(p.getImageUrls().get(0))
                .into(h.imgItem);

        h.txtItemName.setText(p.getName());
        h.txtItemPrice.setText("RM " + p.getPrice());
        h.txtDescription.setText(p.getCondition());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgItem;
        TextView txtItemName, txtItemPrice, txtDescription;

        ViewHolder(@NonNull View v) {
            super(v);
            imgItem = v.findViewById(R.id.imgItem);
            txtItemName = v.findViewById(R.id.txtItemName);
            txtItemPrice = v.findViewById(R.id.txtItemPrice);
            txtDescription = v.findViewById(R.id.txtDescription);
        }
    }
}
