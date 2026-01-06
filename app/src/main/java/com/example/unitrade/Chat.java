package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {

    private String userId; // Seller / receiver ID
    private String lastMessage;
    private long lastMessageTime; // Timestamp in millis
    private boolean isBookmarked;

    // ---------- Constructor ----------
    public Chat(String userId, String lastMessage, long lastMessageTime, boolean isBookmarked) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.isBookmarked = isBookmarked;
    }

    // ---------- Parcelable ----------
    protected Chat(Parcel in) {
        userId = in.readString();
        lastMessage = in.readString();
        lastMessageTime = in.readLong();
        isBookmarked = in.readByte() != 0;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    // ---------- Getters ----------
    public String getUserId() {
        return userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    // ---------- Setters ----------
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    // ---------- Parcelable ----------
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(lastMessage);
        dest.writeLong(lastMessageTime);
        dest.writeByte((byte) (isBookmarked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}