package com.example.unitrade

import android.util.Log
import com.example.unitrade.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

object PurchaseManager {
    private const val TAG = "PurchaseManager"

    interface PurchaseCallback {
        fun onSuccess(purchaseId: String)
        fun onFailure(error: String)
    }

    fun processPurchase(
        productId: String,
        buyerId: String,
        sellerId: String,
        price: Double,
        receivingMethod: String,
        deliveryAddress: String,
        callback: PurchaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Insert purchase
                val purchase = Purchase(productId, buyerId, sellerId, price).apply {
                    id = UUID.randomUUID().toString()
                    this.receivingMethod = receivingMethod
                    this.deliveryAddress = deliveryAddress
                }
                SupabaseClient.client.postgrest["purchases"].insert(purchase)

                // Update product
                val status = if (price > 0) "sold" else "donated"
                SupabaseClient.client.postgrest["products"].update({
                    set("status", status)
                    set("sold_at", Date())
                    set("sold_to", buyerId)
                    set("purchase_id", purchase.id)
                    set("transaction_date", System.currentTimeMillis())
                }) {
                    eq("id", productId)
                }

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Purchase processed successfully: ${purchase.id}")
                    callback.onSuccess(purchase.id!!)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to process purchase", e)
                    callback.onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun processMultiplePurchases(
        productIds: List<String>,
        buyerId: String,
        receivingMethod: String,
        deliveryAddress: String,
        callback: PurchaseCallback
    ) {
        var completed = 0
        var failed = 0
        val errorMessages = StringBuilder()

        for (productId in productIds) {
            // Get product first
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val products = SupabaseClient.client.postgrest["products"]
                        .select {
                            eq("id", productId)
                        }
                        .decodeList<Product>()
                    if (products.isNotEmpty()) {
                        val product = products[0]
                        processPurchase(
                            productId,
                            buyerId,
                            product.sellerId!!,
                            product.price,
                            receivingMethod,
                            deliveryAddress,
                            object : PurchaseCallback {
                                override fun onSuccess(purchaseId: String) {
                                    completed++
                                    Log.d(TAG, "Product $productId purchased successfully")
                                    checkCompletion(completed, failed, productIds.size, errorMessages, callback)
                                }

                                override fun onFailure(error: String) {
                                    failed++
                                    errorMessages.append(error).append(" ")
                                    checkCompletion(completed, failed, productIds.size, errorMessages, callback)
                                }
                            }
                        )
                    } else {
                        failed++
                        errorMessages.append("Product $productId not found. ")
                        checkCompletion(completed, failed, productIds.size, errorMessages, callback)
                    }
                } catch (e: Exception) {
                    failed++
                    errorMessages.append("Failed to load product $productId. ")
                    checkCompletion(completed, failed, productIds.size, errorMessages, callback)
                }
            }
        }
    }

    private fun checkCompletion(
        completed: Int,
        failed: Int,
        total: Int,
        errorMessages: StringBuilder,
        callback: PurchaseCallback
    ) {
        if (completed + failed == total) {
            if (failed == 0) {
                callback.onSuccess("All purchases completed")
            } else if (completed > 0) {
                callback.onFailure("Some purchases failed: $errorMessages")
            } else {
                callback.onFailure("All purchases failed: $errorMessages")
            }
        }
    }
}