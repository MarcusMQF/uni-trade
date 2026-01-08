package com.example.unitrade.backend;

import android.util.Log;

import com.example.unitrade.Product;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Filter {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public Filter(){}

    public void filterPrice(String min, String max,OnFilterResult callback){
        double minPrice = Double.parseDouble(min);
        double maxPrice = Double.parseDouble(max);

        db.collection("products")
                .whereGreaterThanOrEqualTo("price",minPrice)
                .whereLessThanOrEqualTo("price",maxPrice)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        products.add(product);
                    }
                    callback.onSuccess(products);
                })
                .addOnFailureListener(e ->{
                    Log.e("Firestore","Error Fetching Items",e);
                });
    }

    public void chooseCondition(String condition, OnFilterResult callback) {
        db.collection("products")
                .whereEqualTo("productUsed", condition)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        products.add(doc.toObject(Product.class));
                    }
                    callback.onSuccess(products);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching products", e));
    }


    public void dateAfter(String dayStr , String monthStr , String yearStr, OnFilterResult callback){
        int day = Integer.parseInt(dayStr);
        int month = Integer.parseInt(monthStr) - 1; // VERY IMPORTANT AS MONTH STARTS FROM 0 IN JAVA
        int year = Integer.parseInt(yearStr);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);

        Timestamp selectedDate = new Timestamp(calendar.getTime());

        db.collection("products")
                .whereGreaterThan("listingDate", selectedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> filteredProducts = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        filteredProducts.add(product);
                    }

                    callback.onSuccess(filteredProducts);
                });

    }

    public interface OnFilterResult {
        void onSuccess(List<Product> products);
    }

}
