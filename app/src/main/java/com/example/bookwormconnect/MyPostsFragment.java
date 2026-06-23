package com.example.bookwormconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MyPostsFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<postmodel> list;
    postAdapter adapter;

    DatabaseReference reference;

    public MyPostsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(getContext())
        );

        list = new ArrayList<>();

        adapter = new postAdapter(list, "MY_POSTS");

        recyclerView.setAdapter(adapter);

        loadMyPosts();

        return view;
    }
    private void loadMyPosts() {

        String currentUserId =
                com.google.firebase.auth.FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                            : queryDocumentSnapshots) {

                        postmodel post = doc.toObject(postmodel.class);

                        if(post.userId != null &&
                                post.userId.equals(currentUserId)) {

                            post.docId = doc.getId();

                            list.add(post);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}