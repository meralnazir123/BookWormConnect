package com.example.bookwormconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDepositAdapter
        extends RecyclerView.Adapter<AdminDepositAdapter.DepositViewHolder> {

    private Context context;
    private List<AdminDeposit> depositList;
    private FirebaseFirestore db;

    public AdminDepositAdapter(Context context,
                               List<AdminDeposit> depositList) {

        this.context = context;
        this.depositList = depositList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public DepositViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.admin_deposit_item, parent, false);

        return new DepositViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DepositViewHolder holder,
            int position) {

        AdminDeposit deposit = depositList.get(position);

        holder.amountTv.setText(
                "Amount: Rs. " + deposit.getAmount()
        );

        holder.referenceTv.setText(
                "Reference: " + deposit.getTransactionReference()
        );

        holder.statusTv.setText(
                "Status: " + deposit.getStatus()
        );

        holder.borrowerTv.setText(
                "Borrower ID: " + deposit.getBorrowerId()
        );
        if ("pending".equals(deposit.getStatus())) {

            holder.approveBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);

        } else {

            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
        }
        holder.approveBtn.setOnClickListener(v -> {

            String requestId = deposit.getRequestId();
            String bookId = deposit.getBookId();

            if (requestId == null || requestId.isEmpty()) {

                Toast.makeText(
                        context,
                        "Request ID is missing",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            if (bookId == null || bookId.isEmpty()) {

                Toast.makeText(
                        context,
                        "Book ID is missing",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            db.collection("deposits")
                    .document(deposit.getDepositId())
                    .update("status", "approved")

                    .addOnSuccessListener(unused -> {
                        Map<String, Object> updates = new HashMap<>();

                        updates.put("status", "Borrowed");
                        updates.put("borrowerId", deposit.getBorrowerId());

                        db.collection("posts")
                                .document(bookId)
                                .update(updates)

                                .addOnSuccessListener(aVoid -> {

                                    deposit.setStatus("approved");

                                    notifyItemChanged(position);


                                    Toast.makeText(
                                            context,
                                            "Deposit approved. Book is now Borrowed.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                })

                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            context,
                                            "Deposit approved but book status failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();

                                });

                    })

                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                context,
                                "Failed to approve deposit: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    });
        });

        holder.rejectBtn.setOnClickListener(v -> {

            db.collection("deposits")
                    .document(deposit.getDepositId())
                    .update("status", "rejected")

                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                context,
                                "Deposit rejected",
                                Toast.LENGTH_SHORT
                        ).show();

                        deposit.setStatus("rejected");

                        notifyItemChanged(position);
                    })

                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                context,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });
    }


    @Override
    public int getItemCount() {
        return depositList.size();
    }


    public static class DepositViewHolder
            extends RecyclerView.ViewHolder {

        TextView amountTv;
        TextView referenceTv;
        TextView statusTv;
        TextView borrowerTv;

        Button approveBtn;
        Button rejectBtn;

        public DepositViewHolder(@NonNull View itemView) {
            super(itemView);

            amountTv =
                    itemView.findViewById(R.id.amountTv);

            referenceTv =
                    itemView.findViewById(R.id.referenceTv);

            statusTv =
                    itemView.findViewById(R.id.statusTv);

            borrowerTv =
                    itemView.findViewById(R.id.borrowerTv);

            approveBtn =
                    itemView.findViewById(R.id.approveBtn);

            rejectBtn =
                    itemView.findViewById(R.id.rejectBtn);
        }
    }
}