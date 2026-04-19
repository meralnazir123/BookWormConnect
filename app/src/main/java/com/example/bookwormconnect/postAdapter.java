package com.example.bookwormconnect;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class postAdapter extends RecyclerView.Adapter<postAdapter.PostViewHolder> {

    private List<postmodel> postList;

    public postAdapter(List<postmodel> postList){
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

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postmodel post = postList.get(position);
        if (post.userId != null && post.userId.equals(currentUserId)) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }
holder.deleteBtn.setOnClickListener(v -> {
    new AlertDialog.Builder(holder.itemView.getContext()).setTitle("Delete Post")
            .setMessage("Are you sure?").setPositiveButton("Delete", (dialog, which) -> {
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(post.getImageUrl());
                storageRef.delete().addOnSuccessListener(aVoid-> {
                    FirebaseFirestore.getInstance().collection("posts")
                            .document(post.docId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                postList.remove(position);
                                notifyItemRemoved(position);
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(),
                            "Failed to delete image", Toast.LENGTH_SHORT).show();
                });
            }).setNegativeButton("Cancel",null).show();
});



        holder.username.setText(post.username);
        holder.bookType.setText(post.bookType);
        holder.description.setText(post.description);

        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
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

    public void setFilteredList(ArrayList<postmodel> filteredList) {
        this.postList = filteredList;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder{

        ImageView postImage;
        ImageButton deleteBtn;
        TextView bookType;
        TextView username;
        TextView description;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteBtn=itemView.findViewById(R.id.deleteBtn);
            postImage=itemView.findViewById(R.id.postImage);
            username=itemView.findViewById(R.id.username);
            description=itemView.findViewById(R.id.description);
            bookType=itemView.findViewById(R.id.bookType);
        }
    }
}
