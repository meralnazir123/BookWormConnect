package com.example.bookwormconnect;

public class request {

    public String bookDescription;

    public String requestId;
    public String senderId;
    public String receiverId;
    public String address;
    public String status;
    public boolean chatEnabled;
    public String chatId;

    // NEW
    public String postId;

    public request() {
    }

    public request(String senderId,
                   String receiverId,
                   String address,
                   String Description,
                   String postId) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.address = address;
        this.status = "pending";
        this.bookDescription = Description;
        this.chatEnabled = false;
        this.postId = postId;
    }
}