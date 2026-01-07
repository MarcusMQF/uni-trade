package com.example.unitrade.backend;

import com.example.unitrade.Product;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorting {

    // Sort by price
    public static void sortByPrice(List<Product> products, boolean ascending) {
        Collections.sort(products, (p1, p2) -> {
            if (ascending) {
                return Double.compare(p1.getPrice(), p2.getPrice());
            } else {
                return Double.compare(p2.getPrice(), p1.getPrice());
            }
        });
    }

    public static void sortByLatest(List<Product> products) {
        Collections.sort(products, (p1, p2) ->
                p2.getListingDate().compareTo(p1.getListingDate())
        );
    }

    public static void sortByOldest(List<Product> products) {
        Collections.sort(products, (p1, p2) ->
                p1.getListingDate().compareTo(p2.getListingDate())
        );
    }

}