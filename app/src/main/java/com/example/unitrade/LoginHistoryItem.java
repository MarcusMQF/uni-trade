package com.example.unitrade;

public class LoginHistoryItem {
    private long timestamp;

    public LoginHistoryItem(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
