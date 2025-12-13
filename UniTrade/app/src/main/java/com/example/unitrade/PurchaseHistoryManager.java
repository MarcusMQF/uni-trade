package com.example.unitrade;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryManager {

    // ✅ Store ONLY product IDs
    private static final List<String> purchasedProductIds = new ArrayList<>();


    // ----------------------------------------------------------
    // ADD PURCHASED PRODUCT (BY ID)
    // ----------------------------------------------------------
    public static void add(String productId) {
        if (productId == null) return;

        if (!purchasedProductIds.contains(productId)) {
            purchasedProductIds.add(productId);
        }
    }


    // ----------------------------------------------------------
    // GET PURCHASED PRODUCTS FOR USER (RESOLVED)
    // Used by HistoryActivity
    // ----------------------------------------------------------
    public static List<Product> getPurchasedItems(
            Context context,
            String currentUserId
    ) {
        List<Product> result = new ArrayList<>();

        for (String id : purchasedProductIds) {
            Product p = SampleData.getProductById(context, id);

            if (p != null && currentUserId.equals(p.getBuyerId())) {
                result.add(p);
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    // CLEAR ALL (logout / reset)
    // ----------------------------------------------------------
    public static void clear() {
        purchasedProductIds.clear();
    }
}
