package com.example.bookwormconnect;

public class ChatListItem {

    private String chatId;
    private String otherUserId;
    private String username;
    private String requestId;

    public ChatListItem(){}

    public ChatListItem(
            String chatId,
            String otherUserId,
            String username,
            String requestId) {

        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.username = username;
        this.requestId = requestId;
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
}