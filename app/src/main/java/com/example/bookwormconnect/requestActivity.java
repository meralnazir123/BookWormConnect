package com.example.bookwormconnect;

import android.os.Bundle;

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

    List<request> requestList = new ArrayList<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new requestAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadRequests();
    }

    private void loadRequests() {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        db.collection("requests")
                .whereEqualTo("senderId", currentUserId);

        db.collection("requests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    requestList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        request r = doc.toObject(request.class);
                        r.requestId = doc.getId();
                        requestList.add(r);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
