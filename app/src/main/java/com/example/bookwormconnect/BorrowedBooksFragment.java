package com.example.bookwormconnect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
public class BorrowedBooksFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<postmodel> list;
    postAdapter adapter;

    public BorrowedBooksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_posts,
                container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        list = new ArrayList<>();

        adapter = new postAdapter(list, "BORROWED");

        recyclerView.setAdapter(adapter);

        loadBorrowedBooks();

        return view;
    }

    private void loadBorrowedBooks() {

        String currentUserId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for(var doc : queryDocumentSnapshots){

                        postmodel post =
                                doc.toObject(postmodel.class);

                        if(post.borrowerId != null &&
                                post.borrowerId.equals(currentUserId)){

                            post.docId = doc.getId();

                            list.add(post);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}