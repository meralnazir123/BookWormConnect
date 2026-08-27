package com.example.bookwormconnect;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminDepositAdapter adapter;

    private ArrayList<AdminDeposit> depositList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_deposits);

        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null ||
                !currentUserId.equals("8irufe7ft4PmqWvUBxMWeH7Y9oI3")) {

            Toast.makeText(
                    this,
                    "Access denied",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        recyclerView = findViewById(R.id.adminDepositRecycler);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new AdminDepositAdapter(
                this,
                depositList
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadDeposits();
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
                                "Error loading deposits: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (value == null) {
                        return;
                    }

                    depositList.clear();

                    for (DocumentSnapshot document :
                            value.getDocuments()) {

                        AdminDeposit deposit =
                                document.toObject(AdminDeposit.class);

                        if (deposit != null) {

                            deposit.setDepositId(
                                    document.getId()
                            );

                            depositList.add(deposit);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}