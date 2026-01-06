package com.example.unitrade;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Report {

    private String id;
    private String reviewId;
    private String reportedByUserId;
    private String targetUserId;
    private String reason;
    private long timestamp;


    public Report() {}

    public Report(
            String reviewId,
            String reportedByUserId,
            String targetUserId,
            String reason,
            long timestamp
    ) {
        this.reviewId = reviewId;
        this.reportedByUserId = reportedByUserId;
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.timestamp = timestamp;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getReportedByUserId() {
        return reportedByUserId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
