package com.example.bookwormconnect;

public class request {

    public String requestId;
    public String senderId;
    public String receiverId;
    public String address;
    public String status;
    public boolean chatEnabled;
    public String chatId;

    public request() {
    }

    public request(String senderId,
                   String receiverId,
                   String address) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.address = address;
        this.status = "pending";
        this.chatEnabled = false;
    }
}