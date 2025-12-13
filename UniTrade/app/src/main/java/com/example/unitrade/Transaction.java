package com.example.unitrade;

public class Transaction {

    private final String name;
    private final String date;          // display text (e.g. "Dec 13, 2025")
    private final double amount;
    private final boolean isBuy;
    private final String imageUrl;

    // 🔑 time of transaction (sorting, grouping)
    private final long timestamp;

    // 🔑 image cache signature (from Product)
    private final long imageVersion;

    // =====================
    // SINGLE SOURCE OF TRUTH
    // =====================
    public Transaction(
            String name,
            String date,
            double amount,
            boolean isBuy,
            String imageUrl,
            long timestamp,
            long imageVersion
    ) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isBuy = isBuy;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.imageVersion = imageVersion;
    }

    // =====================
    // GETTERS
    // =====================
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

    public long getTimestamp() {
        return timestamp;
    }

    public long getImageVersion() {
        return imageVersion;
    }
}
