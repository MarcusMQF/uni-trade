package com.example.unitrade

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ProductRepository {

    interface ProductListCallback {
        fun onSuccess(products: List<Product>)
        fun onFailure(e: Exception)
    }

    fun getActiveProductsByUser(userId: String, callback: ProductListCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select {
                        eq("seller_id", userId)
                        eq("status", "active")
                    }
                    .decodeList<Product>()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(products)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}