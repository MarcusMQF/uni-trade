package com.example.unitrade;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class AppSettings {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_CURRENCY = "Currency";
    private static final String DEFAULT_CURRENCY = "RM";

    public static String getCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    public static void setCurrency(Context context, String currencyCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CURRENCY, currencyCode);
        editor.apply();
    }

    public static String formatPrice(Context context, double price) {
        String currencyCode = getCurrency(context);
        double conversionRate = 1.0;
        switch (currencyCode) {
            case "$":
                conversionRate = 0.21; // 1 RM = 0.21 USD
                break;
            case "€":
                conversionRate = 0.20; // 1 RM = 0.20 EUR
                break;
            case "£":
                conversionRate = 0.17; // 1 RM = 0.17 GBP
                break;
            case "¥":
                conversionRate = 32.5; // 1 RM = 32.5 JPY
                break;
            case "RM":
            default:
                conversionRate = 1.0;
                break;
        }

        double convertedPrice = price * conversionRate;

        if ("¥".equals(currencyCode)) {
            return String.format(Locale.getDefault(), "%s%.0f", currencyCode, convertedPrice);
        }
        return String.format(Locale.getDefault(), "%s%.2f", currencyCode, convertedPrice);
    }
}
