package com.example.bookwormconnect;

public class msgModelclass  {
    private String message;
    private String senderid;
    private long timeStamp;
    private String receiverId;
    private String bookId;
    private String questionId;
    private String messageType;

    public msgModelclass() {
    }

    public msgModelclass(String message,
                         String senderid,
                         Long timeStamp) {
        this.message = message;
        this.senderid = senderid;
        this.timeStamp = timeStamp;
        this.messageType = "TEXT";
    }
    public msgModelclass(String message,
                         String senderid,
                         String receiverId,
                         String bookId,
                         String questionId,
                         String messageType,
                         long timeStamp) {
        this.message = message;
        this.senderid = senderid;
        this.receiverId = receiverId;
        this.bookId = bookId;
        this.questionId = questionId;
        this.messageType = messageType;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getBookId(String bookId) {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}