package com.example.unitrade;

public class Transaction {
    private String name;
    private String date;
    private double amount;
    private boolean isBuy;

    public Transaction(String name, String date, double amount, boolean isBuy) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isBuy = isBuy;
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
}
