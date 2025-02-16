package com.example.theeventhub;

public class Feedback {
    private String reviewerName;
    private String reviewDate;
    private String reviewText;

    public Feedback(String reviewerName, String reviewDate, String reviewText) {
        this.reviewerName = reviewerName;
        this.reviewDate = reviewDate;
        this.reviewText = reviewText;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public String getReviewText() {
        return reviewText;
    }
}
