package com.example.bookwormconnect;

public class UserModel {
    private String UserName;
    private String UserBio;
    private String UserProfile;
    private String coverPhoto;

    public UserModel() {
    }

    public UserModel(String coverPhoto, String userProfile, String userBio, String userName) {
        this.coverPhoto = coverPhoto;
        UserProfile = userProfile;
        UserBio = userBio;
        UserName = userName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserBio() {
        return UserBio;
    }

    public void setUserBio(String userBio) {
        UserBio = userBio;
    }

    public String getUserProfile() {
        return UserProfile;
    }

    public void setUserProfile(String userProfile) {
        UserProfile = userProfile;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }
}

