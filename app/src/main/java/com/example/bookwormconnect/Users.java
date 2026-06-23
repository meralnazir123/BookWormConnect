package com.example.bookwormconnect;

public class Users {
    String profilepic,userName,lastMessage,status;

    public Users(String uid, String name, String email, String password, String finalimageUri, String status){}

    public Users(String profilepic, String userName, String lastMessage, String status){
        this.profilepic=profilepic;
        this.userName=userName;
        this.lastMessage=lastMessage;
        this.status=status;
    }


    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void getUserId() {
    }
}