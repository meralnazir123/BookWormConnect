package com.example.bookwormconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LentBooksFragment extends Fragment {

    RecyclerView recyclerView;
    View emptyText;
    ArrayList<postmodel> list;
    postAdapter adapter;

    FirebaseFirestore db;

    public LentBooksFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_my_posts,
                container,
                false
        );

        recyclerView =
                view.findViewById(R.id.recyclerView);

        emptyText =
                view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        list = new ArrayList<>();

        adapter = new postAdapter(
                list,
                "LENT"
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadLentBooks();

        return view;
    }

    private void loadLentBooks() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String currentUserId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        db.collection("borrowedBooks")
                .whereEqualTo(
                        "ownerId",
                        currentUserId
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (var doc : queryDocumentSnapshots) {

                        postmodel post =
                                new postmodel();

                        post.docId =
                                doc.getId();

                        post.userId =
                                doc.getString("ownerId");

                        post.borrowerId =
                                doc.getString("borrowerId");

                        post.url =
                                doc.getString("url");

                        post.username =
                                doc.getString("username");

                        post.description =
                                doc.getString("description");

                        post.duration =
                                doc.getString("duration");

                        post.deposit =
                                doc.getString("deposit");

                        post.status =
                                doc.getString("status");

                        post.bookType =
                                doc.getString("bookType");

                        list.add(post);
                    }

                    adapter.notifyDataSetChanged();

                    updateEmptyState();

                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(),
                        "Failed to load lent books: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void updateEmptyState() {

        if (list.isEmpty()) {

            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            ((android.widget.TextView) emptyText)
                    .setText("You haven't lent any books yet");

        } else {

            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}