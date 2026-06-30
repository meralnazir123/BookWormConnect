package com.example.bookwormconnect;

public class ChatMessage {

    private String senderId;
    private String text;
    private String imageUrl;
    private String type;

    public ChatMessage() {}

    public ChatMessage(String senderId,
                       String text,
                       String imageUrl,
                       String type,
                       long timestamp) {

        this.senderId = senderId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getType() {
        return type;
    }
}