package com.example.unitrade.backend;

import java.util.HashMap;

public class RecommendationManager {

    private static final HashMap<String, Integer> categoryClicks = new HashMap<>();

    public static void recordClick(String category) {
        if (category == null) return;

        int current = categoryClicks.getOrDefault(category, 0);
        categoryClicks.put(category, current + 1);
    }

    public static int getClicks(String category) {
        return categoryClicks.getOrDefault(category, 0);
    }

}

