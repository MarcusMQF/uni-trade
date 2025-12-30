package com.example.unitrade;

public class Message {
    private String text;
    private String senderId;
    private long timestamp;
    private String mediaUrl;
    private String mediaType; // "image" or "video"

    // Empty constructor for Firestore
    public Message() {
    }

    public Message(String text, String senderId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public Message(String text, String senderId, long timestamp, String mediaUrl, String mediaType) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
