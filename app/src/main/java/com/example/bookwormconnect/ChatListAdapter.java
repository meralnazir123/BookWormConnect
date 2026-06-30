package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    Context context;
    ArrayList<ChatListItem> chatList;
    public ChatListAdapter(Context context,
                           ArrayList<ChatListItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtUser;
ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUser = itemView.findViewById(R.id.txtUsername);
            image=itemView.findViewById(R.id.imgProfile);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.chatlist_row,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ChatListItem item = chatList.get(position);

        holder.txtUser.setText(item.getUsername());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(item.getOtherUserId())
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String imageUrl = document.getString("profileImageUrl");

                        Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.user)
                                .circleCrop()
                                .into(holder.image);
                    }
                });

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, ChatActivity.class);

            intent.putExtra("chatId", item.getChatId());
            intent.putExtra("username", item.getUsername());
            intent.putExtra("requestId", item.getRequestId());
            intent.putExtra("otherUserId", item.getOtherUserId());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
