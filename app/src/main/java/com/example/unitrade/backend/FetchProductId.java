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
            @NonNull OnResultListener listener //listener- callback to get the results
    ) {
        db.collection("products") //Goes to Firebase collection called "products"
                .get() //get all documents in the collection
                .addOnSuccessListener(snapshot -> { //When Firebase successfully returns data,
                //  this code runs  snapshot = all the product documents
                    List<Product> products = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) { 
                /* Loops through each product document from Firebase converts the document into a Product object*/
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId()); //Sets the product's ID to the Firebase document ID

                        if (product.getName() != null &&
                                product.getName().toLowerCase()
                                        .contains(keyword.toLowerCase())) {
                            products.add(product);
                        }
                    }
/*Checks if product name is NOT empty
Converts both name and keyword to lowercase
If keyword is found in product name, add it to results list*/

                    listener.onSuccess(products);
                    //Sends the matching products back to the caller via callback
                })
                .addOnFailureListener(listener::onFailure); 
                //If Firebase fails, call the onFailure method with the exception
    }

    // 🔁 Callback
    public interface OnResultListener {
        void onSuccess(List<Product> products);
        //When search succeeds, this method is called with the results
        void onFailure(Exception e);
        //When search fails, this method is called with the error

       
    }
}
