package com.example.unitrade;

import android.net.Uri;

public class Message {
    private String text;
    private boolean isSent;
    private Uri imageUri;

    // Constructor for text-only messages
    public Message(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
    }

    // Constructor for messages with both image and text
    public Message(Uri imageUri, String text, boolean isSent) {
        this.imageUri = imageUri;
        this.text = text;
        this.isSent = isSent;
    }

    public String getText() {
        return text;
    }

    public boolean isSent() {
        return isSent;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
