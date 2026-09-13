package com.example.bookwormconnect;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView depositRecycler;
    private RecyclerView requestRecycler;

    private AdminDepositAdapter depositAdapter;
    private AdminRequestAdapter requestAdapter;

    private final ArrayList<AdminDeposit> depositList = new ArrayList<>();
    private final ArrayList<AdminRequest> requestList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_deposits);

        db = FirebaseFirestore.getInstance();

        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAdminAccess(currentUserId);
    }

    private void checkAdminAccess(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    Boolean isAdmin = document.getBoolean("isAdmin");

                    if (isAdmin == null || !isAdmin) {
                        Toast.makeText(
                                this,
                                "Access denied",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                        return;
                    }

                    initializeAdminPanel();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Unable to verify admin access: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    private void initializeAdminPanel() {

        depositRecycler = findViewById(R.id.adminDepositRecycler);
        requestRecycler = findViewById(R.id.adminRequestRecycler);

        depositRecycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        requestRecycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        depositAdapter = new AdminDepositAdapter(
                this,
                depositList
        );

        requestAdapter = new AdminRequestAdapter(
                this,
                requestList
        );

        depositRecycler.setAdapter(depositAdapter);
        requestRecycler.setAdapter(requestAdapter);

        loadDeposits();
        loadReturnRequests();
    }

    private void loadDeposits() {
        db.collection("deposits")
                .orderBy(
                        "submittedAt",
                        Query.Direction.DESCENDING
                )
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(
                                this,
                                "Error loading deposits: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (value == null) {
                        return;
                    }

                    depositList.clear();

                    for (DocumentSnapshot document : value.getDocuments()) {

                        AdminDeposit deposit =
                                document.toObject(AdminDeposit.class);

                        if (deposit != null) {
                            deposit.setDepositId(
                                    document.getId()
                            );

                            depositList.add(deposit);
                        }
                    }

                    depositAdapter.notifyDataSetChanged();
                });
    }

    private void loadReturnRequests() {

        db.collection("requests")
                .whereIn(
                        "status",
                        java.util.Arrays.asList(
                                "overdue",
                                "returned"
                        )
                )
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(
                                this,
                                "Error loading return requests: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (value == null) {
                        return;
                    }

                    requestList.clear();

                    for (DocumentSnapshot document :
                            value.getDocuments()) {

                        AdminRequest request =
                                document.toObject(AdminRequest.class);

                        if (request != null) {
                            request.setRequestId(
                                    document.getId()
                            );

                            requestList.add(request);
                        }
                    }

                    requestAdapter.notifyDataSetChanged();
                });
    }
}