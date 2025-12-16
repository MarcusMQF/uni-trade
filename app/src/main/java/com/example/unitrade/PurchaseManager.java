package com.example.unitrade;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

public class PurchaseManager {
    private static final String TAG = "PurchaseManager";

    public interface PurchaseCallback {
        void onSuccess(String purchaseId);
        void onFailure(String error);
    }

    /**
     * Process a single purchase and automatically mark product as sold
     */
    public static void processPurchase(
            String productId,
            String buyerId,
            String sellerId,
            double price,
            String receivingMethod,
            String deliveryAddress,
            PurchaseCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a batch write for atomic operation
        WriteBatch batch = db.batch();

        // Step 1: Create purchase document
        String purchaseId = db.collection("purchases").document().getId();
        Purchase purchase = new Purchase(productId, buyerId, sellerId, price);
        purchase.setId(purchaseId);
        purchase.setReceivingMethod(receivingMethod);
        purchase.setDeliveryAddress(deliveryAddress);

        batch.set(db.collection("purchases").document(purchaseId), purchase);

        // Step 2: Update product status
        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("status", price > 0 ? "sold" : "donated");
        productUpdates.put("soldAt", Timestamp.now());
        productUpdates.put("soldTo", buyerId);
        productUpdates.put("purchaseId", purchaseId);
        productUpdates.put("transactionDate", System.currentTimeMillis());

        batch.update(db.collection("products").document(productId), productUpdates);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Purchase processed successfully: " + purchaseId);
                    callback.onSuccess(purchaseId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to process purchase", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Process multiple purchases (for cart checkout)
     */
    public static void processMultiplePurchases(
            java.util.List<String> productIds,
            String buyerId,
            String receivingMethod,
            String deliveryAddress,
            PurchaseCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        final int[] completed = {0};
        final int[] failed = {0};
        final StringBuilder errorMessages = new StringBuilder();

        // Process each product
        for (String productId : productIds) {
            // First get the product to know the seller and price
            db.collection("products")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Product product = doc.toObject(Product.class);
                        if (product == null) {
                            failed[0]++;
                            errorMessages.append("Product ").append(productId).append(" not found. ");
                            checkCompletion(completed, failed, productIds.size(), errorMessages, callback);
                            return;
                        }

                        // Create purchase
                        processPurchase(
                                productId,
                                buyerId,
                                product.getSellerId(),
                                product.getPrice(),
                                receivingMethod,
                                deliveryAddress,
                                new PurchaseCallback() {
                                    @Override
                                    public void onSuccess(String purchaseId) {
                                        completed[0]++;
                                        Log.d(TAG, "Product " + productId + " purchased successfully");
                                        checkCompletion(completed, failed, productIds.size(), errorMessages, callback);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        failed[0]++;
                                        errorMessages.append(error).append(" ");
                                        checkCompletion(completed, failed, productIds.size(), errorMessages, callback);
                                    }
                                }
                        );
                    })
                    .addOnFailureListener(e -> {
                        failed[0]++;
                        errorMessages.append("Failed to load product ").append(productId).append(". ");
                        checkCompletion(completed, failed, productIds.size(), errorMessages, callback);
                    });
        }
    }

    private static void checkCompletion(
            int[] completed,
            int[] failed,
            int total,
            StringBuilder errorMessages,
            PurchaseCallback callback
    ) {
        if (completed[0] + failed[0] == total) {
            if (failed[0] == 0) {
                callback.onSuccess("All purchases completed");
            } else if (completed[0] > 0) {
                callback.onFailure("Some purchases failed: " + errorMessages.toString());
            } else {
                callback.onFailure("All purchases failed: " + errorMessages.toString());
            }
        }
    }
}