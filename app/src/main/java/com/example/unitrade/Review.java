package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    private String id;
    private User reviewer;
    private String comment;
    private double rating;
    private String date;
    private long timestamp;
    private String type; // "user" or "seller" or "all"

    private String targetUserId;

    // Required by Firestore
    public Review() {
    }

    public Review(String id, User reviewer, String targetUserId, String comment,
            double rating, long timestamp, String type) {
        this.id = id;
        this.reviewer = reviewer;
        this.targetUserId = targetUserId;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
        this.type = type;
    }

    protected Review(Parcel in) {
        id = in.readString();
        reviewer = in.readParcelable(User.class.getClassLoader());
        targetUserId = in.readString();
        comment = in.readString();
        rating = in.readDouble();
        timestamp = in.readLong();
        type = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    // Getters
    public String getId() {
        return id;
    }

    public User getReviewer() {
        return reviewer;
    }

    public String getComment() {
        return comment;
    }

    public double getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() { return timestamp; }

    public String getTargetUserId() {
        return targetUserId;
    }


    // Setters (Required for Firestore)
    public void setId(String id) {
        this.id = id;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(reviewer, flags);
        dest.writeString(targetUserId);
        dest.writeString(comment);
        dest.writeDouble(rating);
        dest.writeLong(timestamp);
        dest.writeString(type);
    }
}
