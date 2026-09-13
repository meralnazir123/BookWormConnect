package com.example.bookwormconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatlistFragment extends Fragment {

    RecyclerView chatListRecycler;
    View emptyChatText;

    ArrayList<ChatListItem> chatList;
    ChatListAdapter adapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_chatlist,
                container,
                false
        );

        chatListRecycler =
                view.findViewById(
                        R.id.chatListRecycler
                );

        emptyChatText =
                view.findViewById(
                        R.id.emptyChatText
                );

        chatList = new ArrayList<>();

        adapter = new ChatListAdapter(
                requireContext(),
                chatList
        );

        chatListRecycler.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        chatListRecycler.setAdapter(adapter);

        loadChats();

        return view;
    }

    private void loadChats() {

        String currentUid =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("chats")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            chatList.clear();

                            queryDocumentSnapshots
                                    .getDocuments()
                                    .forEach(doc -> {

                                        ArrayList<String> participants =
                                                (ArrayList<String>)
                                                        doc.get("participants");

                                        if (participants != null &&
                                                participants.contains(
                                                        currentUid
                                                )) {

                                            String otherUserId;

                                            if (participants
                                                    .get(0)
                                                    .equals(currentUid)) {

                                                otherUserId =
                                                        participants.get(1);

                                            } else {

                                                otherUserId =
                                                        participants.get(0);
                                            }

                                            String requestId =
                                                    doc.getString(
                                                            "requestId"
                                                    );

                                            FirebaseDatabase
                                                    .getInstance()
                                                    .getReference("users")
                                                    .child(otherUserId)
                                                    .get()
                                                    .addOnSuccessListener(
                                                            userSnapshot -> {

                                                                String username;

                                                                if (userSnapshot.exists()) {

                                                                    String name =
                                                                            userSnapshot
                                                                                    .child("username")
                                                                                    .getValue(
                                                                                            String.class
                                                                                    );

                                                                    if (name != null &&
                                                                            !name.isEmpty()) {

                                                                        username =
                                                                                name;
                                                                    } else {
                                                                        username = "Unknown User";
                                                                    }
                                                                } else {
                                                                    username = "Unknown User";
                                                                }

                                                                FirebaseFirestore
                                                                        .getInstance()
                                                                        .collection(
                                                                                "requests"
                                                                        )
                                                                        .document(
                                                                                requestId
                                                                        )
                                                                        .get()
                                                                        .addOnSuccessListener(
                                                                                requestDoc -> {

                                                                                    String description =
                                                                                            "";

                                                                                    if (requestDoc.exists()) {

                                                                                        String bookDescription =
                                                                                                requestDoc
                                                                                                        .getString(
                                                                                                                "bookDescription"
                                                                                                        );

                                                                                        if (bookDescription != null) {

                                                                                            description =
                                                                                                    bookDescription;
                                                                                        }
                                                                                    }

                                                                                    chatList.add(
                                                                                            new ChatListItem(
                                                                                                    doc.getId(),
                                                                                                    otherUserId,
                                                                                                    username,
                                                                                                    requestId,
                                                                                                    description
                                                                                            )
                                                                                    );

                                                                                    adapter.notifyDataSetChanged();

                                                                                    updateEmptyState();
                                                                                }
                                                                        );
                                                            }
                                                    );
                                        }
                                    });

                            updateEmptyState();
                        }
                );
    }

    private void updateEmptyState() {

        if (chatList.isEmpty()) {

            chatListRecycler.setVisibility(
                    View.GONE
            );

            emptyChatText.setVisibility(
                    View.VISIBLE
            );

        } else {

            chatListRecycler.setVisibility(
                    View.VISIBLE
            );

            emptyChatText.setVisibility(
                    View.GONE
            );
        }
    }
}