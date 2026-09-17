package com.example.bookwormconnect;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ChatMessage {

    private String  messageId;
    private String  senderId;
    private String  text;
    private String  type;

    @ServerTimestamp
    private Date timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String text, String type) {
        this.senderId = senderId;
        this.text     = text;
        this.type     = type;
    }

    public String getMessageId()          { return messageId; }
    public void   setMessageId(String id) { this.messageId = id; }

    public String getSenderId()              { return senderId; }
    public void   setSenderId(String sid)    { this.senderId = sid; }

    public String getText()              { return text; }
    public void   setText(String t)      { this.text = t; }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    public Date   getTimestamp()         { return timestamp; }
    public void   setTimestamp(Date ts)  { this.timestamp = ts; }

    public boolean isSentBy(String uid)  { return uid != null && uid.equals(senderId); }

    public boolean isQuestion()          { return "predefined_question".equals(type); }

    public boolean isAnswer()            { return "predefined_answer".equals(type); }

    public boolean isCustom()            { return "custom".equals(type); }
}