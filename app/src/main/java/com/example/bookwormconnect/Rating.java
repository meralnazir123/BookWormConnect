package com.example.bookwormconnect;

public class Rating {
    private double rating;

    // 🔹 Empty constructor required by Firestore
    public Rating() {}

    // 🔹 Constructor to set rating
    public Rating(double rating) {
        this.rating = rating;
    }

    // 🔹 Getter
    public double getRating() {
        return rating;
    }

    // 🔹 Setter
    public void setRating(double rating) {
        this.rating = rating;
    }
}
