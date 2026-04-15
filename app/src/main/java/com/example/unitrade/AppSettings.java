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
        //Access SharedPreferences (private to this app)
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    public static void setCurrency(Context context, String currencyCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();//create editor to modify data 
        editor.putString(KEY_CURRENCY, currencyCode);
        editor.apply();//apply change (save asynchronously)
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
            return String.format(Locale.getDefault(), "%s%.0f", currencyCode, convertedPrice);//no decimal places
        }
        return String.format(Locale.getDefault(), "%s%.2f", currencyCode, convertedPrice);//2 decimal places
    }
}//It returns the user’s current region and language settings on their device.
//so must formate the price follow the user region 
/*
Locale.getDefault() returns the device’s current locale, which includes
language and region settings. It is used to format data like numbers,
 currency, and dates according to the user’s regional preferences.*/
