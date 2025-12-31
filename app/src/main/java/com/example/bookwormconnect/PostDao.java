package com.example.bookwormconnect;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(postEntity post);

    @Query("SELECT * FROM posts ORDER BY time DESC")
    List<postEntity> getAllPosts();

    @Query("DELETE FROM posts")

    void deleteAll();
}
