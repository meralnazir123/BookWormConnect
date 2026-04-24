package com.example.bookwormconnect;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class postmodel {

    @PrimaryKey(autoGenerate = true)

    public int id;
public String docId;
    public String userId;

    public String url;
    public String username;
    String bookType;
    String description;
    public long time;
    public String duration;
    public String deposit;

    public postmodel() {}

    public
    postmodel(String url, String username,String bookType,
    String description, long time) {
        this.url = url;
        this.username = username;
        this.time=time;
        this.bookType=bookType;
        this.description=description;
    }
    public String getImageUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
    public String getBookType() {
        return bookType;
    }
    public String getDescription() {
        return description;
    }

    public long getTime() {
        return time;
    }
}
