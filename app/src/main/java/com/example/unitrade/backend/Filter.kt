package com.example.unitrade.backend

import com.example.unitrade.Product
import com.example.unitrade.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

object Filter {

    interface OnFilterResult {
        fun onSuccess(products: List<Product>)
    }

    fun filterPrice(min: String, max: String, callback: OnFilterResult) {
        val minPrice = min.toDouble()
        val maxPrice = max.toDouble()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select {
                        gte("price", minPrice)
                        lte("price", maxPrice)
                    }
                    .decodeList<Product>()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(products)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun chooseCondition(condition: String, callback: OnFilterResult) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select {
                        eq("product_used", condition)
                    }
                    .decodeList<Product>()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(products)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun dateAfter(dayStr: String, monthStr: String, yearStr: String, callback: OnFilterResult) {
        val day = dayStr.toInt()
        val month = monthStr.toInt() - 1
        val year = yearStr.toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        val selectedDate = calendar.time

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = SupabaseClient.client.postgrest["products"]
                    .select {
                        gt("listing_date", selectedDate)
                    }
                    .decodeList<Product>()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(products)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}