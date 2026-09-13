package com.example.bookwormconnect;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookwormconnect.databinding.ActivityPostAdapter2Binding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class PostAdapter2
        extends RecyclerView.Adapter<PostAdapter2.PostViewHolder> {

    private final List<Post> posts;
    private final boolean isOwnProfile;
    private final OnPostDeletedListener listener;

    public interface OnPostDeletedListener {
        void onPostDeleted();
    }

    public PostAdapter2(
            List<Post> posts,
            boolean isOwnProfile,
            OnPostDeletedListener listener
    ) {
        this.posts = posts;
        this.isOwnProfile = isOwnProfile;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        ActivityPostAdapter2Binding binding =
                ActivityPostAdapter2Binding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PostViewHolder holder,
            int position
    ) {

        Post post = posts.get(position);

        Glide.with(holder.binding.getRoot().getContext())
                .load(post.getImageUrl())
                .into(holder.binding.postImage);

        holder.binding.postImage.setOnClickListener(v ->
                showEnlargedImage(
                        holder.binding.getRoot(),
                        post.getImageUrl()
                )
        );

        if (isOwnProfile) {

            holder.binding.postImage.setOnLongClickListener(v -> {

                showDeleteDialog(
                        holder.binding.getRoot(),
                        post,
                        holder.getBindingAdapterPosition()
                );

                return true;
            });

        } else {

            holder.binding.postImage.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void showEnlargedImage(
            View anchorView,
            String imageUrl
    ) {

        Dialog dialog = new Dialog(
                anchorView.getContext()
        );

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        ImageView imageView = new ImageView(
                anchorView.getContext()
        );

        imageView.setBackgroundColor(Color.BLACK);

        imageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        Glide.with(anchorView.getContext())
                .load(imageUrl)
                .into(imageView);

        imageView.setOnClickListener(v ->
                dialog.dismiss()
        );

        dialog.setContentView(imageView);

        Window window = dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(Color.BLACK)
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        dialog.show();

        if (window != null) {

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );

            window.setGravity(Gravity.CENTER);
        }
    }

    private void showDeleteDialog(
            View anchorView,
            Post post,
            int position
    ) {

        new AlertDialog.Builder(
                anchorView.getContext()
        )
                .setTitle("Delete Post")
                .setMessage(
                        "Are you sure you want to delete this post?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deletePost(
                                        anchorView,
                                        post,
                                        position
                                )
                )
                .show();
    }

    private void deletePost(
            View anchorView,
            Post post,
            int position
    ) {

        if (post.getDocId() == null ||
                post.getDocId().isEmpty()) {

            Toast.makeText(
                    anchorView.getContext(),
                    "Unable to delete post",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.getDocId())
                .delete()
                .addOnSuccessListener(unused -> {

                    if (post.getImageUrl() != null &&
                            !post.getImageUrl().isEmpty()) {

                        try {

                            FirebaseStorage
                                    .getInstance()
                                    .getReferenceFromUrl(
                                            post.getImageUrl()
                                    )
                                    .delete()
                                    .addOnCompleteListener(task ->
                                            removePostFromList(
                                                    position
                                            )
                                    );

                        } catch (Exception e) {

                            removePostFromList(position);
                        }

                    } else {

                        removePostFromList(position);
                    }

                    Toast.makeText(
                            anchorView.getContext(),
                            "Post deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            anchorView.getContext(),
                            "Failed to delete post",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void removePostFromList(int position) {

        if (position >= 0 &&
                position < posts.size()) {

            posts.remove(position);

            notifyItemRemoved(position);

            if (listener != null) {
                listener.onPostDeleted();
            }
        }
    }

    static class PostViewHolder
            extends RecyclerView.ViewHolder {

        ActivityPostAdapter2Binding binding;

        public PostViewHolder(
                @NonNull ActivityPostAdapter2Binding binding
        ) {

            super(binding.getRoot());

            this.binding = binding;
        }
    }
}