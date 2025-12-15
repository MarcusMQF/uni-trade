package com.example.unitrade;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Ticket implements Parcelable {

    public static final String STATUS_NOT_SEEN = "Not Seen Yet";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";

    private String subject;
    private String description;
    private String attachmentUri; // Changed to String for better Gson compatibility
    private long timestamp;
    private String status;

    public Ticket(String subject, String description, String attachmentUri, long timestamp) {
        this.subject = subject;
        this.description = description;
        this.attachmentUri = attachmentUri;
        this.timestamp = timestamp;
        this.status = STATUS_NOT_SEEN; // Default status for new tickets
    }

    // Getters
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getAttachmentUri() { return attachmentUri; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Parcelable implementation
    protected Ticket(Parcel in) {
        subject = in.readString();
        description = in.readString();
        attachmentUri = in.readString();
        timestamp = in.readLong();
        status = in.readString();
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
        dest.writeString(attachmentUri);
        dest.writeLong(timestamp);
        dest.writeString(status);
    }
}
