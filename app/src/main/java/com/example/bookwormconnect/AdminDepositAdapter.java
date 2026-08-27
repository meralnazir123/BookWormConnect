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

import java.util.List;

public class AdminDepositAdapter
        extends RecyclerView.Adapter<AdminDepositAdapter.DepositViewHolder> {

    private Context context;
    private List<AdminDeposit> depositList;
    private FirebaseFirestore db;

    public AdminDepositAdapter(Context context, List<AdminDeposit> depositList) {
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

        // Show buttons only when payment is pending
        if ("pending".equals(deposit.getStatus())) {

            holder.approveBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);

        } else {

            holder.approveBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
        }

        // APPROVE
        holder.approveBtn.setOnClickListener(v -> {

            db.collection("deposits")
                    .document(deposit.getDepositId())
                    .update("status", "approved")
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                context,
                                "Deposit approved",
                                Toast.LENGTH_SHORT
                        ).show();

                        deposit.setStatus("approved");

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

        // REJECT
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
        TextView methodTv;
        TextView referenceTv;
        TextView statusTv;
        TextView borrowerTv;

        Button approveBtn;
        Button rejectBtn;

        public DepositViewHolder(@NonNull View itemView) {
            super(itemView);

            amountTv = itemView.findViewById(R.id.amountTv);
            referenceTv = itemView.findViewById(R.id.referenceTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            borrowerTv = itemView.findViewById(R.id.borrowerTv);
            approveBtn = itemView.findViewById(R.id.approveBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
        }
    }
}