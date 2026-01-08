package com.example.unitrade.backend;

import com.example.unitrade.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RecommendationManager {

    private static final HashMap<String, Integer> categoryClicks = new HashMap<>();

    // Record click in memory and Firestore
    public static void recordClick(String category) {
        if (category == null) return;

        // Update in-memory
        int current = categoryClicks.getOrDefault(category, 0);
        categoryClicks.put(category, current + 1);

        // Update Firestore for current user
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(userId)
                    .update("categoryClicks." + category, FieldValue.increment(1))
                    .addOnFailureListener(e -> Log.e("RecommendationManager", "Failed to update category click", e));
        }
    }

    public static int getClicks(String category) {
        return categoryClicks.getOrDefault(category, 0);
    }

    public static HashMap<String, Integer> getCategoryClicks() {
        return new HashMap<>(categoryClicks);
    }


    public static List<Product> sortByRecommendation(List<Product> products) {
        if (products == null || products.isEmpty()) return products;

        // Group products by category
        HashMap<String, List<Product>> grouped = new HashMap<>();
        for (Product p : products) {
            String cat = p.getCategory();
            grouped.putIfAbsent(cat, new ArrayList<>());
            grouped.get(cat).add(p);
        }

        // Shuffle each category to avoid same-order repeats
        for (List<Product> list : grouped.values()) {
            Collections.shuffle(list);
        }

        // Build a weighted feed based on click counts
        List<Product> weightedFeed = new ArrayList<>();
        for (String cat : grouped.keySet()) {
            int clicks = getClicks(cat); // how many times the user clicked
            clicks = Math.max(clicks, 1); // minimum 1 so the category shows at least once

            List<Product> catProducts = grouped.get(cat);
            int index = 0;
            for (int i = 0; i < clicks; i++) {
                weightedFeed.add(catProducts.get(index % catProducts.size()));
                index++;
            }
        }

        // Shuffle the feed a little to mix categories
        Collections.shuffle(weightedFeed);
        return weightedFeed;
    }
}

