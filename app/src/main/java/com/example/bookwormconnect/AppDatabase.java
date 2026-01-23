package com.example.bookwormconnect;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import androidx.room.Database;

@Database(entities = {}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase instance;


    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "bookworm_db"
            ).allowMainThreadQueries().build();
        }
        return instance;
    }
}
