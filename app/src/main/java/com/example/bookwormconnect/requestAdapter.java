package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class requestAdapter extends RecyclerView.Adapter<requestAdapter.ViewHolder> {

    Context context;
    List<request> list;


    public requestAdapter(Context context, List<request> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        request request = list.get(position);
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null &&
                currentUserId.equals(request.receiverId)) {
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.declineBtn.setVisibility(View.VISIBLE);
        } else {
            holder.acceptBtn.setVisibility(View.GONE);
            holder.declineBtn.setVisibility(View.GONE);
        }
        holder.statusTv.setText(request.status);
        holder.bookTv.setText(request.bookDescription);

        holder.addressTv.setText(
                request.address != null ? "Address: " + request.address : "No Address"
        );
        holder.statusTv.setText(
                request.status != null ? "Status: "+request.status : "pending"
        );


        FirebaseFirestore.getInstance()
                .collection("users")
                .document(request.senderId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        holder.nameTv.setText(document.getString("username"));

                    }
                });

        holder.acceptBtn.setOnClickListener(v -> acceptRequest(request));
        holder.declineBtn.setOnClickListener(v -> declineRequest(request));
    }

    private void acceptRequest(request request) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String chatId =
                request.senderId + "_" + request.receiverId;

        Map<String, Object> chat =
                new HashMap<>();

        chat.put("chatId", chatId);
        chat.put("requestId", request.requestId);
        chat.put("senderId", request.senderId);
        chat.put("receiverId", request.receiverId);
        chat.put("bookDescription", request.bookDescription);
        chat.put("participants",
                Arrays.asList(
                        request.senderId,
                        request.receiverId));

        db.collection("chats")
                .document(chatId)
                .set(chat)
                .addOnSuccessListener(unused -> {

                    db.collection("requests")
                            .document(request.requestId)
                            .update(
                                    "status", "accepted",
                                    "chatEnabled", true,
                                    "chatId", chatId
                            );

                    Toast.makeText(
                            context,
                            "Request Accepted",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent =
                            new Intent(
                                    context,
                                    ChatActivity.class);

                    intent.putExtra(
                            "chatId",
                            chatId);

                    intent.putExtra(
                            "requestId",
                            request.requestId);

                    context.startActivity(intent);
                });
    }

    private void declineRequest(request request) {

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(request.requestId)
                .update(
                        "status", "declined",
                        "chatEnabled", false
                );

        Toast.makeText(
                context,
                "Request Declined",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, addressTv,statusTv;
        TextView bookTv;
        Button acceptBtn, declineBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTv = itemView.findViewById(R.id.bookTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
        }
    }
}