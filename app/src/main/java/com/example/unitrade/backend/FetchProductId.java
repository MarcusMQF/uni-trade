package com.example.unitrade.backend;

import androidx.annotation.NonNull;
import com.example.unitrade.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FetchProductId {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔍 Search products and return FULL product data
    public static void searchProductsByKeyword(
            String keyword,
            @NonNull OnResultListener listener
    ) {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());

                        if (product.getName() != null &&
                                product.getName().toLowerCase()
                                        .contains(keyword.toLowerCase())) {
                            products.add(product);
                        }
                    }

                    listener.onSuccess(products);
                })
                .addOnFailureListener(listener::onFailure);
    }

    // 🔁 Callback
    public interface OnResultListener {
        void onSuccess(List<Product> products);
        void onFailure(Exception e);
    }
}
