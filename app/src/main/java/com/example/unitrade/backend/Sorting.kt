package com.example.unitrade.backend

import com.example.unitrade.Product

object Sorting {

    fun sortByPrice(products: MutableList<Product>, ascending: Boolean) {
        products.sortWith { p1, p2 ->
            if (ascending) {
                p1.price.compareTo(p2.price)
            } else {
                p2.price.compareTo(p1.price)
            }
        }
    }

    fun sortByLatest(products: MutableList<Product>) {
        products.sortWith { p1, p2 ->
            p2.listingDate.compareTo(p1.listingDate)
        }
    }

    fun sortByOldest(products: MutableList<Product>) {
        products.sortWith { p1, p2 ->
            p1.listingDate.compareTo(p2.listingDate)
        }
    }
}