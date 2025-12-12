package com.example.unitrade;

public class Transaction {
    private String name;
    private String date;
    private double amount;
    private boolean isBuy;
    private String imageUrl; // NEW

    public Transaction(String name, String date, double amount, boolean isBuy, String imageUrl) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isBuy = isBuy;
        this.imageUrl = imageUrl;
    }

    // Constructor for backward compatibility if needed, but I will update usage
    public Transaction(String name, String date, double amount, boolean isBuy) {
        this(name, date, amount, isBuy, null);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
