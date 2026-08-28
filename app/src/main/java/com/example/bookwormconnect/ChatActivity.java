package com.example.bookwormconnect;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────────
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ChipGroup chipGroupQuestions;   // sender's question chips
    private ChipGroup chipGroupAnswers;     // receiver's answer chips
    private HorizontalScrollView scrollQuestions;
    private HorizontalScrollView scrollAnswers;
    private EditText editTextCustomMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private TextView tvChipLabel;

    // ── Data ───────────────────────────────────────────────────────────
    private String chatId, requestId;
    private String currentUserId;
    private String senderId;        // the original request creator
    private String ownerId;         // the book owner
    private String bookTitle  = "";
    private String bookGenre  = "General";
    private boolean isSender  = false;

    private com.example.bookwormconnect.ChatMessageAdapter adapter;
    private final List<com.example.bookwormconnect.ChatMessage> messageList = new ArrayList<>();

    private ListenerRegistration messagesListener;

    // ── Firebase ───────────────────────────────────────────────────────
    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private final OkHttpClient      http = new OkHttpClient();

    // ── FCM endpoint (Server-side token approach via OkHttp) ───────────
    // Replace YOUR_SERVER_KEY with the value from Firebase Console →
    // Project Settings → Cloud Messaging → Server key
    private static final String FCM_URL    =
            "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY =
            "YOUR_SERVER_KEY";   // ← replace

    // ── Predefined questions per genre (hardcoded fallback) ────────────
    private static final Map<String, List<String>> GENRE_QUESTIONS = new HashMap<>();
    static {
        GENRE_QUESTIONS.put("Fiction", Arrays.asList(
                "Is this book still available?",
                "How long can I borrow it?",
                "What's the condition of the book?",
                "Can I pick it up today?",
                "Is it the original edition?",
                "Has it been read many times?"
        ));
        GENRE_QUESTIONS.put("Science", Arrays.asList(
                "Is this the latest edition?",
                "Does it include exercises/solutions?",
                "Is it suitable for beginners?",
                "Any highlighting or notes inside?",
                "Can I borrow for 2 weeks?",
                "Is the book still available?"
        ));
        GENRE_QUESTIONS.put("History", Arrays.asList(
                "Is this book available now?",
                "What time period does it cover?",
                "Is it in good condition?",
                "Can I keep it for 10 days?",
                "Is it a hardcover or paperback?",
                "Are there maps or illustrations inside?"
        ));
        GENRE_QUESTIONS.put("Self-Help", Arrays.asList(
                "Is this book available?",
                "Did you find it helpful?",
                "How many pages is it?",
                "Can I borrow for 1 week?",
                "Is there a summary inside?",
                "Is it in readable condition?"
        ));
        // Default / General
        GENRE_QUESTIONS.put("General", Arrays.asList(
                "Is this book available?",
                "How long can I borrow it?",
                "What is the condition?",
                "Can we arrange a pickup?",
                "Is it available this week?",
                "How many pages does it have?"
        ));
    }

    // ── Predefined answers keyed by simplified question text ───────────
    private static final Map<String, List<String>> QUESTION_ANSWERS = new HashMap<>();
    static {
        QUESTION_ANSWERS.put("is this book still available", Arrays.asList(
                "Yes, it's available!",
                "Sorry, it's already lent out.",
                "Available from next week.",
                "Yes, come pick it up anytime!"
        ));
        QUESTION_ANSWERS.put("how long can i borrow it", Arrays.asList(
                "Up to 1 week.",
                "2 weeks maximum.",
                "10 days is fine.",
                "A month if you need it."
        ));
        QUESTION_ANSWERS.put("what's the condition of the book", Arrays.asList(
                "It's in excellent condition.",
                "Good condition, minor wear.",
                "Fair condition, still readable.",
                "Some highlighting inside, FYI."
        ));
        QUESTION_ANSWERS.put("can i pick it up today", Arrays.asList(
                "Yes, I'm free after 5 PM.",
                "Tomorrow works better for me.",
                "Anytime between 10 AM–8 PM.",
                "Let's meet at the library."
        ));
        QUESTION_ANSWERS.put("is it the latest edition", Arrays.asList(
                "Yes, it's the latest edition.",
                "It's the 2019 edition.",
                "It's the 3rd edition.",
                "Not sure, check the cover."
        ));
        QUESTION_ANSWERS.put("is it suitable for beginners", Arrays.asList(
                "Yes, very beginner-friendly!",
                "Needs some prior knowledge.",
                "Great for all levels.",
                "Has an introductory chapter."
        ));
        // Default answers for unknown questions
        QUESTION_ANSWERS.put("default", Arrays.asList(
                "Sure, sounds good!",
                "Let me check and get back to you.",
                "No problem at all.",
                "That works for me!"
        ));
    }

    // ──────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        bindViews();
        setupToolbar();

        chatId          = getIntent().getStringExtra("chatId");
        requestId       = getIntent().getStringExtra("requestId");
        currentUserId   = auth.getUid();

        setupRecyclerView();
        checkChatEnabled();
    }

    // ── Bind views ─────────────────────────────────────────────────────
    private void bindViews() {
        toolbar              = findViewById(R.id.chatToolbar);
        recyclerView         = findViewById(R.id.chatRecyclerView);
        chipGroupQuestions   = findViewById(R.id.chipGroupQuestions);
        chipGroupAnswers     = findViewById(R.id.chipGroupAnswers);
        scrollQuestions      = findViewById(R.id.scrollQuestions);
        scrollAnswers        = findViewById(R.id.scrollAnswers);
        editTextCustomMessage = findViewById(R.id.editTextCustomMessage);
        btnSend              = findViewById(R.id.btnSend);
        progressBar          = findViewById(R.id.progressBar);
        tvChipLabel          = findViewById(R.id.tvChipLabel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Chat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new com.example.bookwormconnect.ChatMessageAdapter(messageList, currentUserId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    // ── Step 1: verify chat is enabled ─────────────────────────────────
    private void checkChatEnabled() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("requests")
                .document(requestId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;

                    Boolean enabled = doc.getBoolean("chatEnabled");
                    if (Boolean.TRUE.equals(enabled)) {
                        senderId  = doc.getString("senderId");
                        ownerId   = doc.getString("ownerId");
                        bookTitle = doc.getString("bookTitle") != null
                                ? doc.getString("bookTitle") : "Book";
                        bookGenre = doc.getString("bookGenre") != null
                                ? doc.getString("bookGenre") : "General";

                        isSender = currentUserId.equals(senderId);

                        // Update toolbar with book title
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setTitle(bookTitle);

                        progressBar.setVisibility(View.GONE);

                        loadPredefinedChips();
                        listenForMessages();
                        setupSendButton();

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Chat is not enabled yet.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    // ── Step 2: load predefined chips from Firestore (or fallback) ─────
    private void loadPredefinedChips() {
        // Try to load dynamic questions from Firestore first
        db.collection("book_questions")
                .document(bookGenre)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> questions = null;
                    if (doc.exists()) {
                        questions = (List<String>) doc.get("questions");
                    }
                    if (questions == null || questions.isEmpty()) {
                        // fallback to hardcoded
                        questions = GENRE_QUESTIONS.getOrDefault(
                                bookGenre,
                                GENRE_QUESTIONS.get("General")
                        );
                    }
                    if (isSender) {
                        showQuestionChips(questions);
                    }
                    // Receiver chips shown after sender selects a question
                    // (see onQuestionChipSelected)
                })
                .addOnFailureListener(e ->
                        showQuestionChips(
                                GENRE_QUESTIONS.getOrDefault(bookGenre,
                                        GENRE_QUESTIONS.get("General"))));
    }

    /** Populate the SENDER question chips */
    private void showQuestionChips(List<String> questions) {
        chipGroupQuestions.removeAllViews();
        scrollQuestions.setVisibility(isSender ? View.VISIBLE : View.GONE);

        if (isSender) {
            tvChipLabel.setText("Quick Questions");
            tvChipLabel.setVisibility(View.VISIBLE);
        }

        for (String q : questions) {
            Chip chip = new Chip(this);
            chip.setText(q);
            chip.setChipBackgroundColorResource(R.color.chipBackground);
            chip.setTextColor(getResources().getColor(R.color.chipText, null));
            chip.setCheckable(false);
            chip.setOnClickListener(v -> onQuestionChipSelected(q));
            chipGroupQuestions.addView(chip);
        }
    }

    /** When sender taps a question chip → send it, then show receiver answer chips */
    private void onQuestionChipSelected(String questionText) {
        sendMessage(questionText, "predefined_question");
        // If current user is receiver, show their answers
        // (In a real scenario, the RECEIVER sees answer chips upon receiving a question)
        // Here we also show answers on sender side for demo; remove if not needed
    }

    /** Populate the RECEIVER answer chips based on the question received */
    private void showAnswerChips(String questionText) {
        chipGroupAnswers.removeAllViews();

        // Check Firestore for dynamic answers first
        String questionKey = questionText.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();

        db.collection("book_answers")
                .document(questionKey)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> answers = null;
                    if (doc.exists()) answers = (List<String>) doc.get("answers");
                    if (answers == null || answers.isEmpty()) {
                        answers = getLocalAnswers(questionKey);
                    }
                    populateAnswerChips(answers);
                })
                .addOnFailureListener(e ->
                        populateAnswerChips(getLocalAnswers(questionKey)));
    }

    private List<String> getLocalAnswers(String questionKey) {
        for (Map.Entry<String, List<String>> entry : QUESTION_ANSWERS.entrySet()) {
            if (questionKey.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return QUESTION_ANSWERS.get("default");
    }

    private void populateAnswerChips(List<String> answers) {
        chipGroupAnswers.removeAllViews();
        scrollAnswers.setVisibility(View.VISIBLE);
        tvChipLabel.setText("Quick Replies");
        tvChipLabel.setVisibility(View.VISIBLE);

        for (String ans : answers) {
            Chip chip = new Chip(this);
            chip.setText(ans);
            chip.setChipBackgroundColorResource(R.color.chipAnswerBackground);
            chip.setTextColor(getResources().getColor(R.color.chipText, null));
            chip.setCheckable(false);
            chip.setOnClickListener(v -> {
                sendMessage(ans, "predefined_answer");
                scrollAnswers.setVisibility(View.GONE);
                chipGroupAnswers.removeAllViews();
            });
            chipGroupAnswers.addView(chip);
        }
    }

    // ── Step 3: real-time message listener ─────────────────────────────
    private void listenForMessages() {
        messagesListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    messageList.clear();
                    String lastSenderQuestion = null;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            msg.setMessageId(doc.getId());
                            messageList.add(msg);

                            // Track last question sent by sender
                            if ("predefined_question".equals(msg.getType())
                                    && senderId.equals(msg.getSenderId())) {
                                lastSenderQuestion = msg.getText();
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty())
                        recyclerView.scrollToPosition(messageList.size() - 1);

                    // Show answer chips for receiver when there's a new question
                    if (!isSender && lastSenderQuestion != null) {
                        // Only show if the last message is a question (not answered yet)
                        ChatMessage lastMsg = messageList.get(messageList.size() - 1);
                        if ("predefined_question".equals(lastMsg.getType())
                                && senderId.equals(lastMsg.getSenderId())) {
                            showAnswerChips(lastSenderQuestion);
                        }
                    }
                });
    }

    // ── Step 4: send button (custom text) ──────────────────────────────
    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            String text = editTextCustomMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(text, "custom");
            editTextCustomMessage.setText("");
        });
    }

    // ── Core: write message to Firestore + trigger notification ────────
    private void sendMessage(String text, String type) {
        if (chatId == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId",  currentUserId);
        msg.put("text",      text);
        msg.put("type",      type);               // predefined_question / predefined_answer / custom
        msg.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(ref -> sendPushNotification(text))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show());
    }

    // ── Push notification to the OTHER user ────────────────────────────
    private void sendPushNotification(String messageText) {
        // Determine the recipient's UID
        String recipientUid = isSender ? ownerId : senderId;
        if (recipientUid == null) return;

        // Fetch their FCM token from Firestore
        db.collection("users")
                .document(recipientUid)
                .get()
                .addOnSuccessListener(doc -> {
                    String token = doc.getString("fcmToken");
                    if (token == null || token.isEmpty()) return;

                    // Fetch sender's name for the notification
                    db.collection("users")
                            .document(currentUserId)
                            .get()
                            .addOnSuccessListener(senderDoc -> {
                                String senderName = senderDoc.getString("username");
                                if (senderName == null) senderName = "Someone";
                                dispatchFcmNotification(token, senderName, messageText);
                            });
                });
    }

    /**
     * Sends FCM notification via OkHttp (legacy HTTP API).
     * Replace with FCM v1 / Cloud Functions if needed.
     */
    private void dispatchFcmNotification(String token, String senderName, String body) {
        try {
            JSONObject notification = new JSONObject();
            notification.put("title", "📚 " + senderName + " sent a message");
            notification.put("body",  body);
            notification.put("sound", "default");

            JSONObject data = new JSONObject();
            data.put("chatId",    chatId);
            data.put("requestId", requestId);
            data.put("bookTitle", bookTitle);

            JSONObject payload = new JSONObject();
            payload.put("to",           token);
            payload.put("notification", notification);
            payload.put("data",         data);
            payload.put("priority",     "high");

            RequestBody requestBody = RequestBody.create(
                    payload.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(FCM_URL)
                    .addHeader("Authorization", "key=" + SERVER_KEY)
                    .addHeader("Content-Type",  "application/json")
                    .post(requestBody)
                    .build();

            http.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) {
                    // Silent — notification delivery failure shouldn't disrupt UX
                }
                @Override public void onResponse(Call call, Response response) {
                    response.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) messagesListener.remove();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
