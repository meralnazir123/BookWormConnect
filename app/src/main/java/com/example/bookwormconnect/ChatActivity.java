package com.example.bookwormconnect;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
public class ChatActivity extends AppCompatActivity {
    TextView txtAddress;

    private RecyclerView chatRecycler;
    private LinearLayout optionsLayout;
    private TextView ChatUsername;
    private ImageView imgChatProfile;
    private Button btnAvailable;

    private Button btnMeet;
    private Button btnDeposit;

    private ImageButton btnCamera;
private ImageButton deposit;
    private String chatId;
    private String requestId;
    private String currentUserId;
    private TextView txtDepositStatus;

    private ArrayList<ChatMessage> messages;
    private ChatAdapter adapter;

    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        txtAddress=findViewById(R.id.txtAddress);

        chatRecycler = findViewById(R.id.chatRecycler);
        ChatUsername = findViewById(R.id.chatUsername);
        imgChatProfile = findViewById(R.id.imgChatProfile);
        btnAvailable = findViewById(R.id.btnAvailable);
        btnMeet = findViewById(R.id.btnMeet);
        btnDeposit = findViewById(R.id.btnDeposit);
        btnCamera = findViewById(R.id.btnCamera);
        optionsLayout=findViewById(R.id.optionsLayout);
        String username =getIntent().getStringExtra("username");
        String otherUserId = getIntent().getStringExtra("otherUserId");
        chatId = getIntent().getStringExtra("chatId");
        requestId = getIntent().getStringExtra("requestId");
        txtDepositStatus=findViewById(R.id.txtDepositStatus);
        deposit=findViewById(R.id.Deposit);
        deposit.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, DepositActivity.class);

            intent.putExtra("chatId", chatId);
            intent.putExtra("requestId", requestId);
            startActivity(intent);
        });

        if (otherUserId != null) {

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener(document -> {

                        if (document.exists()) {

                            ChatUsername.setText(document.getString("username"));

                            String imageUrl =
                                    document.getString("profileImageUrl");

                            Glide.with(ChatActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.user)
                                    .circleCrop()
                                    .into(imgChatProfile);
                        }
                    });

        }
        currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();
        checkDepositStatus();
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String address = document.getString("address");

                        if (address != null) {
                            txtAddress.setText("Address: "+ address);
                        }

                        String senderId = document.getString("senderId");

                        if (currentUserId.equals(senderId)) {
                            btnAvailable.setText("Is this book available?");
                            btnMeet.setText("When can we meet?");
                            btnDeposit.setText("I have sent the deposit.");

                        } else {
                            btnAvailable.setText("Yes, it is available.");
                            btnMeet.setText("We can meet tomorrow.");
                            btnDeposit.setText("I have received the deposit.");
                        }
                    }
                });

        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages);

        chatRecycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        chatRecycler.setAdapter(adapter);

        imagePicker =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uploadReceipt(uri);
                            }
                        });
        btnAvailable.setOnClickListener(v ->
                sendMessage(btnAvailable.getText().toString()));

        btnMeet.setOnClickListener(v ->
                sendMessage(btnMeet.getText().toString()));

        btnDeposit.setOnClickListener(v ->
                sendMessage(btnDeposit.getText().toString()));

        btnCamera.setOnClickListener(v ->
                imagePicker.launch("image/*"));
        checkChatEnabled();
        loadMessages();
    }
    private void checkChatEnabled() {
        if (requestId == null) {
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) {
                        return;
                    }
                    Boolean enabled = doc.getBoolean("chatEnabled");
                    if (enabled != null && enabled) {
                        optionsLayout.setEnabled(true);
                    } else {
                        finish();
                    }
                });
    }
    private void sendMessage(String text) {
        String key = FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(chatId)
                .child("messages")
                .push()
                .getKey();
        if (key == null) return;
        ChatMessage msg =
                new ChatMessage(
                        currentUserId,
                        text,
                        "",
                        "text",
                        System.currentTimeMillis()
                );
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(chatId)
                .child("messages")
                .child(key)
                .setValue(msg);
    }
    private void uploadReceipt(Uri uri) {
        StorageReference ref =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child("receipts")
                        .child(System.currentTimeMillis() + ".jpg");
        ref.putFile(uri)
                .continueWithTask(task -> ref.getDownloadUrl())
                .addOnSuccessListener(downloadUri -> {
                    String key =
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Chats")
                                    .child(chatId)
                                    .child("messages")
                                    .push()
                                    .getKey();
                    if (key == null) return;
                    ChatMessage msg =
                            new ChatMessage(
                                    currentUserId,
                                    "",
                                    downloadUri.toString(),
                                    "image",
                                    System.currentTimeMillis()
                            );
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Chats")
                            .child(chatId)
                            .child("messages")
                            .child(key)
                            .setValue(msg);
                });
    }

    private void checkDepositStatus() {

        if (requestId == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("deposits")
                .whereEqualTo("requestId", requestId)

                .limit(1)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {
                        return;
                    }

                    if (snapshot == null || snapshot.isEmpty()) {

                        txtDepositStatus.setText("Deposit: Not Paid");
                        deposit.setEnabled(true);

                        return;
                    }

                    String status =
                            snapshot.getDocuments()
                                    .get(0)
                                    .getString("status");

                    if ("pending".equals(status)) {

                        txtDepositStatus.setText(
                                "Deposit: Pending Verification"
                        );

                        deposit.setEnabled(false);

                    } else if ("verified".equals(status)) {

                        txtDepositStatus.setText(
                                "Deposit: Verified"
                        );

                        deposit.setEnabled(false);

                    } else if ("rejected".equals(status)) {

                        txtDepositStatus.setText(
                                "Deposit: Rejected"
                        );

                        deposit.setEnabled(true);

                    }
                });
    }
    private void loadMessages() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(chatId)
                .child("messages")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {
                                messages.clear();
                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {
                                    ChatMessage msg =
                                            ds.getValue(
                                                    ChatMessage.class);
                                    if (msg != null) {
                                        messages.add(msg);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                                if (!messages.isEmpty()) {
                                    chatRecycler.smoothScrollToPosition(
                                            messages.size() - 1
                                    );
                                }
                            }
                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }
}