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

    public static ArrayList<Product> cartList = new ArrayList<>();
    private static final String PREF = "cart_pref";
    private static final String KEY = "cart_data";

    // ---------------------------------------------------------
    // LOAD CART
    // ---------------------------------------------------------
    public static void loadCart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "");

        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<Product>>() {}.getType();
            cartList = new Gson().fromJson(json, type);
        }
    }

    // ---------------------------------------------------------
    // SAVE CART
    // ---------------------------------------------------------
    public static void saveCart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY, new Gson().toJson(cartList));
        editor.apply();
    }

    // ---------------------------------------------------------
    // ADD ITEM
    // ---------------------------------------------------------
    public static void addItem(Context context, Product p) {
        cartList.add(p);
        saveCart(context);
    }

    // ---------------------------------------------------------
    // REMOVE ONE ITEM
    // ---------------------------------------------------------
    public static void removeItem(Context context, Product p) {
        cartList.remove(p);
        saveCart(context);
    }

    // ---------------------------------------------------------
    // REMOVE MULTIPLE PURCHASED ITEMS (used in CheckoutActivity)
    // ---------------------------------------------------------
    public static void removePurchased(List<Product> purchasedItems) {
        if (purchasedItems == null || purchasedItems.isEmpty()) return;

        Iterator<Product> it = cartList.iterator();

        while (it.hasNext()) {
            Product cartItem = it.next();

            for (Product purchased : purchasedItems) {
                if (cartItem.getId().equals(purchased.getId())) {
                    it.remove();    // safe removal during iteration
                    break;
                }
            }
        }
    }
}
