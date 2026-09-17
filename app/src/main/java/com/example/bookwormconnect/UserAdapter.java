package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context           context;
    private final List<UserModel>   userList;

    public UserAdapter(@NonNull Context context, @NonNull List<UserModel> userList) {
        this.context  = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_row_users, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.tvUsername.setText(
                user.getUsername() != null ? user.getUsername() : "Unknown User"
        );

        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilepic())
                    .placeholder(R.drawable.user)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.user);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId",    user.getChatId());
            intent.putExtra("requestId", user.getRequestId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static final class UserViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final TextView        tvUsername;

        private UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvUsername   = itemView.findViewById(R.id.tvUsername);
        }
    }
}