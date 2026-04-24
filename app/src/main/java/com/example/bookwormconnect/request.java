package com.example.bookwormconnect;

public class request {

    public String requestId;
    public String senderId;
    public static String receiverId;
    public String address;
    public static String status;

    public request() {
    }

    public request(String senderId, String receiverId, String address) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.address = address;
        this.status = "pending";
    }
}