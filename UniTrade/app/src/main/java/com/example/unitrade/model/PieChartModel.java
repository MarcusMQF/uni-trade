package com.example.unitrade.model;

/**
 * Model class representing data for a pie chart in transaction statistics.
 */
public class PieChartModel {
    private String category;
    private float amount;
    private int color;
    private float percentage;

    public PieChartModel(String category, float amount, int color) {
        this.category = category;
        this.amount = amount;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float totalAmount) {
        if (totalAmount > 0) {
            this.percentage = (amount / totalAmount) * 100;
        } else {
            this.percentage = 0;
        }
    }

    public String getFormattedAmount() {
        return String.format("$%.2f", amount);
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", percentage);
    }
}
