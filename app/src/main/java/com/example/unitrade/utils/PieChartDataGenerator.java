package com.example.unitrade.utils;

import com.example.unitrade.model.PieChartModel;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

/**
 * Utility class for generating sample pie chart data for transaction statistics.
 */
public class PieChartDataGenerator {
    
    // Predefined colors for different categories
    private static final int COLOR_FOOD = Color.parseColor("#FF6B6B");
    private static final int COLOR_TRANSPORT = Color.parseColor("#4ECDC4");
    private static final int COLOR_SHOPPING = Color.parseColor("#45B7D1");
    private static final int COLOR_ENTERTAINMENT = Color.parseColor("#96CEB4");
    private static final int COLOR_BILLS = Color.parseColor("#FFEEAD");

    /**
     * Generate sample pie chart data for demonstration purposes.
     * In a real app, this would come from your data source.
     * 
     * @return List of PieChartModel objects with sample data
     */
    public static List<PieChartModel> getSamplePieData() {
        List<PieChartModel> pieData = new ArrayList<>();
        
        // Add sample categories with amounts and colors
        pieData.add(new PieChartModel("Food & Dining", 350.50f, COLOR_FOOD));
        pieData.add(new PieChartModel("Transport", 120.00f, COLOR_TRANSPORT));
        pieData.add(new PieChartModel("Shopping", 275.75f, COLOR_SHOPPING));
        pieData.add(new PieChartModel("Entertainment", 85.25f, COLOR_ENTERTAINMENT));
        pieData.add(new PieChartModel("Bills & Utilities", 420.00f, COLOR_BILLS));
        
        // Calculate total amount
        float totalAmount = 0;
        for (PieChartModel item : pieData) {
            totalAmount += item.getAmount();
        }
        
        // Calculate percentages for each category
        for (PieChartModel item : pieData) {
            item.setPercentage(totalAmount);
        }
        
        return pieData;
    }
}
