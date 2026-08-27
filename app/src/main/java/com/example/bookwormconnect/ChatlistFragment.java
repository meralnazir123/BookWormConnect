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
public class ChatlistFragment extends Fragment {
    RecyclerView chatListRecycler;
    ArrayList<ChatListItem> chatList;
    ChatListAdapter adapter;
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_chatlist,
                        container,
                        false);

        chatListRecycler =
                view.findViewById(R.id.chatListRecycler);

        chatList = new ArrayList<>();

        adapter =
                new ChatListAdapter(
                        requireContext(),
                        chatList);

        chatListRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext()));

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
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    chatList.clear();

                    queryDocumentSnapshots
                            .getDocuments()
                            .forEach(doc -> {

                                ArrayList<String> participants = (ArrayList<String>) doc.get("participants");
                                if (participants != null &&
                                        participants.contains(currentUid)) {
                                    String otherUserId;
                                    if (participants.get(0)
                                            .equals(currentUid)) {
                                        otherUserId =
                                                participants.get(1);
                                    } else {
                                        otherUserId =
                                                participants.get(0);
                                    }
                                    String requestId = doc.getString("requestId");
                                    FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(otherUserId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                final String username;

                                                if (userDoc.exists()
                                                        && userDoc.getString("username") != null) {
                                                    username = userDoc.getString("username");
                                                } else {
                                                    username = "Unknown User";
                                                }
                                                FirebaseFirestore.getInstance()
                                                        .collection("requests")
                                                        .document(requestId)
                                                        .get()
                                                        .addOnSuccessListener(requestDoc -> {

                                                            String description = "";

                                                            if (requestDoc.exists()) {
                                                                description = requestDoc.getString("bookDescription");
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
                                                        });
                                            });
                                }

                            });
                });
    }}