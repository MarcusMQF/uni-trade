package com.example.unitrade.backend

import com.example.unitrade.Product
import com.example.unitrade.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FetchProductId {

    interface OnResultListener {
        fun onSuccess(products: List<Product>)
        fun onFailure(e: Exception)
    }

    fun searchProductsByKeyword(keyword: String, listener: OnResultListener) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()
                    .filter { product ->
                        product.name?.lowercase()?.contains(keyword.lowercase()) == true
                    }
                withContext(Dispatchers.Main) {
                    listener.onSuccess(products)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listener.onFailure(e)
                }
            }
        }
    }
}