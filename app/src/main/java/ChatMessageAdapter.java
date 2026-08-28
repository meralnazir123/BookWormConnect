package com.example.bookwormconnect;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ChatMessageAdapter — renders chat bubbles in the RecyclerView.
 *
 * View types:
 *   TYPE_SENT     (0) — messages sent by the current user (right-aligned)
 *   TYPE_RECEIVED (1) — messages from the other user (left-aligned)
 *
 * Uses item_chat_message.xml for both types; gravity / colors are
 * set programmatically based on the sender.
 */
public class ChatMessageAdapter
        extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int TYPE_SENT     = 0;
    private static final int TYPE_RECEIVED = 1;

    private final List<ChatMessage> messages;
    private final String            currentUserId;

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentBy(currentUserId)
                ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        boolean     isSent = msg.isSentBy(currentUserId);

        holder.tvMessage.setText(msg.getText());

        // Timestamp
        if (msg.getTimestamp() != null) {
            holder.tvTime.setText(TIME_FMT.format(msg.getTimestamp()));
        } else {
            holder.tvTime.setText("");
        }

        // Tag label (predefined vs custom)
        if (msg.isQuestion()) {
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText("❓ Question");
        } else if (msg.isAnswer()) {
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText("💬 Answer");
        } else {
            holder.tvTag.setVisibility(View.GONE);
        }

        // Align bubble: sent → right, received → left
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) holder.bubbleContainer.getLayoutParams();

        if (isSent) {
            params.gravity = Gravity.END;
            holder.bubbleContainer.setBackgroundResource(R.drawable.bg_bubble_sent);
            holder.tvMessage.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(android.R.color.white, null));
        } else {
            params.gravity = Gravity.START;
            holder.bubbleContainer.setBackgroundResource(R.drawable.bg_bubble_received);
            holder.tvMessage.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(R.color.textPrimary, null));
        }
        holder.bubbleContainer.setLayoutParams(params);

        // Outer row alignment
        holder.rowContainer.setGravity(isSent ? Gravity.END : Gravity.START);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // ── ViewHolder ─────────────────────────────────────────────────────
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        LinearLayout bubbleContainer;
        TextView     tvMessage;
        TextView     tvTime;
        TextView     tvTag;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer    = itemView.findViewById(R.id.rowContainer);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
            tvMessage       = itemView.findViewById(R.id.tvMessage);
            tvTime          = itemView.findViewById(R.id.tvTime);
            tvTag           = itemView.findViewById(R.id.tvTag);
        }
    }
}
