package com.example.bookwormconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewwholder> {
    ChatActivity chatActivity;
    ArrayList<Users> userArrayList;
    public UserAdapter(ChatActivity chatActivity, ArrayList<Users> userArrayList) {
        this.chatActivity=chatActivity;
        this.userArrayList=userArrayList;

    }

    @NonNull
    @Override
    public viewwholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(chatActivity).inflate(R.layout.user_item,parent,false);
        return new viewwholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewwholder holder, int position) {

        Users users = userArrayList.get(position);
        holder.username.setTextDirection(Integer.parseInt(users.userName));
        holder.userstatus.setTextDirection(Integer.parseInt(users.status));
        Picasso.get().load(users.profilepic).into(holder.userimg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chatActivity, chatwin.class);
                String name = "";
                intent.putExtra(name;:"nameeee",users.getUserName());
                intent.putExtra(name;:"receiverImg",users.getProfilepic());
                intent.putExtra(name;:"uid",users.getUserId());
                chatActivity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class viewwholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextureView username;
        TextureView userstatus;
        @SuppressLint("WrongViewCast")
        public viewwholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}