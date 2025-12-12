package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    private String id;
    private User reviewer;
    private String comment;
    private double rating;
    private String date;
    private String type;   // "user" or "seller" or "all"

    public Review(String id, User reviewer, String comment,
                  double rating, String date, String type) {
        this.id = id;
        this.reviewer = reviewer;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
        this.type = type;
    }

    protected Review(Parcel in) {
        id = in.readString();
        reviewer = in.readParcelable(User.class.getClassLoader());
        comment = in.readString();
        rating = in.readDouble();
        date = in.readString();
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

    public String getId() { return id; }
    public User getReviewer() { return reviewer; }
    public String getComment() { return comment; }
    public double getRating() { return rating; }
    public String getDate() { return date; }
    public String getType() { return type; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(reviewer, flags);
        dest.writeString(comment);
        dest.writeDouble(rating);
        dest.writeString(date);
        dest.writeString(type);
    }
}
