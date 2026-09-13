package com.example.bookwormconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyPostsFragment extends Fragment {

    RecyclerView recyclerView;
    View emptyText;
    ArrayList<postmodel> list;
    postAdapter adapter;

    public MyPostsFragment() {
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
                new androidx.recyclerview.widget.LinearLayoutManager(
                        getContext()
                )
        );

        list = new ArrayList<>();

        adapter = new postAdapter(
                list,
                "MY_POSTS"
        );

        recyclerView.setAdapter(adapter);

        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String currentUserId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                            : queryDocumentSnapshots) {

                        postmodel post =
                                doc.toObject(postmodel.class);

                        if (post.userId != null &&
                                post.userId.equals(currentUserId)) {

                            post.docId = doc.getId();

                            list.add(post);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    updateEmptyState();

                });
    }

    private void updateEmptyState() {

        if (list.isEmpty()) {

            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            ((android.widget.TextView) emptyText)
                    .setText("You haven't posted any books yet");

        } else {

            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}