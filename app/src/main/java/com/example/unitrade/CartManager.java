package com.example.unitrade;

import android.content.Context;
import android.content.SharedPreferences;
// Context provides app environment info; SharedPreferences stores simple data locally

import com.google.firebase.firestore.FirebaseFirestore;
//Cloud database service for storing/retrieving data online
import com.google.gson.Gson;
//Converts Java objects to JSON and vice versa
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
// Used for generic type information
import java.util.ArrayList;
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

    // ---------------- RESOLVE PRODUCTS (Firestore) ----------------
    public interface OnProductsLoadedListener { //callback interface
        void onLoaded(List<Product> products); //call when products are loaded from firebase
    }

    public static void getCartProducts(Context context, OnProductsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Product> products = new ArrayList<>();
        int total = cartProductIds.size();

        if (total == 0) {
            listener.onLoaded(products);
            return;
        }

        for (String id : cartProductIds) {
            db.collection("products")
                    .document(id)
                    .get() //retrieve the document
                    .addOnSuccessListener(doc -> { //successfully retrieve the document
                        Product p = doc.toObject(Product.class);
                        if (p != null) products.add(p);

                        if (products.size() == total) {
                            listener.onLoaded(products);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (products.size() == total) {
                            listener.onLoaded(products);
                        }
                    });
        }
    }
}

