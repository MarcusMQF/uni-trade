package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {
    private String name;
    private String lastMessage;
    private String timestamp;
    private String avatarUrl;
    private boolean isBookmarked;
    private String userId;

    public Chat(String name, String lastMessage, String timestamp, String avatarUrl, boolean isBookmarked, String userId) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.isBookmarked = isBookmarked;
        this.userId = userId;
    }

    protected Chat(Parcel in) {
        name = in.readString();
        lastMessage = in.readString();
        timestamp = in.readString();
        avatarUrl = in.readString();
        isBookmarked = in.readByte() != 0;
        userId = in.readString();
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

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
    
    public String getUserId() {
        return userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(lastMessage);
        dest.writeString(timestamp);
        dest.writeString(avatarUrl);
        dest.writeByte((byte) (isBookmarked ? 1 : 0));
        dest.writeString(userId);
    }
}
