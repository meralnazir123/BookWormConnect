package com.example.bookwormconnect;

public class ChatListItem {

    private String chatId;
    private String otherUserId;
    private String username;
    private String requestId;
    private String bookDescription;

    public ChatListItem(){}

    public ChatListItem(
            String chatId,
            String otherUserId,
            String username,
            String requestId,
            String bookDescription) {

        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.username = username;
        this.requestId = requestId;
        this.bookDescription = bookDescription;
    }

    public String getChatId() {
        return chatId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBookDescription() {
        return bookDescription;

    }
}