package com.example.bookwormconnect;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private String category;
    RecyclerView recyclerView;
    postAdapter adapter;
    List<postmodel> postList = new ArrayList<>();

    public static CategoriesFragment newInstance(String category) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }
        if (getArguments() != null) {
            category = getArguments().getString("category");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).hideSearchBar();
        ((HomeActivity) requireActivity()).hideCamera();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        recyclerView = view.findViewById(R.id.RecyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new postAdapter(postList);
        recyclerView.setAdapter(adapter);

        loadPostsByCategory();  //load posts

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadPostsByCategory() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .whereEqualTo("bookType", category)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    postList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        postmodel post = doc.toObject(postmodel.class);
                        postList.add(post);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}