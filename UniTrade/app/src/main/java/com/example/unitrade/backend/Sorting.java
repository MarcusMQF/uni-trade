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
                Long.compare(p2.getCreatedAt(), p1.getCreatedAt())
        );
    }

    public static void sortByRecommendation(List<Product> products) {
        Collections.sort(products, (p1, p2) ->
                Integer.compare(
                        RecommendationManager.getClicks(p2.getCategory()),
                        RecommendationManager.getClicks(p1.getCategory())
                )
        );
    }

}
