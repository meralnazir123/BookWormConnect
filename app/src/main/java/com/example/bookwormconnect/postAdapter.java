package com.example.bookwormconnect;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private void showRequestDialog(View itemView, String bookId, String ownerId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        View view = LayoutInflater.from(itemView.getContext())
                .inflate(R.layout.activity_request_form, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText addressInput = view.findViewById(R.id.addressInput);
        Button submitBtn = view.findViewById(R.id.submitBtn);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        submitBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();

            if (address.isEmpty()) {
                Toast.makeText(itemView.getContext(), "Fill address please", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId.equals(ownerId)) {
                Toast.makeText(itemView.getContext(), "You cannot request your own book", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> request = new HashMap<>();
            request.put("bookId", bookId);
            request.put("senderId", currentUserId);
            request.put("receiverId", ownerId);
            request.put("address", address);
            request.put("status", "pending");
            request.put("chatEnabled", false);
            request.put("timestamp", FieldValue.serverTimestamp());

            db.collection("requests")
                    .add(request)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(itemView.getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
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
        holder.requestBtn.setOnClickListener(v -> {
            String bookId = post.docId;      // or your book ID field
            String ownerId = post.userId;

            showRequestDialog(holder.itemView, bookId, ownerId);
        });
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
        holder.durationTv.setText("Duration: " + post.duration);
        holder.depositTv.setText("Deposit: Rs " + post.deposit);

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
        ImageButton deleteBtn;
        ImageButton requestBtn;

        ImageView postImage;
        TextView bookType;
        TextView username;
        TextView description;
        TextView durationTv, depositTv;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            requestBtn = itemView.findViewById(R.id.requestButton);
            deleteBtn=itemView.findViewById(R.id.deleteBtn);
            postImage=itemView.findViewById(R.id.postImage);
            username=itemView.findViewById(R.id.username);
            description=itemView.findViewById(R.id.description);
            bookType=itemView.findViewById(R.id.bookType);
            durationTv = itemView.findViewById(R.id.durationTv);
            depositTv = itemView.findViewById(R.id.depositTv);
        }

    }
}
