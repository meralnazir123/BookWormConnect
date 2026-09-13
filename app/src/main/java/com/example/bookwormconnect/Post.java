package com.example.bookwormconnect;

public class Post {

    private String imageUrl;
    private String description;
    private String docId;

    public Post() {
    }

    public Post(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}