package com.example.unitrade;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SellImageSliderAdapter
        extends RecyclerView.Adapter<SellImageSliderAdapter.ViewHolder> {

    private final Context context;
    private final List<String> imageList = new ArrayList<>();
    private final Runnable onImageRemoved;

    // ---------------------------------------------------------
    public SellImageSliderAdapter(
            Context context,
            List<String> initialImages,
            Runnable onImageRemoved) {

        this.context = context;
        this.onImageRemoved = onImageRemoved;

        if (initialImages != null) {
            imageList.addAll(initialImages);
        }
    }

    // ---------------------------------------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_sell_image, parent, false);

        return new ViewHolder(view);
    }

    // ---------------------------------------------------------
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Glide.with(context).clear(holder.imgPreview);
        holder.imgPreview.setImageDrawable(null);

        String imgUrl = imageList.get(position);

        Glide.with(context)
                .load(Uri.parse(imgUrl))
                .centerCrop()
                .placeholder(R.drawable.bg_rounded_border)
                .error(R.drawable.bg_rounded_border)
                .into(holder.imgPreview);

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            imageList.remove(pos);
            notifyItemRemoved(pos);

            if (onImageRemoved != null) {
                onImageRemoved.run();
            }
        });
    }


    // ---------------------------------------------------------
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // ---------------------------------------------------------
    public List<String> getImages() {
        return new ArrayList<>(imageList);
    }

    // ---------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPreview;
        ImageButton btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.imgSellPreview);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}
