package com.example.bookwormconnect;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * ChatMessage — Firestore document model for a single chat message.
 *
 * Firestore collection: chats/{chatId}/messages/{messageId}
 *
 * Fields:
 *   senderId  — UID of the user who sent the message
 *   text      — The message content
 *   type      — "predefined_question" | "predefined_answer" | "custom"
 *   timestamp — Server timestamp for ordering
 */
public class ChatMessage {

    private String  messageId;   // local only (not stored in Firestore)
    private String  senderId;
    private String  text;
    private String  type;        // predefined_question | predefined_answer | custom

    @ServerTimestamp
    private Date timestamp;

    // ── Required empty constructor for Firestore deserialization ───────
    public ChatMessage() {}

    public ChatMessage(String senderId, String text, String type) {
        this.senderId = senderId;
        this.text     = text;
        this.type     = type;
    }

    // ── Getters & setters ──────────────────────────────────────────────
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

    /** Helper — returns true when this message was sent by the given uid */
    public boolean isSentBy(String uid)  { return uid != null && uid.equals(senderId); }

    /** Helper — returns true when this is a predefined question chip */
    public boolean isQuestion()          { return "predefined_question".equals(type); }

    /** Helper — returns true when this is a predefined answer chip */
    public boolean isAnswer()            { return "predefined_answer".equals(type); }

    /** Helper — returns true when this is a custom typed message */
    public boolean isCustom()            { return "custom".equals(type); }
}