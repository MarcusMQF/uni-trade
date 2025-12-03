package com.example.unitrade;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CartManager {

    public static ArrayList<Product> cartList = new ArrayList<>();
    private static final String PREF = "cart_pref";
    private static final String KEY = "cart_data";

    public static void loadCart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "");

        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<Product>>(){}.getType();
            cartList = new Gson().fromJson(json, type);
        }
    }

    public static void saveCart(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY, new Gson().toJson(cartList));
        editor.apply();
    }

    public static void addItem(Context context, Product p) {
        cartList.add(p);
        saveCart(context);
    }

    public static void removeItem(Context context, Product p) {
        cartList.remove(p);
        saveCart(context);
    }
}


