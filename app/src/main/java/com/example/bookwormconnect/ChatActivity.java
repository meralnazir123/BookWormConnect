package com.example.bookwormconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    LinearLayout optionsLayout;
    String chatId, requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        optionsLayout = findViewById(R.id.optionsLayout);

        chatId = getIntent().getStringExtra("chatId");
        requestId = getIntent().getStringExtra("requestId");

        checkChatEnabled();
    }

    private void checkChatEnabled() {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .addSnapshotListener((doc, error) -> {

                    boolean enabled = doc.getBoolean("chatEnabled");

                    if (enabled) {
                        loadPredefinedMessages();
                    } else {
                        Toast.makeText(this, "Chat not enabled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void loadPredefinedMessages() {
        FirebaseFirestore.getInstance()
                .collection("predefined_messages")
                .get()
                .addOnSuccessListener(query -> {

                    for (DocumentSnapshot doc : query) {
                        String text = doc.getString("text");

                        Button btn = new Button(this);
                        btn.setText(text);

                        btn.setOnClickListener(v -> sendMessage(text));

                        optionsLayout.addView(btn);
                    }
                });
    }

    private void sendMessage(String text) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", FirebaseAuth.getInstance().getUid());
        msg.put("text", text);
        msg.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg);
    }
}