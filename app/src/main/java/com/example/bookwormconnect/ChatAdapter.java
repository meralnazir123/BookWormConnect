package com.example.bookwormconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    Context context;
    ArrayList<ChatMessage> messages;

    public ChatAdapter(Context context,
                       ArrayList<ChatMessage> messages) {

        this.context = context;
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtMessage;
        ImageView imgReceipt;

        public ViewHolder(View itemView) {
            super(itemView);

            txtMessage =
                    itemView.findViewById(R.id.txtMessage);

            imgReceipt =
                    itemView.findViewById(R.id.imgMessage);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view;

        if(viewType == VIEW_TYPE_SENT){

            view = LayoutInflater.from(context)
                    .inflate(
                            R.layout.chat_sender,
                            parent,
                            false);

        }else{

            view = LayoutInflater.from(context)
                    .inflate(
                            R.layout.chat_receiver,
                            parent,
                            false);
        }

        return new ViewHolder(view);
    }
    @Override
    public int getItemViewType(int position) {

        String currentUserId =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

        if(messages.get(position)
                .getSenderId()
                .equals(currentUserId)) {

            return VIEW_TYPE_SENT;

        } else {

            return VIEW_TYPE_RECEIVED;
        }
    }
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        ChatMessage msg = messages.get(position);

        if(msg.getType().equals("text")){

            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.imgReceipt.setVisibility(View.GONE);

            holder.txtMessage.setText(msg.getText());

        }else{

            holder.txtMessage.setVisibility(View.GONE);
            holder.imgReceipt.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(msg.getImageUrl())
                    .into(holder.imgReceipt);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
