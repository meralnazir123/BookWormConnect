package com.example.bookwormconnect;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class postEntity {

        @PrimaryKey(autoGenerate = true)
        public int id;

        public String imageUrl;

        public String username;
        public String bookType;
        public String description;
        public long time;
    }

