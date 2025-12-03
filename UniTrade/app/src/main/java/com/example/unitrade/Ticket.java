package com.example.unitrade;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ticket implements Parcelable {

    private String subject;
    private String description;
    private Uri attachmentUri;
    private long timestamp;

    public Ticket(String subject, String description, Uri attachmentUri, long timestamp) {
        this.subject = subject;
        this.description = description;
        this.attachmentUri = attachmentUri;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public Uri getAttachmentUri() { return attachmentUri; }
    public long getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Parcelable implementation
    protected Ticket(Parcel in) {
        subject = in.readString();
        description = in.readString();
        attachmentUri = in.readParcelable(Uri.class.getClassLoader());
        timestamp = in.readLong();
    }

    public static final Creator<Ticket> CREATOR = new Creator<Ticket>() {
        @Override
        public Ticket createFromParcel(Parcel in) {
            return new Ticket(in);
        }

        @Override
        public Ticket[] newArray(int size) {
            return new Ticket[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeString(description);
        dest.writeParcelable(attachmentUri, flags);
        dest.writeLong(timestamp);
    }
}
