package com.example.bookwormconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookwormconnect.databinding.ActivityPostAdapter2Binding;  // Adjust package name

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class PostAdapter2 extends RecyclerView.Adapter<PostAdapter2.PostViewHolder> {

    ActivityPostAdapter2Binding binding;

    private List<Post> posts;

    public PostAdapter2(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ActivityPostAdapter2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Glide.with(holder.binding.getRoot().getContext()).load(post.getImageUrl()).into(holder.binding.postImage);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ActivityPostAdapter2Binding binding;

        public PostViewHolder(@NonNull ActivityPostAdapter2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}