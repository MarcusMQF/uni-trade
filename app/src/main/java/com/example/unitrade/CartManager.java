package com.example.unitrade;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartManager {

    private static final String PREF = "cart_pref";
    private static final String KEY = "cart_product_ids";

    public static ArrayList<String> cartProductIds = new ArrayList<>();

    // ---------------- LOAD ----------------
    public static void loadCart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "");

        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            cartProductIds = new Gson().fromJson(json, type);
        }
    }

    // ---------------- SAVE ----------------
    private static void save(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, new Gson().toJson(cartProductIds)).apply();
    }

    // ---------------- ADD ----------------
    public static void addItem(Context context, String productId) {
        if (!cartProductIds.contains(productId)) {
            cartProductIds.add(productId);
            save(context);
        }
    }

    // ---------------- REMOVE ONE ----------------
    public static void removeItem(Context context, String productId) {
        cartProductIds.remove(productId);
        save(context);
    }

    // ---------------- REMOVE PURCHASED ----------------
    public static void removePurchasedByIds(Context context, List<String> purchasedIds) {
        if (purchasedIds == null) return;

        cartProductIds.removeAll(purchasedIds);
        save(context);
    }

    // ---------------- RESOLVE PRODUCTS ----------------
    public static List<Product> getCartProducts(Context context) {
        List<Product> products = new ArrayList<>();
        for (String id : cartProductIds) {
            Product p = SampleData.getProductById(context, id);
            if (p != null) products.add(p);
        }
        return products;
    }
}
