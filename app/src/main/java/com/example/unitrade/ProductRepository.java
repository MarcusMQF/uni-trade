package com.example.unitrade;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public interface ProductListCallback {
        void onSuccess(List<Product> products);

        void onFailure(Exception e);
    }

    public static void getActiveProductsByUser(String userId, ProductListCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("products")

                .whereEqualTo("sellerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        products.add(doc.toObject(Product.class));
                    }
                    callback.onSuccess(products);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
