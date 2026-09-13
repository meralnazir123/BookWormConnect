package com.example.bookwormconnect;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class requestActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    requestAdapter adapter;
    View emptyRequestsText;

    List<request> requestList = new ArrayList<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request);

        recyclerView = findViewById(R.id.recyclerView);
        emptyRequestsText = findViewById(R.id.emptyRequestsText);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new requestAdapter(
                this,
                requestList
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadRequests();
    }

    private void loadRequests() {

        String currentUserId =
                FirebaseAuth.getInstance().getUid();

        db.collection("requests")
                .whereEqualTo(
                        "receiverId",
                        currentUserId
                )
                .whereEqualTo(
                        "status",
                        "pending"
                )
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) {
                        return;
                    }

                    requestList.clear();

                    for (DocumentSnapshot doc :
                            value.getDocuments()) {

                        request r =
                                doc.toObject(request.class);

                        if (r != null) {

                            r.requestId =
                                    doc.getId();

                            requestList.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {

                        recyclerView.setVisibility(
                                View.GONE
                        );

                        emptyRequestsText.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        recyclerView.setVisibility(
                                View.VISIBLE
                        );

                        emptyRequestsText.setVisibility(
                                View.GONE
                        );
                    }
                });
    }
}