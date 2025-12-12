package com.example.unitrade;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryManager {

    // Store purchased items in memory for now
    public static final List<Product> purchasedItems = new ArrayList<>();


    // ----------------------------------------------------------
    // ADD PURCHASED ITEM
    // ----------------------------------------------------------
    public static void addPurchasedItem(Product product) {
        if (product == null) return;

        // Avoid duplicate entries (same product ID)
        if (!purchasedItems.contains(product)) {
            purchasedItems.add(product);
        }
    }


    // ----------------------------------------------------------
    // GET PURCHASED ITEMS FOR CURRENT USER
    // (HistoryActivity uses this)
    // ----------------------------------------------------------
    public static List<Product> getPurchasedItems(String currentUserId) {
        List<Product> result = new ArrayList<>();

        for (Product p : purchasedItems) {
            if (currentUserId.equals(p.getBuyerId())) {
                result.add(p);
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    // CLEAR ALL (Useful for reset or logout)
    // ----------------------------------------------------------
    public static void clear() {
        purchasedItems.clear();
    }
}
