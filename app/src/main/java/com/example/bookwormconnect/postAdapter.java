package com.example.bookwormconnect;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class postAdapter extends RecyclerView.Adapter<postAdapter.PostViewHolder> {

    private List<postEntity> postList;

    public postAdapter(List<postEntity> postList){
        this.postList=postList;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item,parent,false);


        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        postEntity post = postList.get(position);

        holder.username.setText(post.username);
        holder.bookType.setText(post.bookType);
        holder.description.setText(post.description);

        Glide.with(holder.postImage.getContext())
                .load(post.imageUrl)
                .into(holder.postImage);

        holder.description.setText(
                post.description == null || post.description.isEmpty()
                        ? "No description"
                        : post.description
        );
        Log.d("POST_DEBUG",
                "bookType = " + post.bookType +
                        " | description = " + post.description);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder{

        ImageView postImage;
        TextView bookType;
        TextView username;
        TextView description;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage=itemView.findViewById(R.id.postImage);
            username=itemView.findViewById(R.id.username);
            description=itemView.findViewById(R.id.description);
            bookType=itemView.findViewById(R.id.bookType);
        }
    }
}
