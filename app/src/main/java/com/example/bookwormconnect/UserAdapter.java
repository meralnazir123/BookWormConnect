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

/**
 * UserAdapter — shows a list of users in the chat list screen.
 * Clicking a user opens ChatActivity with the correct chatId and requestId.
 *
 * Layout used: res/layout/row_users.xml  (created below)
 *
 * Fields read from UserModel:
 *   uid, username, profilepic, requestId
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context       context;
    private final List<UserModel> userList;

    public UserAdapter(Context context, List<UserModel> userList) {
        this.context  = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Use parent.getContext() — NOT ChatActivity directly
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_users, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // Set username
        holder.tvUsername.setText(
                user.getUsername() != null ? user.getUsername() : "Unknown User"
        );

        // Load profile picture with Glide
        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilepic())
                    .placeholder(R.drawable.user)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.user);
        }

        // ✅ On click — open ChatActivity (NOT chatwin)
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

    // ── ViewHolder ─────────────────────────────────────────────────────
    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView        tvUsername;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvUsername   = itemView.findViewById(R.id.tvUsername);
        }
    }
}