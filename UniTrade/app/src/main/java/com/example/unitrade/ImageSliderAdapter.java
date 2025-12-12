package com.example.unitrade;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter
        extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private final Context context;
    private final List<String> images = new ArrayList<>();

    private long imageVersion;

    // ---------------------------------------------------------
    // CONSTRUCTOR — COPY INTO MUTABLE LIST
    // ---------------------------------------------------------
    public ImageSliderAdapter(Context context, List<String> initialImages, long imageVersion) {
        this.context = context;
        this.imageVersion = imageVersion;
        images.addAll(initialImages);
    }

    // ---------------------------------------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image_slider, parent, false);

        return new ViewHolder(view);
    }

    // ---------------------------------------------------------
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Glide.with(context).clear(holder.img);
        holder.img.setImageDrawable(null);

        String url = images.get(position);


        if (url == null || url.isEmpty()) {
            // ✅ FALLBACK DEFAULT IMAGE
            holder.img.setImageResource(R.drawable.upload_pic);
            return;
        }

        Glide.with(context)
                .load(Uri.parse(url))
                .signature(new ObjectKey(imageVersion)) // ✅ cache busting
                .centerCrop()
                .placeholder(R.drawable.bg_rounded_border) // 🔥 REQUIRED
                .error(R.drawable.bg_rounded_border)       // 🔥 REQUIRED
                .into(holder.img);
    }

    // ---------------------------------------------------------
    @Override
    public int getItemCount() {
        return images.size();
    }

    // ---------------------------------------------------------
    // 🔥 SAFE UPDATE METHOD — NO CRASH
    // ---------------------------------------------------------
    public void updateImages(List<String> newImages) {

        images.clear();  // ✅ always safe (ArrayList)

        if (newImages != null) {
            images.addAll(newImages);
        }

        notifyDataSetChanged();
    }

    // ---------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgSlider);
        }
    }
}
