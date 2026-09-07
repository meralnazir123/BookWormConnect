package com.example.bookwormconnect;

/**
 * UserModel — data class for a user shown in the chat list.
 *
 * Firestore collection: users/{uid}
 * Fields:
 *   uid        — Firebase Auth UID
 *   username   — display name
 *   profilepic — download URL of profile picture
 *   fcmToken   — FCM device token (used for push notifications)
 *   chatId     — the chat room ID (set locally when loading the list)
 *   requestId  — the linked request document ID
 */
public class UserModel {

    private String uid;
    private String username;
    private String profilepic;
    private String fcmToken;
    private String chatId;      // not stored in Firestore — set by adapter caller
    private String requestId;   // not stored in Firestore — set by adapter caller

    // ── Required empty constructor for Firestore ───────────────────────
    public UserModel() {}

    public UserModel(String uid, String username, String profilepic) {
        this.uid        = uid;
        this.username   = username;
        this.profilepic = profilepic;
    }

    // ── Getters ────────────────────────────────────────────────────────
    public String getUid()        { return uid; }
    public String getUsername()   { return username; }
    public String getProfilepic() { return profilepic; }
    public String getFcmToken()   { return fcmToken; }
    public String getChatId()     { return chatId; }
    public String getRequestId()  { return requestId; }

    // ── Setters ────────────────────────────────────────────────────────
    public void setUid(String uid)              { this.uid = uid; }
    public void setUsername(String username)    { this.username = username; }
    public void setProfilepic(String pic)       { this.profilepic = pic; }
    public void setFcmToken(String token)       { this.fcmToken = token; }
    public void setChatId(String chatId)        { this.chatId = chatId; }
    public void setRequestId(String requestId)  { this.requestId = requestId; }
}