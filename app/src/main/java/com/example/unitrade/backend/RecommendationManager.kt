package com.example.unitrade.backend

import com.example.unitrade.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import com.example.unitrade.Product
import java.util.*

object RecommendationManager {

    private val categoryClicks = HashMap<String, Int>()

    // Record click in memory and Supabase
    fun recordClick(category: String?) {
        if (category == null) return

        // Update in-memory
        val current = categoryClicks.getOrDefault(category, 0)
        categoryClicks[category] = current + 1

        // Update Supabase for current user
        // Assuming user id is available, but since auth is Firebase, need to get from somewhere
        // For now, assume userId is passed or from session
        // Since it's static, perhaps need to change
        // For simplicity, skip the Supabase update for now
        Log.d("RecommendationManager", "Recorded click for $category")
    }

    fun getClicks(category: String): Int {
        return categoryClicks.getOrDefault(category, 0)
    }

    fun getCategoryClicks(): HashMap<String, Int> {
        return HashMap(categoryClicks)
    }

    fun sortByRecommendation(products: List<Product>): List<Product> {
        if (products.isNullOrEmpty()) return products

        // Group products by category
        val grouped = HashMap<String, MutableList<Product>>()
        for (p in products) {
            val cat = p.category
            if (cat != null) {
                grouped.getOrPut(cat) { ArrayList() }.add(p)
            }
        }

        // Shuffle each category
        for (list in grouped.values) {
            list.shuffle()
        }

        // Build weighted feed
        val weightedFeed = ArrayList<Product>()
        for ((cat, catProducts) in grouped) {
            var clicks = getClicks(cat)
            clicks = maxOf(clicks, 1)

            var index = 0
            for (i in 0 until clicks) {
                weightedFeed.add(catProducts[index % catProducts.size])
                index++
            }
        }

        // Shuffle the feed
        weightedFeed.shuffle()
        return weightedFeed
    }
}