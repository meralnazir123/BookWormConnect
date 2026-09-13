package com.example.bookwormconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<AdminRequest> requestList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminRequestAdapter(
            Context context,
            ArrayList<AdminRequest> requestList
    ) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_admin_request,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        AdminRequest request = requestList.get(position);

        holder.statusText.setText(
                "Status: " + request.getStatus()
        );

        holder.returnDateText.setText(
                "Return Date: " +
                        (request.getReturnDate() == null
                                ? "Not set"
                                : request.getReturnDate())
        );

        holder.returnedDateText.setText(
                "Returned Date: " +
                        (request.getReturnedDate() == null
                                ? "Not returned"
                                : request.getReturnedDate())
        );

        holder.borrowerText.setText("Borrower: Loading...");
        holder.ownerText.setText("Owner: Loading...");

        loadUsername(
                request.getSenderId(),
                username -> holder.borrowerText.setText(
                        "Borrower: " + username
                )
        );

        loadUsername(
                request.getReceiverId(),
                username -> holder.ownerText.setText(
                        "Owner: " + username
                )
        );

        if ("overdue".equals(request.getStatus())) {

            holder.actionButton.setText("Mark Returned");
            holder.actionButton.setVisibility(View.VISIBLE);

            holder.actionButton.setOnClickListener(v -> {

                new AlertDialog.Builder(context)
                        .setTitle("Mark Book Returned")
                        .setMessage(
                                "Are you sure the book has been returned?"
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Yes",
                                (dialog, which) -> {

                                    String returnedDate =
                                            new SimpleDateFormat(
                                                    "d/M/yyyy",
                                                    Locale.getDefault()
                                            ).format(
                                                    new Date()
                                            );

                                    db.collection("requests")
                                            .document(
                                                    request.getRequestId()
                                            )
                                            .update(
                                                    "status",
                                                    "returned",
                                                    "returnedDate",
                                                    returnedDate
                                            )
                                            .addOnSuccessListener(unused -> {

                                                request.setStatus("returned");
                                                request.setReturnedDate(
                                                        returnedDate
                                                );

                                                notifyItemChanged(position);

                                            });
                                }
                        )
                        .show();
            });

        } else {

            holder.actionButton.setVisibility(View.GONE);
        }
    }

    private void loadUsername(
            String userId,
            UsernameCallback callback
    ) {
        if (userId == null || userId.isEmpty()) {
            callback.onUsernameLoaded("Unknown");
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String username =
                                document.getString("username");

                        if (username != null &&
                                !username.isEmpty()) {

                            callback.onUsernameLoaded(username);

                        } else {

                            callback.onUsernameLoaded("Unknown");
                        }

                    } else {

                        callback.onUsernameLoaded("Unknown");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onUsernameLoaded("Unknown")
                );
    }

    interface UsernameCallback {
        void onUsernameLoaded(String username);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView statusText;
        TextView returnDateText;
        TextView returnedDateText;
        TextView borrowerText;
        TextView ownerText;
        Button actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            statusText =
                    itemView.findViewById(R.id.adminRequestStatus);

            returnDateText =
                    itemView.findViewById(R.id.adminReturnDate);

            returnedDateText =
                    itemView.findViewById(R.id.adminReturnedDate);

            borrowerText =
                    itemView.findViewById(R.id.adminBorrower);

            ownerText =
                    itemView.findViewById(R.id.adminOwner);

            actionButton =
                    itemView.findViewById(R.id.adminRequestAction);
        }
    }
}
